package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.IParser;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Andrey Solano
 * @author Alejandro Miranda
 */
public class Combinators {

    @SafeVarargs
    public static <I, T, R> IParser<I, T, List<R>> Or(IParser<I, T, R>... parsers) {
        return (I source) -> {
            var reasons = new ArrayList<R>();

            for (var p : parsers) {
                switch (p.parse(source)) {
                    case Ok(I rest, T value) -> {
                        return new Ok<>(rest, value);
                    }
                    case Fail(R reason) -> {
                        reasons.add(reason);
                    }
                }
            }

            return new Fail<>(reasons);
        };
    }

    @SafeVarargs
    public static <I, T, R> IParser<I, List<T>, R> Sequence(IParser<I, T, R>... parsers) {
        return (I source) -> {
            var allValues = new ArrayList<T>();

            for (var p : parsers) {
                switch (p.parse(source)) {
                    case Ok(I rest, T value) -> {
                        allValues.add(value);
                        source = rest;
                    }
                    case Fail(R reason) -> {
                        return new Fail<>(reason);
                    }
                }
            }

            return new Ok<>(source, allValues);
        };
    }

    public static <I, T, R> IParser<I, List<T>, R> Star(IParser<I, T, R> parser){
        return (I source) -> {
            var allValues = new ArrayList<T>();

            var result = parser.parse(source);
            while (result instanceof Ok<I, T, R>(I rest, T value)) {
                allValues.add(value);
                source = rest;
                result = parser.parse(source);
            }

            return new Ok<>(source, allValues);
        };
    }

    public static <I, T, R> IParser<I, List<T>, R> Plus(IParser<I, T, R> parser) {
        return (I source) -> {
            var allValues = new ArrayList<T>();

            switch (parser.parse(source)) {
                case Fail<I, T, R>(R reason) -> {
                    return new Fail<>(reason);
                }
                case Ok<I, T, R>(I rest, T value) -> {
                    allValues.add(value);
                    source = rest;
                }
            }

            var result = parser.parse(source);
            while (result instanceof Ok<I, T, R>(I rest, T value)) {
                allValues.add(value);
                source = rest;
                result = parser.parse(source);
            }

            return new Ok<>(source, allValues);
        };
    }

    public static <I, T, R> IParser<I, List<T>, R> Optional(IParser<I, T, R> parser) {
        return (I source) -> switch (parser.parse(source)) {
            case Ok(I rest, T value) -> new Ok<>(rest, List.of(value));
            case Fail(R _) -> new Ok<>(source, List.of());
        };
    }

    public static <I, T, R> IParser<I, T, R> TFlat(IParser<I, List<T>, R> parser, Function<List<T>, T> mapper) {
        return (I source) -> switch (parser.parse(source)) {
            case Ok(I rest, List<T> value) -> new Ok<>(rest, mapper.apply(value));
            case Fail(R reason) -> new Fail<>(reason);
        };
    }

    public static <I, T, R> IParser<I, T, R> RFlat(IParser<I, T, List<R>> parser, Function<List<R>, R> concat) {
        return (I source) -> switch (parser.parse(source)) {
            case Ok(I rest, T value) -> new Ok<>(rest, value);
            case Fail(List<R> reason) -> new Fail<>(concat.apply(reason));
        };
    }
}