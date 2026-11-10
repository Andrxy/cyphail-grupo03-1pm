package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Cláusula SKIP para omitir resultados dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param expression la expresión que determina la cantidad de resultados a omitir
 */
public record Skip(AstNode expression) implements AstNode{
}
