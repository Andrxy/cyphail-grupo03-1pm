package cr.ac.una.eif400.cyphail.compiler.tokenizer;

public record TokenString(TokenType type, String value) implements IToken<String> {
}
