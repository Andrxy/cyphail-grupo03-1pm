package cr.ac.una.eif400.cyphail.compiler.ast.specializations;

import cr.ac.una.eif400.cyphail.compiler.ast.AstVisitor;

import java.util.List;

public record Node(List<AstNode> items) implements AstNode {
}
