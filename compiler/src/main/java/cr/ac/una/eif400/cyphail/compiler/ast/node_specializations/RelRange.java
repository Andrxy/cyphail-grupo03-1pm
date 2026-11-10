package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Rango de longitud de una relación dentro del AST.
 *
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @version P2
 * @param min la lista de hojas que representan el límite mínimo
 * @param max la lista de hojas que representan el límite máximo
 */
public record RelRange(List<Leaf> min, List<Leaf> max) implements AstNode {
}
