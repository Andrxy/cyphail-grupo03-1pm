package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.parser.response.Result;

@FunctionalInterface
public interface Parser<I, O> {
    Result<I, O> parse(I source);
}
