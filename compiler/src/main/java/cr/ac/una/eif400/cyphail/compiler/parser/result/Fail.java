package cr.ac.una.eif400.cyphail.compiler.parser.result;

/**
 * @author Alejandro Mirando
 * @author Andrey Solano
 * @param <I>
 * @param <T>
 * @param <R>
 */
public record Fail<I, T, R>(R reason) implements Result<I, T, R> {
}