package cr.ac.una.eif400.cyphail.compiler.codegen;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.semantic.records.AnalysisResult;
import cr.ac.una.eif400.cyphail.compiler.semantic.records.SymbolTable;
import cr.ac.una.eif400.cyphail.compiler.semantic.records.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * Transpila una consulta analizada a un goal de Prolog.
 *
 * La tabla semántica proporciona las categorías de las variables.
 * El generador mantiene los nombres Prolog, los temporales,
 * las propiedades consultadas y los goals producidos.
 *
 * @version P2
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @author Sebastian González
 * @author Wilson Umaña
 */
public class CodeGen {

    private final SymbolTable symbols;

    private final List<String> goals = new ArrayList<>();
    private final List<String> headers = new ArrayList<>();
    private final List<String> columns = new ArrayList<>();

    /**
     * Asociación entre variables o propiedades de Cyphail
     * y sus nombres en Prolog.
     */
    private final Map<String, String> names = new HashMap<>();

    /**
     * Variables de patrón en el orden recorrido por el generador.
     * Determina el orden de las columnas de RETURN *.
     */
    private final Set<String> patternVariables = new LinkedHashSet<>();

    private final Set<String> taken = new HashSet<>();

    private List<String> matchRelationships = new ArrayList<>();
    private int counter = 0;

    private CodeGen(SymbolTable symbols) {
        this.symbols = symbols;
    }

    /**
     * Genera código únicamente para un análisis sin errores.
     */
    public static String generate(AnalysisResult analysis) {
        if (!analysis.isValid()) {
            throw new IllegalArgumentException(
                    "Cannot generate code from a query with semantic errors"
            );
        }

        var generator = new CodeGen(analysis.symbols());
        generator.visit(analysis.ast());

        return "cy_exec_query(%s, %s, %s, Rows)".formatted(
                allOf(generator.goals),
                list(generator.headers.stream().map(CodeGen::atom).toList()),
                list(generator.columns)
        );
    }

