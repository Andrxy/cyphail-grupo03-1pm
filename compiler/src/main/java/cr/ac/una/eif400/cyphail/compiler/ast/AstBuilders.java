package cr.ac.una.eif400.cyphail.compiler.ast;

import java.util.ArrayList;
import java.util.List;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.Relationship.Direction;

/**
 * Métodos constructores y transformadores para armar
 * los diferentes nodos del Árbol de Sintaxis Abstracta (AST) a partir de las partes analizadas.
 *
 * @author Alejandro Miranda
 * @author Wilson Umaña
 * @version P1
 */
public class AstBuilders {

    /**
     * Construye un nodo de consulta ({@link Query})
     *
     * @param parts la lista de componentes de la consulta
     * @return un nodo {@link AstNode} de tipo {@link Query}
     */
    public static AstNode toQuery(List<AstNode> parts) {
        return new Query(
                itemsAs(parts.get(0), Match.class),
                itemsAs(parts.get(1), UpdatingClause.class),
                itemsAs(parts.get(2), Return.class));
    }

    /**
     * Construye un nodo ({@link Match}).
     *
     * @param parts la lista de partes que conforman la coincidencia y sus filtros
     * @return un nodo {@link AstNode} de tipo {@link Match}
     */
    public static AstNode toMatch(List<AstNode> parts) {
        return new Match(
                itemsAs(parts.get(1), AstNode.class),
                itemsAs(parts.get(2), Where.class));
    }

    /**
     * Construye un nodo({@link Where})
     *
     * @param parts la lista que contiene la expresión condicional
     * @return un nodo {@link AstNode} de tipo {@link Where}
     */
    public static AstNode toWhere(List<AstNode> parts) {
        return new Where(parts.get(1));
    }

    /**
     * Agrupa una lista de ASTs en un nodo contenedor ({@link Node})
     *
     * @param parts la lista de patrones de entrada
     * @return un nodo {@link AstNode} contenedor con los patrones
     */
    public static AstNode toPatterns(List<AstNode> parts) {
        var patterns = new ArrayList<AstNode>();
        patterns.add(parts.get(0));
        patterns.addAll(itemsAs(parts.get(1), AstNode.class));
        return new Node(patterns);
    }

    /**
     * Agrupa un primer elemento y los que le siguen, separados por coma, en un nodo ({@link Node})
     *
     * @param parts la lista que contiene el primer elemento y el nodo con los demás
     * @return un nodo {@link AstNode} contenedor con todos los elementos
     */
    public static AstNode toItems(List<AstNode> parts) {
        var items = new ArrayList<AstNode>();
        items.add(parts.get(0));
        items.addAll(itemsAs(parts.get(1), AstNode.class));
        return new Node(items);
    }

    /**
     * Construye un nodo ({@link Path})
     *
     * @param parts la lista que contiene el origen, la relación y el destino
     * @return un nodo {@link AstNode} de tipo {@link Path}
     */
    public static AstNode toPath(List<AstNode> parts) {
        return new Path(
                (Pattern) parts.get(0),
                (Relationship) parts.get(1),
                (Pattern) parts.get(2));
    }

    /**
     * Agrupa las partes del detalle de una relación en un nodo ({@link Node})
     *
     * @param parts la lista de componentes del detalle, con sus corchetes
     * @return un nodo {@link AstNode} contenedor con la variable, los tipos y las propiedades
     */
    public static AstNode toRelationshipDetail(List<AstNode> parts) {
        return new Node(List.of(parts.get(1), parts.get(2), parts.get(3)));
    }

    /**
     * Agrupa los tipos de una relación en un nodo ({@link Node})
     *
     * @param parts la lista que contiene el primer tipo y los tipos alternativos separados por {@code |}
     * @return un nodo {@link AstNode} contenedor con los tipos
     */
    public static AstNode toRelTypes(List<AstNode> parts) {
        var types = new ArrayList<AstNode>();
        types.add(parts.get(1));
        types.addAll(itemsAs(parts.get(2), AstNode.class));
        return new Node(types);
    }

    /**
     * Construye un nodo ({@link Relationship})
     *
     * @param parts la lista que contiene el símbolo de apertura, el detalle opcional y el símbolo de cierre
     * @param direction el sentido de la relación según las flechas escritas
     * @return un nodo {@link AstNode} de tipo {@link Relationship}
     */
    public static AstNode toRelationship(List<AstNode> parts, Direction direction) {
        var detail = itemsAs(parts.get(1), AstNode.class);
        if (detail.isEmpty()) {
            return new Relationship(List.of(), List.of(), List.of(), List.of(), direction);
        }
        return new Relationship(
                itemsAs(detail.get(0), Leaf.class),
                itemsAs(detail.get(1), Leaf.class),
                List.of(),
                itemsAs(detail.get(2), Property.class),
                direction);
    }

    /**
     * Construye un nodo ({@link Pattern})
     *
     * @param parts la lista de componentes del patrón
     * @return un nodo {@link AstNode} de tipo {@link Pattern}
     */
    public static AstNode toPattern(List<AstNode> parts) {
        return new Pattern(
                itemsAs(parts.get(1), Leaf.class),
                itemsAs(parts.get(2), Leaf.class),
                itemsAs(parts.get(3), Property.class)
        );
    }

