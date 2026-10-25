package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Cláusula de eliminación ({@code DELETE}) dentro del AST
 *
 * @author Andrey Solano
 * @version P1
 * @param targets la lista de nodos o cosas a eliminar
 */
public record Delete(boolean detach, List<AstNode> targets) implements UpdatingClause {
}
