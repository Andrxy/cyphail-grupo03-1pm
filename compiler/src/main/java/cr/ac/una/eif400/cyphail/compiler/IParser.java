package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.cyphail.compiler.parser.response.Result;

/**
 * @author Andrey Solano
 * @param <I> input
 * @param <T> return value
 * @param <R> reasons
 */
@FunctionalInterface
public interface IParser<I, T, R> {
    Result<I, T, R> parse(I source);
}