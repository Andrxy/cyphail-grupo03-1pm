package cr.ac.una.eif400.cyphail.compiler.ast.specializations;

import cr.ac.una.eif400.cyphail.compiler.ast.AstVisitor;

import java.util.List;

public record Match(List<AstNode> patterns, List<Where> where) implements AstNode {
}
