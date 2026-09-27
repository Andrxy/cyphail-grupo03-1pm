package cr.ac.una.eif400.cyphail.compiler.ast.specializations;

import cr.ac.una.eif400.cyphail.compiler.ast.AstVisitor;

public record Where(AstNode condition) implements AstNode {
}