    /**
     * Recorre cláusulas, patrones y proyecciones.
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

            case Where(var expression) ->
                    goals.add(
                            "cy_where(%s)".formatted(condition(expression))
                    );

            case Pattern pattern -> {
                var variable = bindPatternVariable(pattern.variable());

                goals.add("cy_node(%s)".formatted(variable));
                constrainNode(variable, pattern);
            }

            case Path(var from, var relationship, var to) -> {
                var source = bindPatternVariable(from.variable());
                var target = bindPatternVariable(to.variable());

                relationship(relationship, source, target);
                constrainNode(source, from);
                constrainNode(target, to);
            }

            case Return(var distinct, var projections, var modifiers) -> {
                if (distinct) {
                    throw unsupported("RETURN DISTINCT");
                }

                if (!modifiers.isEmpty()) {
                    throw unsupported("ORDER BY, SKIP and LIMIT");
                }

                projections.forEach(this::visit);
            }

            case SimpleProjection(var expression) -> {
                var value = term(expression);
                column(source(expression), value);
            }

            case AliasedProjection(var expression, var alias) ->
                    column(alias.token().value(), term(expression));

            case Leaf(var token) when token.type() == TokenType.MULTIPLY ->
                    patternVariables.forEach(variable ->
                            column(variable, names.get(variable)));

            case Create(var patterns) ->
                    patterns.forEach(this::create);

            case SetClause(var items) ->
                    items.forEach(this::visit);

            case RemoveClause(var items) ->
                    items.forEach(this::visit);

            case Delete(var detach, var targets) ->
                    targets.forEach(target ->
                            goals.add(delete(target, detach)));

            case SetProperty(
                    PropertyAccess(Leaf(var variable), Leaf(var key)),
                    var value
            ) -> {
                var newValue = term(value);

                goals.add(
                        "%s(%s, %s, %s)".formatted(
                                entityPredicate(variable.value(), "set", "prop"),
                                names.get(variable.value()),
                                atom(key.value()),
                                newValue
                        )
                );

                forget(variable.value(), key.value());
            }

            case RemoveProperty(
                    PropertyAccess(Leaf(var variable), Leaf(var key))
            ) -> {
                goals.add(
                        "%s(%s, %s)".formatted(
                                entityPredicate(variable.value(), "remove", "prop"),
                                names.get(variable.value()),
                                atom(key.value())
                        )
                );

                forget(variable.value(), key.value());
            }

            case SetLabels(var variable, var labels) ->
                    labels.forEach(label -> goals.add(
                            "cy_set_node_label(%s, %s)".formatted(
                                    names.get(variable.token().value()),
                                    atom(label.token().value())
                            )
                    ));

            case RemoveLabels(var variable, var labels) ->
                    labels.forEach(label -> goals.add(
                            "cy_remove_node_label(%s, %s)".formatted(
                                    names.get(variable.token().value()),
                                    atom(label.token().value())
                            )
                    ));

            case SetProperty _, RemoveProperty _ ->
                    throw unsupported("nested property access");

            case SetAllProperties _ ->
                    throw unsupported("SET of all the properties");

            case Leaf _, Node _, Relationship _, RelRange _, Property _,
                 PropertyAccess _, Comparison _, Or _, And _, Not _,
                 Arithmetic _, Unary _, NullTest _, FunctionCall _,
                 LabelTest _, Order _, SortItem _, Skip _, Limit _ ->
                    throw new IllegalStateException(
                            "Unexpected node outside an expression: " + node
                    );
        }
    }

    /**
     * Traduce una expresión a un término Prolog.
     * Agrega goals cuando necesita leer propiedades o calcular valores.
     */
    private String term(AstNode expression) {
        return switch (expression) {
            case Leaf(var token) -> switch (token.type()) {
                case IDENTIFIER -> names.get(token.value());
                case NUMBER -> token.value();

                case STRING_LITERAL ->
                        atom(token.value().substring(
                                1,
                                token.value().length() - 1
                        ));

                case TRUE -> "true";
                case FALSE -> "false";
                case NULL -> "null";

                default -> throw new IllegalStateException(
                        "Unexpected token in expression: " + token
                );
            };

            case PropertyAccess(Leaf(var variable), Leaf(var key)) ->
                    property(variable.value(), key.value());

            case PropertyAccess _ ->
                    throw unsupported("nested property access");

            case Arithmetic(var left, var operator, var right) ->
                    calculate(
                            "%s %s %s".formatted(
                                    term(left),
                                    arithmetic(operator),
                                    term(right)
                            )
                    );

            case Unary(var operator, var operand) ->
                    calculate(
                            "%s(%s)".formatted(
                                    operator.token().value(),
                                    term(operand)
                            )
                    );

            case FunctionCall(var name, var arguments)
                    when isType(name, arguments) -> {
                var result = fresh("Type");

                goals.add(
                        "cy_edge_type(%s, %s)".formatted(
                                term(arguments.getFirst()),
                                result
                        )
                );

                yield result;
            }

            case FunctionCall(var name, var _) ->
                    throw unsupported("function " + name.token().value());

            case Comparison _, Or _, And _, Not _, NullTest _, LabelTest _ ->
                    throw unsupported("boolean expressions outside WHERE");

            case Query _, Match _, Where _, Pattern _, Path _,
                 Relationship _, RelRange _, Node _, Property _, Return _,
                 SimpleProjection _, AliasedProjection _, Order _,
                 SortItem _, Skip _, Limit _, UpdatingClause _,
                 SetProperty _, SetAllProperties _, SetLabels _,
                 RemoveProperty _, RemoveLabels _ ->
                    throw new IllegalStateException(
                            "Not an expression: " + expression
                    );
        };
    }

