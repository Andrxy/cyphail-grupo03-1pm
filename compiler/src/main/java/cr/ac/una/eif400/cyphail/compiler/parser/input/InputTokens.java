package cr.ac.una.eif400.cyphail.compiler.parser.input;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;

import java.util.List;

public record InputTokens(List<TokenString> input, int index) implements Input<List<TokenString>>{
}
