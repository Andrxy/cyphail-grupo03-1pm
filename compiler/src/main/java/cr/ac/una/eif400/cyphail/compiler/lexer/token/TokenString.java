package cr.ac.una.eif400.cyphail.compiler.lexer.token;

/**
 * @author Alejandro Miranda
 * @param type
 * @param value
 */
public record TokenString(TokenType type, String value) implements IToken<String> {}