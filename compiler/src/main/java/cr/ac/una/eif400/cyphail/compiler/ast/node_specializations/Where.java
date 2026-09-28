package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Cláusula de condición ({@code WHERE}) dentro del AST.
 *
 * @author Andrey Solano
 * @version P1
 * @param condition el nodo o expresión lógica que evalúa la condición de filtrado
 */
public record Where(AstNode condition) implements AstNode {
}