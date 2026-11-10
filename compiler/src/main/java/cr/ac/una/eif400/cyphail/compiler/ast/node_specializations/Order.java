package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Cláusula ORDER BY dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param items la lista de criterios de ordenamiento
 */
public record Order(List<SortItem> items) implements AstNode{
}
