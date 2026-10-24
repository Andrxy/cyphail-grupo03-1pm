package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Relación entre dos nodos dentro de un patrón del AST, por ejemplo {@code -[r:KNOWS {since: 1999}]->}
 *
 * @author Alejandro Miranda
 * @version P2
 * @param variable la variable de la relación, vacía si no la tiene
 * @param types los tipos de la relación, vacíos si no se indican
 * @param range el rango de longitud de la relación (un {@link RelRange}), vacío si no tiene
 * @param properties las propiedades de la relación
 * @param direction el sentido de la relación según las flechas escritas
 */
public record Relationship(List<Leaf> variable, List<Leaf> types, List<AstNode> range,
                           List<Property> properties, Direction direction) implements AstNode {

    /**
     * Sentido de la relación según las flechas escritas: {@code OUT} para {@code (a)-[]->(b)},
     * {@code IN} para {@code (a)<-[]-(b)} y {@code NONE} para {@code (a)-[]-(b)}
     */
    public enum Direction {
        IN, OUT, NONE
    }
}