    /**
     * Construye la condición que evaluará cy_where.
     */
    private String condition(AstNode expression) {
        return switch (expression) {
            case And(var left, var right) ->
                    conjunction(condition(left), condition(right));

            case Or(var left, var right) ->
                    "or(%s, %s)".formatted(
                            condition(left),
                            condition(right)
                    );

            case Not(var inner) ->
                    "not(%s)".formatted(condition(inner));

            case Comparison(var left, var operator, var right) ->
                    "%s(%s, %s)".formatted(
                            comparison(operator),
                            term(left),
                            term(right)
                    );

            case NullTest(var inner, var negated) -> {
                var test = "is_null(%s)".formatted(term(inner));
                yield negated ? "not(%s)".formatted(test) : test;
            }

            case LabelTest(Leaf(var variable), var labels) -> {
                var predicate = labelPredicate(variable.value());
                var prolog = names.get(variable.value());

                yield labels.stream()
                        .map(label -> "%s(%s, %s)".formatted(
                                predicate,
                                prolog,
                                atom(label.token().value())
                        ))
                        .reduce(CodeGen::conjunction)
                        .orElseThrow();
            }

            case LabelTest _ ->
                    throw unsupported(
                            "label test on something other than a variable"
                    );

            case Leaf _, PropertyAccess _, FunctionCall _,
                 Arithmetic _, Unary _ ->
                    "cy_equal(%s, true)".formatted(term(expression));

            case Query _, Match _, Where _, Pattern _, Path _,
                 Relationship _, RelRange _, Node _, Property _, Return _,
                 SimpleProjection _, AliasedProjection _, Order _,
                 SortItem _, Skip _, Limit _, UpdatingClause _,
                 SetProperty _, SetAllProperties _, SetLabels _,
                 RemoveProperty _, RemoveLabels _ ->
                    throw new IllegalStateException(
                            "Not a condition: " + expression
                    );
        };
    }

    /**
     * Genera la búsqueda de una relación y sus restricciones.
     */
    private void relationship(
            Relationship relationship,
            String source,
            String target
    ) {
        if (!relationship.range().isEmpty()) {
            goals.add(path(relationship, source, target));
            return;
        }

        var edge = bindPatternVariable(relationship.variable());
        matchRelationships.add(edge);

        goals.add(switch (relationship.direction()) {
            case RIGHT -> edge(edge, source, target);
            case LEFT -> edge(edge, target, source);

            case NONE -> anyOf(List.of(
                    edge(edge, source, target),
                    edge(edge, target, source)
            ));
        });

        if (!relationship.types().isEmpty()) {
            goals.add(anyOf(
                    relationship.types().stream()
                            .map(type -> "cy_edge_type(%s, %s)".formatted(
                                    edge,
                                    atom(type.token().value())
                            ))
                            .toList()
            ));
        }

        relationship.properties().forEach(property ->
                goals.add(propertyGoal(
                        "cy_edge_prop",
                        edge,
                        property.key().token().value(),
                        term(property.value())
                ))
        );
    }

    /**
     * Genera una búsqueda de longitud variable.
     */
    private String path(
            Relationship relationship,
            String source,
            String target
    ) {
        if (!relationship.variable().isEmpty()) {
            throw unsupported("variable on a variable-length relationship");
        }

        var range = (RelRange) relationship.range().getFirst();
        var options = new ArrayList<String>();

        var direction = switch (relationship.direction()) {
            case RIGHT -> "right";
            case LEFT -> "left";
            case NONE -> "none";
        };

        options.add("edge_direction(%s)".formatted(direction));

        options.add("bounds(%s, %s)".formatted(
                range.min().isEmpty()
                        ? "1"
                        : range.min().getFirst().token().value(),

                range.max().isEmpty()
                        ? "infinite"
                        : range.max().getFirst().token().value()
        ));

        if (!relationship.types().isEmpty()) {
            options.add("edge_types(%s)".formatted(
                    list(relationship.types().stream()
                            .map(type -> atom(type.token().value()))
                            .toList())
            ));
        }

        if (!relationship.properties().isEmpty()) {
            options.add(
                    "edge_props(%s)".formatted(
                            pairs(relationship.properties())
                    )
            );
        }

        return "cy_path(%s, %s, _, %s)".formatted(
                source,
                target,
                list(options)
        );
    }

    private void constrainNode(String variable, Pattern pattern) {
        pattern.labels().forEach(label ->
                goals.add("cy_node_label(%s, %s)".formatted(
                        variable,
                        atom(label.token().value())
                ))
        );

        pattern.properties().forEach(property ->
                goals.add(propertyGoal(
                        "cy_node_prop",
                        variable,
                        property.key().token().value(),
                        term(property.value())
                ))
        );
    }

