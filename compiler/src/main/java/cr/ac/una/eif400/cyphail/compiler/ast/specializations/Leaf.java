package cr.ac.una.eif400.cyphail.compiler.ast.specializations;

import cr.ac.una.eif400.cyphail.compiler.ast.AstVisitor;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;

public record Leaf(TokenString token) implements AstNode {
}
