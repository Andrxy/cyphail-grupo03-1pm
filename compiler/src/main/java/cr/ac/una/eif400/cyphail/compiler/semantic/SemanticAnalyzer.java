package cr.ac.una.eif400.cyphail.compiler.semantic;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Un analizador semántico minimalista (no tanto) que ahorita solo verifica el uso de variables
 * definidas con un Set con propósito del P1 <br><br>
 *
 * Prompt: Explíqueme qué es una tabla de símbolos y cómo sabe ella el scope de las cosas para hacer revisiones semánticas
 * <br><br>
 *
 * Prompt: Como solo tengo que revisar la definición de las variables, se puede de momento usar solo un Set?
 * <br><br>
 *
 * Prompt: Proporcióneme un ejemplo de cómo funciona una verificación semántica básica en base a un AST de un lenguaje
 *
 * @author Andrey Solano
 * @version P1
 */
public class SemanticAnalyzer {
    private Set<String> defined = new HashSet<>();
    private List<String> reasons = new ArrayList<>();

    private SemanticAnalyzer() {}

    /**
     * Ejecuta el análisis semántico sobre la raíz del AST y retorna una lista con los posibles errores
     *
     * @param query el nodo raíz del AST ({@link AstNode}) que representa la consulta a evaluar
     * @return una lista de cadenas con los mensajes de error o variables no definidas detectadas
     */
    public static List<String> check(AstNode query) {
        var checker = new SemanticAnalyzer();
        checker.visit(query);

        return checker.reasons;
    }

    /**
     * Recorre recursivamente los nodos del AST para validar declaraciones y el uso correcto de variables por ámbito.
     *
     * @param node el nodo actual del AST a visitar
     */
    private void visit(AstNode node) {
        switch (node) {
            case Query(var matches, var updates, var ret) -> {
                matches.forEach(this::visit);
                updates.forEach(this::visit);
                ret.forEach(this::visit);
            }

            case Match(var patterns, var where) -> {
                patterns.forEach(this::visit);
                where.forEach(this::visit);
            }

            case Pattern(var variable, var _, var properties) -> {
                var name = variable.stream()
                        .map(SemanticAnalyzer::name)
                        .collect(joining());

                properties.forEach(property ->
                        use(property, "properties of (" + name + ")"));

                variable.forEach(leaf -> defined.add(name(leaf)));
            }

            case Where(var condition) -> use(condition, "WHERE");

            case Create(var patterns) -> patterns.forEach(this::visit);

            case Delete(var targets) ->
                    targets.forEach(target -> use(target, "DELETE"));

            case Return(var distinct, var projections, var modifiers) -> {
                projections.forEach(projection -> use(projection, "RETURN"));
                modifiers.forEach(this::visit);
            }

            case Path(var from, var relationship, var to) -> {
                visit(from);
                visit(relationship);
                visit(to);
            }

            case Node(var items) -> items.forEach(this::visit);

            case SetClause(var items) -> items.forEach(this::visit);

            case SetProperty(var target, var value) -> {
                use(target, "SET target");
                use(value, "SET value");
            }

            case SetAllProperties(var variable, var value) -> {
                use(variable, "SET target");
                use(value, "SET value");
            }

            case SetLabels(var variable, var labels) ->
                    use(variable, "SET labels");

            case RemoveClause(var items) -> items.forEach(this::visit);

            case RemoveProperty(var target) ->
                    use(target, "REMOVE property");

            case RemoveLabels(var variable, var labels) ->
                    use(variable, "REMOVE labels");

            case Leaf _, Property _, PropertyAccess _, Comparison _,
                 SimpleProjection _, AliasedProjection _ -> {}

            case Relationship(var variable, var _, var _, var properties, var _) -> {
                var name = variable.stream().map(SemanticAnalyzer::name).collect(joining());
                properties.forEach(property -> use(property, "properties of [" + name + "]"));
                variable.forEach(leaf -> defined.add(name(leaf)));
            }

            case Or _, And _, Not _, Arithmetic _, Unary _, NullTest _,
                 FunctionCall _, LabelTest _ -> {}
        }
    }

    /**
     * Verifica que todas las variables utilizadas dentro de una expresión hayan sido previamente definidas
     *
     * @param expression la expresión o nodo donde se usan las variables
     * @param place una cadena descriptiva que indica el lugar o cláusula donde ocurrió el uso
     */
    private void use(AstNode expression, String place) {
        for (var name : variables(expression))
            if (!defined.contains(name))
                reasons.add("Undefined variable '%s' in %s".formatted(name, place));
    }

    /**
     * Obtiene el valor contenido en una hoja
     *
     * @param leaf la hoja analizada
     * @return el texto del token asociado
     */
    private static String name(Leaf leaf) {
        return leaf.token().value();
    }

    /**
     * Extrae recursivamente la lista de nombres de variables utilizadas dentro de una expresión o nodo del AST
     *
     * @param expression la expresión a evaluar
     * @return una lista con los nombres de las variables encontradas
     */
    private static List<String> variables(AstNode expression) {
        return switch (expression) {
            case Leaf(var token) -> token.type() == TokenType.IDENTIFIER ? List.of(token.value()) : List.of();

            case PropertyAccess(var variable, var _) -> List.of(name(variable));

            case Property(var _, var value) -> variables(value);

            case Comparison(var left, var _, var right) -> Stream.concat(variables(left).stream(), variables(right).stream()).toList();

            case SimpleProjection(var inner) -> variables(inner);

            case AliasedProjection(var inner, var _) -> variables(inner);

            case Node(var items) -> items.stream().flatMap(item -> variables(item).stream()).toList();

            case Query _, Match _, Where _, Create _, Delete _, Return _, Pattern _, Path _ -> List.of();

            case Relationship _ -> List.of();

            case Or _, And _, Not _, Arithmetic _, Unary _, NullTest _, FunctionCall _, LabelTest _ -> List.of();

            case SetClause _, SetProperty _, SetAllProperties _, SetLabels _,
                 RemoveClause _, RemoveProperty _, RemoveLabels _ -> List.of();
        };
    }
}