    /**
     * Genera la creación de un nodo o de un patrón nodo-relación-nodo.
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
        var edge = bindPatternVariable(relationship.variable());

        goals.add(
                relationship.direction() == Relationship.Direction.LEFT
                        ? createEdge(target, source, type, properties, edge)
                        : createEdge(source, target, type, properties, edge)
        );
    }

    /**
     * Reutiliza un nodo ya procesado o genera su creación.
     * La tabla semántica no determina si ya se emitió su creación.
     */
    private String createNode(Pattern pattern) {
        var bound = pattern.variable().stream()
                .anyMatch(variable ->
                        names.containsKey(variable.token().value()));

        var variable = bindPatternVariable(pattern.variable());

        if (bound) {
            return variable;
        }

        var labels = list(
                pattern.labels().stream()
                        .map(label -> atom(label.token().value()))
                        .toList()
        );

        var properties = pairs(pattern.properties());

        goals.add(
                "cy_create_node(%s, %s, %s)".formatted(
                        labels,
                        properties,
                        variable
                )
        );

        return variable;
    }

    private String delete(AstNode target, boolean detach) {
        if (!(target instanceof Leaf(var token))
                || token.type() != TokenType.IDENTIFIER) {
            throw unsupported("DELETE of something other than a variable");
        }

        var predicate = switch (typeOf(token.value())) {
            case NODE ->
                    detach ? "cy_detach_delete_node" : "cy_delete_node";

            case RELATIONSHIP ->
                    "cy_delete_edge";

            case ALIAS ->
                    throw unsupported("DELETE of a projection alias");
        };

        return "%s(%s)".formatted(
                predicate,
                names.get(token.value())
        );
    }

    /**
     * Consulta la categoría establecida por el análisis semántico.
     */
    private Type typeOf(String variable) {
        return symbols.lookup(variable).orElseThrow().type();
    }

    /**
     * Nombre del elemento en los predicados del engine.
     */
    private String entityOf(String variable) {
        return switch (typeOf(variable)) {
            case NODE -> "node";
            case RELATIONSHIP -> "edge";

            case ALIAS ->
                    throw unsupported("graph operation on a projection alias");
        };
    }

    /**
     * Construye nombres como cy_set_node_prop o cy_remove_edge_prop.
     */
    private String entityPredicate(
            String variable,
            String operation,
            String attribute
    ) {
        return "cy_%s_%s_%s".formatted(
                operation,
                entityOf(variable),
                attribute
        );
    }

    /**
     * Las etiquetas de nodo y los tipos de relación usan
     * nombres distintos en el engine.
     */
    private String labelPredicate(String variable) {
        return switch (typeOf(variable)) {
            case NODE -> "cy_node_label";
            case RELATIONSHIP -> "cy_edge_type";

            case ALIAS ->
                    throw unsupported("label test on a projection alias");
        };
    }

    /**
     * Asigna un nombre Prolog a una variable de patrón.
     */
    private String bindPatternVariable(List<Leaf> variable) {
        if (variable.isEmpty()) {
            return "__" + ++counter;
        }

        var name = variable.getFirst().token().value();

        patternVariables.add(name);

        return names.computeIfAbsent(
                name,
                _ -> fresh(capitalize(name))
        );
    }

    /**
     * Reutiliza una lectura de propiedad hasta que sea invalidada.
     */
    private String property(String variable, String key) {
        return names.computeIfAbsent(variable + "." + key, _ -> {
            var value = fresh(capitalize(variable) + key);
            var predicate = "cy_%s_prop".formatted(entityOf(variable));

            goals.add(
                    propertyGoal(
                            predicate,
                            names.get(variable),
                            key,
                            value
                    )
            );

            return value;
        });
    }

    private void forget(String variable, String key) {
        names.remove(variable + "." + key);
    }

    private String calculate(String expression) {
        var result = "__" + ++counter;

        goals.add(
                "cy_calculate(%s, %s)".formatted(expression, result)
        );

        return result;
    }

    private String fresh(String hint) {
        var candidate = "__" + hint;

        return taken.add(candidate)
                ? candidate
                : "__" + ++counter;
    }

