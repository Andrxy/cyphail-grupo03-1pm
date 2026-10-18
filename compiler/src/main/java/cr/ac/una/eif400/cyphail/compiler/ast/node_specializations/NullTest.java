package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

public record NullTest(AstNode expression, boolean negated) implements AstNode {
}