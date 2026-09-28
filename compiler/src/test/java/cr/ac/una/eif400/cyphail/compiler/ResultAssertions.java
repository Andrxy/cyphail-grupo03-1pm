package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;

import static org.junit.jupiter.api.Assertions.fail;

public final class ResultAssertions {

    private ResultAssertions() {
    }

    public static <I, T, R> Ok<I, T, R> assertOk(Result<I, T, R> result) {
        if (result instanceof Ok<I, T, R> ok) {
            return ok;
        }
        return fail("Se esperaba Ok pero se obtuvo " + result);
    }

    public static <I, T, R> Fail<I, T, R> assertFail(Result<I, T, R> result) {
        if (result instanceof Fail<I, T, R> failure) {
            return failure;
        }
        return fail("Se esperaba Fail pero se obtuvo " + result);
    }
}
