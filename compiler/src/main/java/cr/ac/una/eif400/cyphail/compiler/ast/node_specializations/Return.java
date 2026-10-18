package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Cláusula de retorno ({@code RETURN}) dentro del AST
 *
 * @author Andrey Solano
 * @version P1
 * @param projections la lista de proyecciones o expresiones a retornar
 */
public record Return(boolean distinct, List<AstNode> projections, List<AstNode> modifiers) implements AstNode {
}
