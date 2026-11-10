package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Cláusula LIMIT dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param expression la expresión que determina el límite de resultados
 */
public record Limit(AstNode expression) implements AstNode{
}
