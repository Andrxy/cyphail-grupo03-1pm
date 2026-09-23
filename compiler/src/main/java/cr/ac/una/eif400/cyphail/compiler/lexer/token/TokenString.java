package cr.ac.una.eif400.cyphail.compiler.lexer.token;

public record TokenString(TokenType type, String value) implements IToken<String> {}