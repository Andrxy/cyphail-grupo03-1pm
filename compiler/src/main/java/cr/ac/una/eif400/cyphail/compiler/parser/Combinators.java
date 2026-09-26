package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.ast.ASTNode;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Result;

import java.util.ArrayList;
import java.util.List;
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

    public static Function<List<String>, String> concatStringList = (List<String> list) -> String.join(" & ", list);

    /**
     * Sugerigo por Claude con el fin que el codigo sea mas dry
     * @param concat
     * @param parsers
     * @return
     * @param <I>
     * @param <T>
     * @param <E>
     */
    @SafeVarargs
    public static <I, T, E> IParser<I, T, E> OrFlat(Function<List<E>, E> concat, IParser<I, T, E>...  parsers) {
        return RFlat(Or(parsers), concat);
    }

    public static <I, T, R> IParser<I, T, R> JustGetRight(IParser<I, T, R> left, IParser<I, T, R> right) {
        return TFlat(Sequence(left, right), list -> list.get(1));
    }

    public static <I, T, R> IParser<I, T, R> JustGetLeft(IParser<I, T, R> left, IParser<I, T, R> right) {
        return TFlat(Sequence(left, right), list -> list.get(0));
    }

    public static <I, T, R> IParser<I, T, R> JustGetMiddle(IParser<I, T, R> left, IParser<I, T, R> middle, IParser<I, T, R> right) {
        return TFlat(Sequence(left, middle, right), list -> list.get(1));
    }

}