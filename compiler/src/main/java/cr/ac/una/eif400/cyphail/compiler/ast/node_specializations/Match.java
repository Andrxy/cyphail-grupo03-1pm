package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Cláusula de coincidencia ({@code MATCH}) en el AST,
 *
 * @author Andrey Solano
 * @version P1
 * @param patterns la lista de patrones o elementos a buscar
 * @param where la lista opcional de condiciones de filtrado
 */
public record Match(List<AstNode> patterns, List<Where> where) implements AstNode {
}
