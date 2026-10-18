package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

public record LabelTest(AstNode expression, List<Leaf> labels) implements AstNode {
}