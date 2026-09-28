package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Nodo genérico del AST para agrupare elementos
 *
 * @author Andrey Solano
 * @version P1
 * @param items la lista de elementos contenidos en este nodo
 */
public record Node(List<AstNode> items) implements AstNode {
}
