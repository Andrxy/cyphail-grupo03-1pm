package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Expresión de comparación dentro del AST
 *
 * @author Andrey Solano
 * @version P1
 * @param left la expresión o nodo del lado izquierdo
 * @param operator la hoja que representa el operador de comparación utilizado
 * @param right la expresión o nodo del lado derecho
 */
public record Comparison(AstNode left, Leaf operator, AstNode right) implements AstNode {
}