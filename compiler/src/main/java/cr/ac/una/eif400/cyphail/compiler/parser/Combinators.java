package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.ArrayList;
import java.util.List;

public class Combinators {

    @SafeVarargs
    public static <I, T> Parser<I, T> Or(Parser<I, T>... parsers) {
        return (I source) -> {
            if (parsers == null || parsers.length == 0)
                return new Fail<>(null);

            for (Parser<I, T> parser : parsers) {
                var result = parser.parse(source);
                if (result instanceof Ok<I, T> ok)
                    return ok;
            }
            return new Fail<>(null);
        };
    }

    @SafeVarargs
    public static <I, T> Parser<I, List<T>> Sequence(Parser<I, T>... parsers) {
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

    public static <I, T> Parser<I, List<T>> Plus(Parser<I, T> parser) {
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

    public static <I, T> Parser<I, List<T>> Star(Parser<I, T> parser) {
        return (I source) -> {
            var plusResult = Plus(parser).parse(source);

            if (plusResult instanceof Ok<I, List<T>> ok)
                return ok;

            return new Ok<>(source, new ArrayList<>());
        };
    }

    public static <I, T> Parser<I, T> Optional(Parser<I, T> parser, T defaultValue) {
        return (I source) -> {
            var parseResult = parser.parse(source);

            if (parseResult instanceof Ok<I, T> ok)
                return ok;

            return new Ok<>(source, defaultValue);
        };
    }
}
