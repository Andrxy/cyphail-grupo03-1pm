package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

public record RemoveLabels (Leaf variable, List<Leaf> labels) implements AstNode{
}
