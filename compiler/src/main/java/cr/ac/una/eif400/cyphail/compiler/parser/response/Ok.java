package cr.ac.una.eif400.cyphail.compiler.parser.response;

public record Ok<I, O>(I value, O rest) implements Result<I, O> {
}