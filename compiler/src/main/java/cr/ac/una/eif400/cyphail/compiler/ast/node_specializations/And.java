package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Conjunción lógica AND dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param left la expresión del operando izquierdo
 * @param right la expresión del operando derecho
 */
public record And(AstNode left, AstNode right) implements AstNode {
}