package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ParsersFactory {
    @SafeVarargs
    public static <I, T, R> Parser<I, T, R> Or(Parser<I, T, R>... parsers) {
        return (I source) -> {

            if (parsers == null || parsers.length == 0)
                return new Fail<>(null);

            for (Parser<I, T, R> parser : parsers) {

                var result = parser.parse(source);

                if (result instanceof Ok)
                    return result;
            }

            return new Fail<>(null);
        };
    }

    @SafeVarargs
    public static <I, T, R> Parser <I, List<T>, R> Sequence(Parser<I, T, R>... parsers){

        Parser<I, List<T>, R> parser = (I source) -> {
            List<T> results = new ArrayList<>();
            for (var p : parsers) {
                var result = p.parse(source);
                switch ( result ){
                    case Fail(R reason) -> {
                        return new Fail<I, List<T>, R>(reason);
                    }
                    case Ok(T token, I rest) -> {
                        results.add( token );
                        source = rest;
                    }
                };
            }

            return new Ok<I, List<T>, R>(results, source);

        };

        return parser;
    }

    public static <I, T, R> Parser<I, List<T>, R> Plus(Parser<I, T, R> parser) {

        Parser<I, List<T>, R> p = (I source) -> {
            if (parser == null)
                return new Fail<> (null);

            List<T> tokens = new ArrayList<>();

            var isOk = true;
            while (isOk) {
                var result = parser.parse(source);
                switch ( result ){
                    case Fail(R reason) -> {
                        isOk = false;
                        if(tokens.isEmpty())
                            return new Fail<>(reason);
                    }
                    case Ok(T token, I rest) -> {
                        tokens.add( token );
                        source = rest;
                    }
                };
            }

            return new Ok<>(tokens, source);
        };

        return p;
    }

    public static <I, T, R> Parser<I, List<T>, R> Star(Parser<I, T, R> parser) {
        Parser<I, List<T>, R> p = (I source) -> {
            var plusResult = Plus(parser).parse(source);

            if (plusResult instanceof Ok)
                return plusResult;

            return new Ok<>(new ArrayList<>(), source);
        };

        return p;
    }

    public static <I, T, R> Parser<I, T, R> Optional(Parser<I, T, R> parser, T defaultValue) {
        Parser<I, T, R> p = (I source) -> {
            var parseResult = parser.parse(source);

            if (parseResult instanceof Ok)
                return parseResult;

            return new Ok<>(defaultValue, source);
        };

        return p;
    }
}
