package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

public record SortItem(AstNode expression, boolean descending) implements AstNode {
}