    /**
     * Agrupa las propiedades en un nodo ({@link Node})
     *
     * @param parts la lista de propiedades a agrupar
     * @return un nodo {@link AstNode} contenedor de propiedades
     */
    public static AstNode toProperties(List<AstNode> parts) {
        var properties = new ArrayList<AstNode>();
        properties.add(parts.get(0));
        properties.addAll(itemsAs(parts.get(1), Property.class));
        return new Node(properties);
    }

    /**
     * Construye una nodo ({@link Property})
     *
     * @param parts la lista de partes que definen la clave y el valor
     * @return un nodo {@link AstNode} de tipo {@link Property}
     */
    public static AstNode toProperty(List<AstNode> parts) {
        return new Property((Leaf) parts.get(0), parts.get(2));
    }

    /**
     * Construye un nodo ({@link Return})
     *
     * @param parts la lista de elementos a retornar
     * @return un nodo {@link AstNode} de tipo {@link Return}
     */
    public static AstNode toReturn(List<AstNode> parts) {
        var projections = new ArrayList<AstNode>();
        projections.add(parts.get(1));
        projections.addAll(itemsAs(parts.get(2), AstNode.class));
        return new Return(false, projections, List.of());
    }

    /**
     * Construye un nodo({@link AliasedProjection}).
     *
     * @param parts la lista de partes de la proyección
     * @return un nodo {@link AstNode} que representa la proyección
     */
    public static AstNode toProjection(List<AstNode> parts) {
        if (parts.size() == 1) {
            return new SimpleProjection(parts.get(0));
        }
        return new AliasedProjection(parts.get(0), (Leaf) parts.get(2));
    }

    /**
     * Convierte y proyecta los elementos contenidos en un nodo genérico hacia una lista de un tipo específico.
     * <br>
     * <b>Se le pidió a Claude:</b> Crea un método en Java que reciba un AstNode que en realidad es un Node,
     * que adentro tiene una lista items, y que me devuelva esa lista pero convertida al tipo que se indique
     * por parametro
     *
     * @param node el nodo contenedor del cual se extraerán los ítems
     * @param type la clase del tipo de nodo esperado
     * @param <N> el tipo del nodo que extiende de {@link AstNode}
     * @return una lista con los elementos casteados al tipo especificado
     */
    public static <N extends AstNode> List<N> itemsAs(AstNode node, Class<N> type) {
        return ((Node) node).items().stream().map(item -> type.cast(item)).toList();
    }

    /**
     * Construye un nodo  ({@link PropertyAccess})
     *
     * @param parts la lista que contiene la variable y la clave de la propiedad
     * @return un nodo {@link AstNode} de tipo {@link PropertyAccess}
     */
    public static AstNode toPropertyAccess(List<AstNode> parts) {
        return new PropertyAccess(
                (Leaf) parts.get(0),
                (Leaf) parts.get(2)
        );
    }

    /**
     * Construye un nodo ({@link Delete})
     *
     * @param parts la lista de elementos a eliminar
     * @return un nodo {@link AstNode} de tipo {@link Delete}
     */
    public static AstNode toDelete(List<AstNode> parts) {
        var targets = new ArrayList<AstNode>();
        targets.add(parts.get(2));
        targets.addAll(itemsAs(parts.get(3), AstNode.class));

        return new Delete(targets);
    }

    /**
     * Construye una nodo {@link Create}
     *
     * @param parts la lista de patrones de creación
     * @return un nodo {@link AstNode} de tipo {@link Create}
     */
    public static AstNode toCreate(List<AstNode> parts) {
        return new Create(itemsAs(parts.get(1), AstNode.class));
    }

    /**
     * Construye una expresión general, evaluando si se trata de una comparación o una expresión simple
     *
     * @param parts la lista de partes que forman la expresión
     * @return un nodo {@link AstNode} que representa la expresión resultante
     */
    public static AstNode toExpression(List<AstNode> parts) {
        var opt = new ArrayList<AstNode>();
        opt.addAll(itemsAs(parts.get(1), AstNode.class));
        if (opt.isEmpty()) return parts.get(0);

        var comp = new ArrayList<AstNode>();
        comp.addAll(itemsAs(opt.get(0), AstNode.class));
        return new Comparison(parts.get(0), (Leaf) comp.get(0), comp.get(1));
    }

    public static AstNode toSetProperty(List<AstNode> parts) {
        return new SetProperty(
                (PropertyAccess) parts.get(0),
                parts.get(2)
        );
    }

    public static AstNode toSetAllProperties(List<AstNode> parts) {
        return new SetAllProperties(
                (Leaf) parts.get(0),
                parts.get(2)
        );
    }

    public static AstNode toSetLabels(List<AstNode> parts) {
        return new SetLabels(
                (Leaf) parts.get(0),
                itemsAs(parts.get(1), Leaf.class)
        );
    }

    public static AstNode toSet(List<AstNode> parts) {
        var items = new ArrayList<AstNode>();
        items.add(parts.get(1));
        items.addAll(itemsAs(parts.get(2), AstNode.class));

        return new Set(items);
    }

    public static AstNode toRemoveLabels(List<AstNode> parts) {
        return new RemoveLabels(
                (Leaf) parts.get(0),
                itemsAs(parts.get(1), Leaf.class)
        );
    }

    public static AstNode toRemoveProperty(AstNode target) {
        return new RemoveProperty((PropertyAccess) target);
    }

    public static AstNode toRemove(List<AstNode> parts) {
        var items = new ArrayList<AstNode>();
        items.add(parts.get(1));
        items.addAll(itemsAs(parts.get(2), AstNode.class));

        return new Remove(items);
    }

}
