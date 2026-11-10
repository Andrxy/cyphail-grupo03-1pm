package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Expresión aritmética binaria dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param left la expresión del operando izquierdo
 * @param operator la hoja que contiene el operador aritmético
 * @param right la expresión del operando derecho
 */
public record Arithmetic(AstNode left, Leaf operator, AstNode right) implements AstNode {
}