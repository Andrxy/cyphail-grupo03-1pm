package cr.ac.una.eif400.cyphail.compiler.parser.response;

public record Ok<I, O>(I rest, O value) implements Result<I, O> {
}