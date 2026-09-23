package cr.ac.una.eif400.cyphail.compiler.parser.response;

public record Fail<I, O>(String reason) implements Result<I, O> {

}