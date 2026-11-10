package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Cláusula REMOVE dentro del AST.
 *
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @version P2
 * @param items la lista de elementos que se desean eliminar
 */
public record RemoveClause(List<AstNode> items) implements UpdatingClause {
}