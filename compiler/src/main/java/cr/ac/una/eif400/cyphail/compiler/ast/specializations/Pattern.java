package cr.ac.una.eif400.cyphail.compiler.ast.specializations;

import cr.ac.una.eif400.cyphail.compiler.ast.AstVisitor;

import java.util.List;

public record Pattern(List<Leaf> variable, List<Leaf> labels, List<Property> properties) implements AstNode {
}
