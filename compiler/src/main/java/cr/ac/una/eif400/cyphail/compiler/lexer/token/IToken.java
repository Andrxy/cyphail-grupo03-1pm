package cr.ac.una.eif400.cyphail.compiler.lexer.token;

public interface IToken<T> {
    TokenType type();
    T value();
}
