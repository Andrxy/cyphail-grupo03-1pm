package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

public record SetProperty(PropertyAccess target, AstNode value) implements AstNode {
}
