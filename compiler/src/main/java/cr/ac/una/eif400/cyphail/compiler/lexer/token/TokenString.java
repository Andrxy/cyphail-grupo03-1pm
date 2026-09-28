package cr.ac.una.eif400.cyphail.compiler.lexer.token;

/**
 * Representa un token cuyo valor está almacenado en una string
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @version P1
 * @param type el tipo del token
 * @param value el valor en texto del token
 */
public record TokenString(TokenType type, String value) implements IToken<String> {}