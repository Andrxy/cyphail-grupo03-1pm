package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * "Camino" o relación dentro del AST
 *
 * @author Andrey Solano, Alejandro Miranda
 * @version P2
 * @param from el patrón o nodo de origen del camino
 * @param relationship el nodo que define la relación de conexión
 * @param to el patrón o nodo de destino del camino
 */
public record Path(Pattern from, Relationship relationship, Pattern to) implements AstNode {
}