package cr.ac.una.eif400.cyphail.compiler.ast;

public interface ASTVisitor<R> {
    R visit(ASTNode.Query node);
    R visit(ASTNode.Match node);
    R visit(ASTNode.Where node);
    R visit(ASTNode.Create node);
    R visit(ASTNode.Delete node);
    R visit(ASTNode.Return node);
    R visit(ASTNode.NodePattern node);
    R visit(ASTNode.Property node);
    R visit(ASTNode.PropertyAccess node);
    R visit(ASTNode.Comparison node);
    R visit(ASTNode.SimpleProjection node);
    R visit(ASTNode.AliasedProjection node);
    R visit(ASTNode.Leaf node);
}
