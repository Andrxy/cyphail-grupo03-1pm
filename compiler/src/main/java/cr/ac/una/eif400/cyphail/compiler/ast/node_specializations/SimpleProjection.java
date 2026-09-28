package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Proyección simple sin alias dentro del AST
 * (ej, MATCH (p) RETURN p.name)
 *
 * @author Andrey Solano
 * @version P1
 * @param expression la expresión o nodo proyectado
 */
public record SimpleProjection(AstNode expression) implements AstNode {
}