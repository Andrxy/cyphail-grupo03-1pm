package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Cláusula SET para actualizar propiedades dentro del AST.
 *
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @version P2
 * @param items la lista de asignaciones o actualizaciones de propiedades
 */
public record SetClause(List<AstNode> items) implements UpdatingClause {
}