package cr.ac.una.eif400.cyphail.compiler.codegen;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * Generador de código: transpila el AST de una consulta Cyphail a un goal de Prolog
 * que el engine resuelve con {@code cy_exec_query(Goal, Headers, Columns, Rows)}.<br><br>
 *
 * Igual que el {@code SemanticAnalyzer}, recorre el AST con {@code switch} exhaustivos sobre
 * el {@link AstNode} sellado. Se usan tres recorridos según el lugar donde aparece el nodo:
 * {@link #visit} para cláusulas y patrones (agrega goals), {@link #term} para expresiones que
 * producen un valor (devuelve un término de Prolog) y {@link #condition} para las condiciones
 * del {@code WHERE} (devuelve el término que evalúa {@code cy_where}).<br><br>
 *
 * Las variables de Cyphail se renombran con el prefijo {@code __} y la inicial en mayúscula
 * ({@code a} pasa a {@code __A}). Las propiedades consultadas se guardan en variables propias
 * ({@code a.edad} pasa a {@code __Aedad}) que se leen una sola vez por consulta. Una propiedad
 * que no existe vale {@code null}, y ese valor se propaga por los cálculos y las condiciones.<br><br>
 *
 * Las cláusulas {@code CREATE}, {@code SET}, {@code REMOVE} y {@code DELETE} se traducen a goals
 * que modifican el grafo en memoria. Van después de los goals del {@code MATCH}, así que se
 * ejecutan una vez por cada fila encontrada.<br><br>
 *
 * <b>Sugerido por Claude (Anthropic)</b>. Prompt: Elabore el visitor del codegen en base a lo que
 * se tiene de predicados de momento, que sea con switch exhaustivo. Tome de base la demos de MQI
 * o el visitor del pretty printer. Que se pueda hacer lo que dice el .md
 *
 * @author Andrey Solano
 * @author Sebastián González
 * @version P2
 */
public class CodeGen {

    /**
     * Si una variable de patrón es de nodo o de relación, con los predicados del engine que
     * le corresponden a cada una para consultarla y modificarla.
     */
    private enum Kind {
        NODE("cy_node_label", "cy_node_prop", "cy_set_node_prop", "cy_remove_node_prop", "cy_delete_node"),
        RELATIONSHIP("cy_edge_type", "cy_edge_prop", "cy_set_edge_prop", "cy_remove_edge_prop", "cy_delete_edge");

        private final String label;
        private final String property;
        private final String setProperty;
        private final String removeProperty;
        private final String delete;

        Kind(String label, String property, String setProperty, String removeProperty, String delete) {
            this.label = label;
            this.property = property;
            this.setProperty = setProperty;
            this.removeProperty = removeProperty;
            this.delete = delete;
        }
    }

    private final List<String> goals = new ArrayList<>();
    private final List<String> headers = new ArrayList<>();
    private final List<String> columns = new ArrayList<>();

    /** Variable o propiedad de Cyphail ({@code a}, {@code a.edad}) y su variable de Prolog. */
    private final Map<String, String> names = new HashMap<>();
    /** Tipo de cada variable de patrón, en el orden en que se declaran (es el orden de {@code RETURN *}). */
    private final Map<String, Kind> kinds = new LinkedHashMap<>();
    private final Set<String> taken = new HashSet<>();
    private List<String> matchRelationships = new ArrayList<>();
    private int counter = 0;

    private CodeGen() {}

    /**
     * Transpila una consulta ya validada por el {@code SemanticAnalyzer}.
     *
     * @param query la raíz del AST
     * @return el goal que se envía al engine, sin el punto final
     * @throws UnsupportedOperationException si la consulta usa algo que el engine todavía no resuelve
     */
    public static String generate(AstNode query) {
        var generator = new CodeGen();
        generator.visit(query);

        return "cy_exec_query(%s, %s, %s, Rows)".formatted(
                allOf(generator.goals),
                list(generator.headers.stream().map(CodeGen::atom).toList()),
                list(generator.columns));
    }

    /**
     * Recorre cláusulas, patrones y proyecciones, agregando sus goals en orden.
     *
     * @param node el nodo actual del AST
     */
    private void visit(AstNode node) {
        switch (node) {
            case Query(var matches, var updates, var ret) -> {
                matches.forEach(this::visit);
                updates.forEach(this::visit);
                ret.forEach(this::visit);
            }

            case Match(var patterns, var where) -> {
                matchRelationships = new ArrayList<>();
                patterns.forEach(this::visit);
                distinctRelationships();
                where.forEach(this::visit);
            }

            case Where(var condition) -> goals.add("cy_where(%s)".formatted(condition(condition)));

            case Pattern pattern -> {
                var variable = declare(pattern.variable(), Kind.NODE);
                goals.add("cy_node(%s)".formatted(variable));
                constrainNode(variable, pattern);
            }

            case Path(var from, var relationship, var to) -> {
                var source = declare(from.variable(), Kind.NODE);
                var target = declare(to.variable(), Kind.NODE);
                relationship(relationship, source, target);
                constrainNode(source, from);
                constrainNode(target, to);
            }

            case Return(var distinct, var projections, var modifiers) -> {
                if (distinct) throw unsupported("RETURN DISTINCT");
                if (!modifiers.isEmpty()) throw unsupported("ORDER BY, SKIP and LIMIT");
                projections.forEach(this::visit);
            }

            case SimpleProjection(var expression) -> {
                var value = term(expression);   // primero term: si no se puede traducir, el error sale de ahí
                column(source(expression), value);
            }

            case AliasedProjection(var expression, var alias) -> column(alias.token().value(), term(expression));

            case Leaf(var token) when token.type() == TokenType.MULTIPLY ->
                    kinds.keySet().forEach(variable -> column(variable, names.get(variable)));

            case Create(var patterns) -> patterns.forEach(this::create);

            case SetClause(var items) -> items.forEach(this::visit);

            case RemoveClause(var items) -> items.forEach(this::visit);

            case Delete(var detach, var targets) -> targets.forEach(target -> goals.add(delete(target, detach)));

            case SetProperty(PropertyAccess(Leaf(var variable), Leaf(var key)), var value) -> {
                var newValue = term(value);
                goals.add("%s(%s, %s, %s)".formatted(
                        kinds.get(variable.value()).setProperty, names.get(variable.value()), atom(key.value()), newValue));
                forget(variable.value(), key.value());
            }

            case RemoveProperty(PropertyAccess(Leaf(var variable), Leaf(var key))) -> {
                goals.add("%s(%s, %s)".formatted(
                        kinds.get(variable.value()).removeProperty, names.get(variable.value()), atom(key.value())));
                forget(variable.value(), key.value());
            }

            case SetLabels(var variable, var labels) -> labels.forEach(label -> goals.add(
                    "cy_set_node_label(%s, %s)".formatted(names.get(variable.token().value()), atom(label.token().value()))));

            case RemoveLabels(var variable, var labels) -> labels.forEach(label -> goals.add(
                    "cy_remove_node_label(%s, %s)".formatted(names.get(variable.token().value()), atom(label.token().value()))));

            case SetProperty _, RemoveProperty _ -> throw unsupported("nested property access");

            case SetAllProperties _ -> throw unsupported("SET of all the properties");

            case Leaf _, Node _, Relationship _, RelRange _, Property _, PropertyAccess _, Comparison _,
                 Or _, And _, Not _, Arithmetic _, Unary _, NullTest _, FunctionCall _, LabelTest _,
                 Order _, SortItem _, Skip _, Limit _ ->
                    throw new IllegalStateException("Unexpected node outside an expression: " + node);
        }
    }

    /**
     * Traduce una expresión que produce un valor. Si para obtenerlo hace falta un cálculo o leer
     * una propiedad, agrega ese goal antes y devuelve la variable que lo guarda.
     *
     * @param expression la expresión de Cyphail
     * @return el término de Prolog con su valor
     */
    private String term(AstNode expression) {
        return switch (expression) {
            case Leaf(var token) -> switch (token.type()) {
                case IDENTIFIER -> names.get(token.value());
                case NUMBER -> token.value();
                case STRING_LITERAL -> atom(token.value().substring(1, token.value().length() - 1));
                case TRUE -> "true";
                case FALSE -> "false";
                case NULL -> "null";
                default -> throw new IllegalStateException("Unexpected token in expression: " + token);
            };

            case PropertyAccess(Leaf(var variable), Leaf(var key)) -> property(variable.value(), key.value());

            case PropertyAccess _ -> throw unsupported("nested property access");

            case Arithmetic(var left, var operator, var right) ->
                    calculate("%s %s %s".formatted(term(left), arithmetic(operator), term(right)));

            case Unary(var operator, var operand) -> calculate("%s(%s)".formatted(operator.token().value(), term(operand)));

            case FunctionCall(var name, var arguments) when isType(name, arguments) -> {
                var type = fresh("Type");
                goals.add("%s(%s, %s)".formatted(Kind.RELATIONSHIP.label, term(arguments.getFirst()), type));
                yield type;
            }

            case FunctionCall(var name, var _) -> throw unsupported("function " + name.token().value());

            case Comparison _, Or _, And _, Not _, NullTest _, LabelTest _ ->
                    throw unsupported("boolean expressions outside WHERE");

            case Query _, Match _, Where _, Pattern _, Path _, Relationship _, RelRange _, Node _, Property _,
                 Return _, SimpleProjection _, AliasedProjection _, Order _, SortItem _, Skip _, Limit _,
                 UpdatingClause _, SetProperty _, SetAllProperties _, SetLabels _, RemoveProperty _,
                 RemoveLabels _ ->
                    throw new IllegalStateException("Not an expression: " + expression);
        };
    }

    /**
     * Traduce la condición de un {@code WHERE} al término que evalúa {@code cy_where}, que maneja
     * tres valores: {@code true}, {@code false} y {@code null}. Las propiedades que usa se leen
     * antes, como en {@code cy_node_prop(N, edad, E), cy_where(not(cy_greater_than(E, 30)))}.
     *
     * @param expression la condición de Cyphail
     * @return el término de Prolog con la condición
     */
    private String condition(AstNode expression) {
        return switch (expression) {
            case And(var left, var right) -> conjunction(condition(left), condition(right));

            case Or(var left, var right) -> "or(%s, %s)".formatted(condition(left), condition(right));

            case Not(var inner) -> "not(%s)".formatted(condition(inner));

            case Comparison(var left, var operator, var right) ->
                    "%s(%s, %s)".formatted(comparison(operator), term(left), term(right));

            case NullTest(var inner, var negated) -> {
                var test = "is_null(%s)".formatted(term(inner));
                yield negated ? "not(%s)".formatted(test) : test;
            }

            case LabelTest(Leaf(var variable), var labels) -> {
                var kind = kinds.get(variable.value());
                var prolog = names.get(variable.value());
                yield labels.stream()
                        .map(label -> "%s(%s, %s)".formatted(kind.label, prolog, atom(label.token().value())))
                        .reduce(CodeGen::conjunction)
                        .orElseThrow();
            }

            case LabelTest _ -> throw unsupported("label test on something other than a variable");

            case Leaf _, PropertyAccess _, FunctionCall _, Arithmetic _, Unary _ ->
                    "cy_equal(%s, true)".formatted(term(expression));

            case Query _, Match _, Where _, Pattern _, Path _, Relationship _, RelRange _, Node _, Property _,
                 Return _, SimpleProjection _, AliasedProjection _, Order _, SortItem _, Skip _, Limit _,
                 UpdatingClause _, SetProperty _, SetAllProperties _, SetLabels _, RemoveProperty _,
                 RemoveLabels _ ->
                    throw new IllegalStateException("Not a condition: " + expression);
        };
    }

    /**
     * Agrega la relación entre dos nodos ya nombrados, con su tipo y sus propiedades.
     * Para {@code <-[]-} se invierten los extremos y para {@code -[]-} se aceptan los dos sentidos.
     */
    private void relationship(Relationship relationship, String source, String target) {
        if (!relationship.range().isEmpty()) {
            goals.add(path(relationship, source, target));
            return;
        }

        var edge = declare(relationship.variable(), Kind.RELATIONSHIP);
        matchRelationships.add(edge);

        goals.add(switch (relationship.direction()) {
            case RIGHT -> edge(edge, source, target);
            case LEFT -> edge(edge, target, source);
            case NONE -> anyOf(List.of(edge(edge, source, target), edge(edge, target, source)));
        });

        if (!relationship.types().isEmpty())
            goals.add(anyOf(relationship.types().stream()
                    .map(type -> "%s(%s, %s)".formatted(Kind.RELATIONSHIP.label, edge, atom(type.token().value())))
                    .toList()));

        relationship.properties().forEach(property -> goals.add(
                lookup(Kind.RELATIONSHIP, edge, property.key().token().value(), term(property.value()))));
    }

    /**
     * Arma el goal {@code cy_path} de una relación de longitud variable. El camino no se retorna,
     * así que va como variable anónima; por eso la relación no puede tener variable propia.
     *
     * @param relationship la relación con su rango, por ejemplo {@code -[:AMIGO_DE*1..3]->}
     * @param source la variable de Prolog del nodo de origen
     * @param target la variable de Prolog del nodo de destino
     * @return el goal con la dirección, los límites, los tipos y las propiedades como opciones
     */
    private String path(Relationship relationship, String source, String target) {
        if (!relationship.variable().isEmpty()) throw unsupported("variable on a variable-length relationship");

        var range = (RelRange) relationship.range().getFirst();
        var options = new ArrayList<String>();

        options.add("edge_direction(%s)".formatted(switch (relationship.direction()) {
            case RIGHT -> "right";
            case LEFT -> "left";
            case NONE -> "none";
        }));
        options.add("bounds(%s, %s)".formatted(
                range.min().isEmpty() ? "1" : range.min().getFirst().token().value(),
                range.max().isEmpty() ? "infinite" : range.max().getFirst().token().value()));
        if (!relationship.types().isEmpty())
            options.add("edge_types(%s)".formatted(list(relationship.types().stream()
                    .map(type -> atom(type.token().value())).toList())));
        if (!relationship.properties().isEmpty())
            options.add("edge_props(%s)".formatted(list(relationship.properties().stream()
                    .map(property -> "%s-%s".formatted(atom(property.key().token().value()), term(property.value())))
                    .toList())));

        return "cy_path(%s, %s, _, %s)".formatted(source, target, list(options));
    }

    /** Agrega los labels y las propiedades escritos en el patrón de un nodo. */
    private void constrainNode(String variable, Pattern pattern) {
        pattern.labels().forEach(label -> goals.add(
                "%s(%s, %s)".formatted(Kind.NODE.label, variable, atom(label.token().value()))));
        pattern.properties().forEach(property -> goals.add(
                lookup(Kind.NODE, variable, property.key().token().value(), term(property.value()))));
    }

    /**
     * Agrega los goals que crean un patrón del {@code CREATE}. Los nodos que ya tienen variable
     * enlazada (por el {@code MATCH} o por una parte anterior del mismo patrón) se reutilizan;
     * los demás se crean. La semántica ya garantiza que la relación tiene un tipo y una dirección.
     *
     * @param pattern un nodo ({@link Pattern}) o un nodo-relación-nodo ({@link Path})
     */
    private void create(AstNode pattern) {
        if (!(pattern instanceof Path(var from, var relationship, var to))) {
            createNode((Pattern) pattern);
            return;
        }

        var source = createNode(from);
        var target = createNode(to);
        var type = atom(relationship.types().getFirst().token().value());
        var properties = pairs(relationship.properties());
        var edge = declare(relationship.variable(), Kind.RELATIONSHIP);

        goals.add(relationship.direction() == Relationship.Direction.LEFT
                ? createEdge(target, source, type, properties, edge)
                : createEdge(source, target, type, properties, edge));
    }

    /**
     * Devuelve la variable de un nodo del {@code CREATE}: si ya estaba enlazada la reutiliza y
     * si no agrega el goal que lo crea con sus etiquetas y propiedades.
     *
     * @param pattern el nodo escrito en el {@code CREATE}
     * @return la variable de Prolog con el id del nodo
     */
    private String createNode(Pattern pattern) {
        var bound = pattern.variable().stream().anyMatch(variable -> names.containsKey(variable.token().value()));
        var variable = declare(pattern.variable(), Kind.NODE);
        if (bound) return variable;

        var labels = list(pattern.labels().stream().map(label -> atom(label.token().value())).toList());
        var properties = pairs(pattern.properties());
        goals.add("cy_create_node(%s, %s, %s)".formatted(labels, properties, variable));
        return variable;
    }

    /**
     * Arma el goal que borra una variable del {@code DELETE}. Un nodo con relaciones solo se
     * borra con {@code DETACH}; borrar una relación nunca borra sus nodos.
     *
     * @param target la expresión escrita en el {@code DELETE}
     * @param detach si la cláusula es {@code DETACH DELETE}
     * @return el goal de Prolog
     */
    private String delete(AstNode target, boolean detach) {
        if (!(target instanceof Leaf(var token)) || token.type() != TokenType.IDENTIFIER)
            throw unsupported("DELETE of something other than a variable");

        var kind = kinds.get(token.value());
        var predicate = detach && kind == Kind.NODE ? "cy_detach_delete_node" : kind.delete;
        return "%s(%s)".formatted(predicate, names.get(token.value()));
    }

    /**
     * Olvida el valor leído de una propiedad que se acaba de cambiar, para que una lectura
     * posterior (por ejemplo en el {@code RETURN}) la vuelva a consultar y vea el valor nuevo.
     */
    private void forget(String variable, String key) {
        names.remove(variable + "." + key);
    }

    /** Lista de Prolog con las propiedades como pares {@code 'clave'-Valor}. */
    private String pairs(List<Property> properties) {
        return list(properties.stream()
                .map(property -> "%s-%s".formatted(atom(property.key().token().value()), term(property.value())))
                .toList());
    }

    /**
     * En un mismo {@code MATCH}, dos relaciones no pueden ser el mismo arco
     * (en {@code (a)-[]->(b), (b)-[]->(a)} los dos arcos deben ser distintos).
     */
    private void distinctRelationships() {
        for (int i = 0; i < matchRelationships.size(); i++)
            for (int j = i + 1; j < matchRelationships.size(); j++)
                goals.add("cy_not_equal(%s, %s)".formatted(matchRelationships.get(i), matchRelationships.get(j)));
    }

    /**
     * Devuelve la variable de Prolog de una variable de patrón, creándola la primera vez.
     * Un patrón sin variable recibe una variable anónima propia.
     */
    private String declare(List<Leaf> variable, Kind kind) {
        if (variable.isEmpty()) return "__" + ++counter;

        var name = variable.getFirst().token().value();
        kinds.putIfAbsent(name, kind);
        return names.computeIfAbsent(name, _ -> fresh(capitalize(name)));
    }

    /** Lee la propiedad una sola vez por consulta y devuelve la variable que guarda su valor. */
    private String property(String variable, String key) {
        return names.computeIfAbsent(variable + "." + key, _ -> {
            var value = fresh(capitalize(variable) + key);
            goals.add(lookup(kinds.get(variable), names.get(variable), key, value));
            return value;
        });
    }

    /** Agrega {@code cy_calculate(Expresión, Variable)} y devuelve la variable con el resultado. */
    private String calculate(String expression) {
        var result = "__" + ++counter;
        goals.add("cy_calculate(%s, %s)".formatted(expression, result));
        return result;
    }

    /**
     * Arma un nombre de variable de Prolog a partir de una sugerencia. Si ya está en uso
     * ({@code a} y {@code A} sugieren los dos {@code __A}) usa un número, que ninguna
     * variable de Cyphail puede producir porque un identificador no empieza con dígito.
     */
    private String fresh(String hint) {
        var candidate = "__" + hint;
        return taken.add(candidate) ? candidate : "__" + ++counter;
    }

    private void column(String header, String value) {
        headers.add(header);
        columns.add(value);
    }

    private static String lookup(Kind kind, String variable, String key, String value) {
        return "%s(%s, %s, %s)".formatted(kind.property, variable, atom(key), value);
    }

    private static String edge(String edge, String source, String target) {
        return "cy_edge(%s, %s, %s)".formatted(edge, source, target);
    }

    private static String createEdge(String source, String target, String type, String properties, String edge) {
        return "cy_create_edge(%s, %s, %s, %s, %s)".formatted(source, target, type, properties, edge);
    }

    /** Conjunción de dos condiciones de un {@code WHERE}. */
    private static String conjunction(String left, String right) {
        return "and(%s, %s)".formatted(left, right);
    }

    /** Conjunción de goals; sin goals es {@code true}. */
    private static String allOf(List<String> goals) {
        return goals.isEmpty() ? "true" : group(goals, ", ");
    }

    /** Disyunción de goals alternativos. */
    private static String anyOf(List<String> alternatives) {
        return group(alternatives, " ; ");
    }

    /** Un solo goal va tal cual; varios van entre paréntesis para no mezclarse con lo que los rodea. */
    private static String group(List<String> goals, String separator) {
        return goals.size() == 1 ? goals.getFirst() : goals.stream().collect(joining(separator, "(", ")"));
    }

    private static boolean isType(Leaf name, List<AstNode> arguments) {
        return name.token().value().equalsIgnoreCase("type") && arguments.size() == 1;
    }

    private static String comparison(Leaf operator) {
        return switch (operator.token().type()) {
            case EQUALS -> "cy_equal";
            case NOTEQUALS -> "cy_not_equal";
            case LESS_THAN -> "cy_less_than";
            case GREATER_THAN -> "cy_greater_than";
            case LESS_THAN_OR_EQUAL -> "cy_less_or_equal";
            case GREATER_THAN_OR_EQUAL -> "cy_greater_or_equal";
            default -> throw new IllegalStateException("Not a comparison operator: " + operator);
        };
    }

    private static String arithmetic(Leaf operator) {
        return operator.token().value().equals("%") ? "mod" : operator.token().value();
    }

    /**
     * Texto de Cyphail de una expresión, para el encabezado de su columna ({@code n.nombre}).
     * Solo llega aquí lo que {@link #term} ya aceptó.
     */
    private static String source(AstNode expression) {
        return switch (expression) {
            case Leaf(var token) -> token.value();
            case PropertyAccess(var target, var key) -> source(target) + "." + source(key);
            case FunctionCall(var name, var arguments) ->
                    source(name) + arguments.stream().map(CodeGen::source).collect(joining(", ", "(", ")"));
            case Arithmetic(var left, var operator, var right) ->
                    "%s %s %s".formatted(source(left), source(operator), source(right));
            case Unary(var operator, var operand) -> source(operator) + source(operand);

            case Query _, Match _, Where _, Pattern _, Path _, Relationship _, RelRange _, Node _, Property _,
                 Return _, SimpleProjection _, AliasedProjection _, Order _, SortItem _, Skip _, Limit _,
                 UpdatingClause _, SetProperty _, SetAllProperties _, SetLabels _, RemoveProperty _,
                 RemoveLabels _, Comparison _, Or _, And _, Not _, NullTest _, LabelTest _ ->
                    throw new IllegalStateException("No column for: " + expression);
        };
    }

    /** Átomo de Prolog entre comillas simples, escapando las comillas y las barras invertidas. */
    private static String atom(String text) {
        return "'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private static String list(List<String> items) {
        return "[" + String.join(", ", items) + "]";
    }

    private static String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static UnsupportedOperationException unsupported(String feature) {
        return new UnsupportedOperationException("Not supported yet: " + feature);
    }
}