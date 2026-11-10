package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Criterio de ordenamiento dentro del AST.
 *
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @version P1
 * @param expression la expresión utilizada para ordenar los resultados
 * @param descending indica si el ordenamiento es descendente
 */
public record SortItem(AstNode expression, boolean descending) implements AstNode {
}