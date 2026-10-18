package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

public record Unary(Leaf operator, AstNode expression) implements AstNode {
}