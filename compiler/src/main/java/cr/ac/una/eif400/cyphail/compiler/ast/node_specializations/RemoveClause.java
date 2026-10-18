package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

public record RemoveClause(List<AstNode> items) implements AstNode {
}