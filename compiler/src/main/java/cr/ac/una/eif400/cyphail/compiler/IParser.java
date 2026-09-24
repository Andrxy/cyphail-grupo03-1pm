package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.cyphail.compiler.parser.response.Result;

@FunctionalInterface
public interface IParser<I, O> {
    Result<I, O> parse(I source);
}
