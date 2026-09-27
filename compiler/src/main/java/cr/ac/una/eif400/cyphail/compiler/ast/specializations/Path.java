package cr.ac.una.eif400.cyphail.compiler.ast.specializations;

import cr.ac.una.eif400.cyphail.compiler.ast.AstVisitor;

public record Path(Pattern from, AstNode relationship, Pattern to) implements AstNode {
}