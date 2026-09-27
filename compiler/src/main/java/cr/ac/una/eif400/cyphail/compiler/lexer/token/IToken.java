package cr.ac.una.eif400.cyphail.compiler.lexer.token;

/**
 * Interfaz que define la estructura básica de un token
 *
 * @author Alejandro Miranda
 * @version P1
 * @param <T> el tipo de valor asociado al token
 */
public interface IToken<T> {

    /**
     * @return el tipo del token según {@link TokenType}
     */
    TokenType type();

    /**
     * @return el valor del token
     */
    T value();
}
