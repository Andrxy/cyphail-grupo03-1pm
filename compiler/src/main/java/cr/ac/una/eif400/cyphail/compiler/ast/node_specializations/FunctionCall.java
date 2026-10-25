package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

public record FunctionCall(Leaf name, List<AstNode> arguments) implements AstNode {
}