package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.parser.response.Response;

public interface Parser <I, T, R>{
    Response<I, T, R> parse(I source);
}
