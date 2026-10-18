package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

public record Arithmetic(AstNode left, Leaf operator, AstNode right) implements AstNode {
}