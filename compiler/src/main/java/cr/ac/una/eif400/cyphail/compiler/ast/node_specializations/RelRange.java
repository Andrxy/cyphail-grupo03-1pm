package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

public record RelRange(List<Leaf> min, List<Leaf> max) implements AstNode {
}
