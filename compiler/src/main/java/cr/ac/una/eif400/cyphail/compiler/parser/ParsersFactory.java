package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.parser.input.Input;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ParsersFactory {
    @SafeVarargs
    static <I, T, R> Parser<I, T, R> Or(Parser<I, T, R>... parsers) {
        Parser<I, T, R> p = (I source) -> {
            if (parsers == null || parsers.length == 0) {
                return new Fail<>(null);
            }

            return Stream.of(parsers)
                    .map(parser -> parser.parse(source))
                    .filter(result -> result instanceof Ok)
                    .findFirst()
                    .orElseGet(() -> new Fail<>(null));
        };

        return p;
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

//    static <I, T, R> Parser<I, T, R> Optional(Parser<I, T, R> parser) {
//        Parser<I, T, R> p = (I source) -> {
//            var parseResult = parser.parse(source);
//
//            if (parseResult instanceof Ok)
//                return parseResult;
//
//            return new Ok<>(null, source);
//        };
//
//        return p;
//    }


    // Plus(Id)   ->     List<Tokens>

    // Id +
    // Plus(Id)

//    function Star(parser, max: number = Infinity) {
//        function parse(source: InputString, it: number = 0, tokens: Token[] = []) {
//            if (it === max)
//                return Ok(tokens, source)
//
//		const {input, index} = source
//		const result = parser(source)
//            if (isFail(result))
//                return Ok(tokens, source)
//
//		const {token, rest} = result
//
//            return parse(InputString(input, rest.index), it + 1, [...tokens, token])
//        }
//
//        return parse
//    }
}
