package cr.ac.una.eif400.cyphail.compiler.parser.response;

public record Fail<I, T, R>(R reason) implements Response<I, T, R>{

}