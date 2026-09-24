package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.IParser;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Combinators {

    @SafeVarargs
    public static <I, T> IParser<I, T> Or(IParser<I, T>... parsers) {
        return (I source) -> {
            if (parsers == null || parsers.length == 0)
                return new Fail<>(null);

            for (IParser<I, T> parser : parsers) {
                var result = parser.parse(source);
                if (result instanceof Ok<I, T> ok)
                    return ok;
            }
            return new Fail<>(null);
        };
    }

    @SafeVarargs
    public static <I, T> IParser<I, List<T>> Sequence(IParser<I, T>... parsers) {
        return (I source) -> {
            List<T> results = new ArrayList<>();
            I currentSource = source;

            for (var p : parsers) {
                var result = p.parse(currentSource);

                if (result instanceof Fail<I, T> fail) {
                    return new Fail<>(fail.reason());
                } else if (result instanceof Ok<I, T> ok) {
                    // ok.rest() es el Input restante (I), ok.value() es el Token obtenido (T)
                    currentSource = ok.rest();
                    results.add(ok.value());
                }
            }
            // Calza con Ok<I, List<T>> -> (I rest, List<T> value)
            return new Ok<>(currentSource, results);
        };
    }

    public static <I, T> IParser<I, List<T>> Plus(IParser<I, T> parser) {
        return (I source) -> {
            if (parser == null)
                return new Fail<>(null);

            List<T> tokens = new ArrayList<>();
            I currentSource = source;
            String lastError = null;

            while (true) {
                var result = parser.parse(currentSource);

                if (result instanceof Fail<I, T> fail) {
                    lastError = fail.reason();
                    break;
                } else if (result instanceof Ok<I, T> ok) {
                    // ok.rest() es el Input restante (I), ok.value() es el Token obtenido (T)
                    currentSource = ok.rest();
                    tokens.add(ok.value());
                }
            }

            if (tokens.isEmpty()) {
                return new Fail<>(lastError);
            }
            // Calza con Ok<I, List<T>> -> (I rest, List<T> value)
            return new Ok<>(currentSource, tokens);
        };
    }

    public static <I, T> IParser<I, List<T>> Star(IParser<I, T> parser) {
        return (I source) -> {
            var plusResult = Plus(parser).parse(source);

            if (plusResult instanceof Ok<I, List<T>> ok)
                return ok;

            return new Ok<>(source, new ArrayList<>());
        };
    }

    static <I, T> IParser<I, Optional<T>> Optional(IParser<I, T> parser) {
        return (I source) -> switch (parser.parse(source)) {
            case Ok(I rest, T token) -> new Ok<>(rest, Optional.of(token));
            case Fail(String error) -> new Ok<>(source, Optional.empty());
        };
    }
}
