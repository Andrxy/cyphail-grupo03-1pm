package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Disyunción lógica OR dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param left la expresión del operando izquierdo
 * @param right la expresión del operando derecho
 */
public record Or(AstNode left, AstNode right) implements AstNode {
}