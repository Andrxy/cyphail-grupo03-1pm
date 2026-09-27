package cr.ac.una.eif400.cyphail.compiler.parser.result;

/**
 * @author Alejandro Miranda
 * @param <I>
 * @param <T>
 * @param <R>
 */
public sealed interface Result<I, T, R> permits Ok, Fail{
}
