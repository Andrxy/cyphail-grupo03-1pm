package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Expresión unaria dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param operator la hoja que contiene el operador unario
 * @param expression la expresión sobre la que se aplica el operador
 */
public record Unary(Leaf operator, AstNode expression) implements AstNode {
}