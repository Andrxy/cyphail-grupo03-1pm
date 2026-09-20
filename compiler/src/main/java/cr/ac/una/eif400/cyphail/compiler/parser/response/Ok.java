package cr.ac.una.eif400.cyphail.compiler.parser.response;

public record Ok<I, T, R>(T token, I rest) implements Response<I, T, R> {
}