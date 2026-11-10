package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Negación lógica NOT dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param expression la expresión lógica que se desea negar
 */
public record Not(AstNode expression) implements AstNode {
}