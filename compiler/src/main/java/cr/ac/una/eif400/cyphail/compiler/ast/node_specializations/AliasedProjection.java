package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Proyección con alias dentro del AST
 *
 * @author Andrey Solano
 * @version P1
 * @param expression la expresión evaluada o proyectada
 * @param alias la hoja que contiene el nombre del alias asignado
 */
public record AliasedProjection(AstNode expression, Leaf alias) implements AstNode {
}