    /**
     * Impide que dos posiciones de relación de un MATCH
     * se resuelvan al mismo arco.
     */
    private void distinctRelationships() {
        for (int i = 0; i < matchRelationships.size(); i++) {
            for (int j = i + 1; j < matchRelationships.size(); j++) {
                goals.add(
                        "cy_not_equal(%s, %s)".formatted(
                                matchRelationships.get(i),
                                matchRelationships.get(j)
                        )
                );
            }
        }
    }

    private String pairs(List<Property> properties) {
        return list(
                properties.stream()
                        .map(property -> "%s-%s".formatted(
                                atom(property.key().token().value()),
                                term(property.value())
                        ))
                        .toList()
        );
    }

    private void column(String header, String value) {
        headers.add(header);
        columns.add(value);
    }

    private static String propertyGoal(
            String predicate,
            String variable,
            String key,
            String value
    ) {
        return "%s(%s, %s, %s)".formatted(
                predicate,
                variable,
                atom(key),
                value
        );
    }

    private static String edge(String edge, String source, String target) {
        return "cy_edge(%s, %s, %s)".formatted(edge, source, target);
    }

    private static String createEdge(
            String source,
            String target,
            String type,
            String properties,
            String edge
    ) {
        return "cy_create_edge(%s, %s, %s, %s, %s)".formatted(
                source,
                target,
                type,
                properties,
                edge
        );
    }

    private static String conjunction(String left, String right) {
        return "and(%s, %s)".formatted(left, right);
    }

    private static String allOf(List<String> goals) {
        return goals.isEmpty() ? "true" : group(goals, ", ");
    }

    private static String anyOf(List<String> alternatives) {
        return group(alternatives, " ; ");
    }

    private static String group(List<String> goals, String separator) {
        return goals.size() == 1
                ? goals.getFirst()
                : goals.stream().collect(joining(separator, "(", ")"));
    }

    private static boolean isType(Leaf name, List<AstNode> arguments) {
        return name.token().value().equalsIgnoreCase("type")
                && arguments.size() == 1;
    }

    private static String comparison(Leaf operator) {
        return switch (operator.token().type()) {
            case EQUALS -> "cy_equal";
            case NOTEQUALS -> "cy_not_equal";
            case LESS_THAN -> "cy_less_than";
            case GREATER_THAN -> "cy_greater_than";
            case LESS_THAN_OR_EQUAL -> "cy_less_or_equal";
            case GREATER_THAN_OR_EQUAL -> "cy_greater_or_equal";

            default -> throw new IllegalStateException(
                    "Not a comparison operator: " + operator
            );
        };
    }

    private static String arithmetic(Leaf operator) {
        return operator.token().value().equals("%")
                ? "mod"
                : operator.token().value();
    }

    /**
     * Construye el encabezado de una expresión sin alias.
     */
    private static String source(AstNode expression) {
        return switch (expression) {
            case Leaf(var token) ->
                    token.value();

            case PropertyAccess(var target, var key) ->
                    source(target) + "." + source(key);

            case FunctionCall(var name, var arguments) ->
                    source(name)
                            + arguments.stream()
                            .map(CodeGen::source)
                            .collect(joining(", ", "(", ")"));

            case Arithmetic(var left, var operator, var right) ->
                    "%s %s %s".formatted(
                            source(left),
                            source(operator),
                            source(right)
                    );

            case Unary(var operator, var operand) ->
                    source(operator) + source(operand);

            case Query _, Match _, Where _, Pattern _, Path _,
                 Relationship _, RelRange _, Node _, Property _, Return _,
                 SimpleProjection _, AliasedProjection _, Order _,
                 SortItem _, Skip _, Limit _, UpdatingClause _,
                 SetProperty _, SetAllProperties _, SetLabels _,
                 RemoveProperty _, RemoveLabels _, Comparison _,
                 Or _, And _, Not _, NullTest _, LabelTest _ ->
                    throw new IllegalStateException(
                            "No column for: " + expression
                    );
        };
    }

    private static String atom(String text) {
        return "'"
                + text.replace("\\", "\\\\").replace("'", "\\'")
                + "'";
    }

    private static String list(List<String> items) {
        return "[" + String.join(", ", items) + "]";
    }

    private static String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static UnsupportedOperationException unsupported(String feature) {
        return new UnsupportedOperationException(
                "Not supported yet: " + feature
        );
    }
}