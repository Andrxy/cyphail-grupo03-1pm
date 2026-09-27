package cr.ac.una.eif400.cyphail.compiler.ast;

import cr.ac.una.eif400.cyphail.compiler.ast.specializations.*;

public interface AstVisitor<R> {
    R visit(Query node);
    R visit(Match node);
    R visit(Where node);
    R visit(Create node);
    R visit(Delete node);
    R visit(Return node);
    R visit(Path node);
    R visit(Pattern node);
    R visit(Property node);
    R visit(PropertyAccess node);
    R visit(Comparison node);
    R visit(SimpleProjection node);
    R visit(AliasedProjection node);
    R visit(Leaf node);
    R visit(Node node);
}
