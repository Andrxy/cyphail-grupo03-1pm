package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Verificación de valores nulos dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param expression la expresión que se desea verificar
 * @param negated indica si se verifica IS NOT NULL en lugar de IS NULL
 */
public record NullTest(AstNode expression, boolean negated) implements AstNode {
}