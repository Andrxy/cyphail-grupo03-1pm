package cr.ac.una.eif400.cyphail.compiler.ast;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;

import java.util.List;

public sealed interface ASTNode {
    record Leaf(TokenString token) implements ASTNode {}
    record Node(List<ASTNode> items) implements ASTNode {}

    record Query(List<Match> matches, List<ASTNode> updates, List<Return> ret) implements ASTNode {}
    record Match(List<NodePattern> patterns, List<Where> where) implements ASTNode {}
    record Where(ASTNode condition) implements ASTNode {}
    record Create(List<NodePattern> patterns) implements ASTNode {}
    record Delete(List<ASTNode> targets) implements ASTNode {}
    record Return(List<ASTNode> projections) implements ASTNode {}

    record NodePattern(List<Leaf> variable, List<Leaf> labels, List<Property> properties) implements ASTNode {}
    record Property(Leaf key, ASTNode value) implements ASTNode {}
    record PropertyAccess(Leaf variable, Leaf key) implements ASTNode {}
    record Comparison(ASTNode left, Leaf operator, ASTNode right) implements ASTNode {}

    record SimpleProjection(ASTNode expression) implements ASTNode {}
    record AliasedProjection(ASTNode expression, Leaf alias) implements ASTNode {}
}
