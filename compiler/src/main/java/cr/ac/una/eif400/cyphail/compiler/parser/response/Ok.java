package cr.ac.una.eif400.cyphail.compiler.parser.response;

/**
 * @author Alejandro Mirando
 * @author Andrey Solano
 * @param rest
 * @param value
 * @param <I>
 * @param <T>
 * @param <R>
 */
public record Ok<I, T, R>(I rest, T value) implements Result<I, T, R> {
}