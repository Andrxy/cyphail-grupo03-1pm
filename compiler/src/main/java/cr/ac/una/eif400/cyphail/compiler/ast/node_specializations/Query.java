package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Nodo raíz del AST que representa la consulta
 *
 * @author Andrey Solano
 * @version P1
 * @param matches la lista de cláusulas de búsqueda ({@code MATCH})
 * @param updates la lista de operaciones de actualización o mutación del grafo
 * @param ret la lista de cláusulas de retorno o proyección ({@code RETURN})
 */
public record Query(List<Match> matches, List<AstNode> updates, List<Return> ret) implements AstNode {
}