package cr.ac.una.eif400.cyphail.compiler.ast;

import java.util.ArrayList;
import java.util.List;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;

public class AstBuilders {

    public static AstNode toQuery(List<AstNode> parts) {
        return new Query(
                itemsAs(parts.get(0), Match.class),
                itemsAs(parts.get(1), AstNode.class),
                itemsAs(parts.get(2), Return.class));
    }

    public static AstNode toMatch(List<AstNode> parts) {
        return new Match(
                itemsAs(parts.get(1), AstNode.class),
                itemsAs(parts.get(2), Where.class));
    }

    public static AstNode toWhere(List<AstNode> parts) {
        return new Where(parts.get(1));
    }

    public static AstNode toPatterns(List<AstNode> parts) {
        var patterns = new ArrayList<AstNode>();
        patterns.add(parts.get(0));
        patterns.addAll(itemsAs(parts.get(1), AstNode.class));
        return new Node(patterns);
    }

    public static AstNode toPath(List<AstNode> parts) {
        return new Path(
                (Pattern) parts.get(0),
                parts.get(1),
                (Pattern) parts.get(2));
    }

    public static AstNode toPattern(List<AstNode> parts) {
        return new Pattern(
                itemsAs(parts.get(1), Leaf.class),
                itemsAs(parts.get(2), Leaf.class),
                itemsAs(parts.get(3), Property.class)
        );
    }

    public static AstNode toProperties(List<AstNode> parts) {
        var properties = new ArrayList<AstNode>();
        properties.add(parts.get(0));
        properties.addAll(itemsAs(parts.get(1), Property.class));
        return new Node(properties);
    }

    public static AstNode toProperty(List<AstNode> parts) {
        return new Property((Leaf) parts.get(0), parts.get(2));
    }

    public static AstNode toReturn(List<AstNode> parts) {
        var projections = new ArrayList<AstNode>();
        projections.add(parts.get(1));
        projections.addAll(itemsAs(parts.get(2), AstNode.class));
        return new Return(projections);
    }

    public static AstNode toProjection(List<AstNode> parts) {
        if (parts.size() == 1) {
            return new SimpleProjection(parts.get(0));
        }
        return new AliasedProjection(parts.get(0), (Leaf) parts.get(2));
    }

    public static <N extends AstNode> List<N> itemsAs(AstNode node, Class<N> type) {
        return ((Node) node).items().stream().map(item -> type.cast(item)).toList();
    }

    public static AstNode toPropertyAccess(List<AstNode> parts) {
        return new PropertyAccess(
                (Leaf) parts.get(0),
                (Leaf) parts.get(2)
        );
    }

    public static AstNode toDelete(List<AstNode> parts) {
        var targets = new ArrayList<AstNode>();
        targets.add(parts.get(2));
        targets.addAll(itemsAs(parts.get(3), AstNode.class));

        return new Delete(targets);
    }

    public static AstNode toCreate(List<AstNode> parts) {
        return new Create(itemsAs(parts.get(1), Pattern.class));
    }

    public static AstNode toExpression(List<AstNode> parts) {
        var opt = new ArrayList<AstNode>();
        opt.addAll(itemsAs(parts.get(1), AstNode.class));
        if (opt.isEmpty()) return parts.get(0);

        var comp = new ArrayList<AstNode>();
        comp.addAll(itemsAs(opt.get(0), AstNode.class));
        return new Comparison(parts.get(0), (Leaf) comp.get(0), comp.get(1));
    }
}
