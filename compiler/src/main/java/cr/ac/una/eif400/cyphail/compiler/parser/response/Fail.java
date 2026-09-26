package cr.ac.una.eif400.cyphail.compiler.parser.response;

/**
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @param reason
 * @param <I>
 * @param <T>
 * @param <R>
 */
public record Fail<I, T, R>(R reason) implements Result<I, T, R> {

}