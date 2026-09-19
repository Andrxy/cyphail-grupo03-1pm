package cr.ac.una.eif400.cyphail.compiler.tokenizer;

public interface IToken<T> {
    TokenType type();
    T value();
}
