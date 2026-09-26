package cr.ac.una.eif400.cyphail.compiler.lexer.token;

/**
 * @author Alejandro Miranda
 * @param <T>
 */
public interface IToken<T> {
    TokenType type();
    T value();
}
