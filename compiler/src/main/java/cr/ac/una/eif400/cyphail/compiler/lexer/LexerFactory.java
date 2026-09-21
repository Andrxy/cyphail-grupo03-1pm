package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.Set;
import java.util.regex.Pattern;

public class LexerFactory {
    public static Lexer Identifier() {
        var re_id = Pattern.compile("\\s*(?<token>[a-zA-Z_]\\w*)");

        Lexer lexer = (InputString source) -> {
            var index = source.index();
            var input = source.input();

            var matcher = re_id.matcher(source.input());
            matcher.region(index, input.length());

            if(!matcher.lookingAt()) {
                return new Fail<>("No identifier could be found");
            }

            var token = matcher.group("token");
            return new Ok<>(new TokenString(TokenType.ID, token), new InputString(input, matcher.end()));
        };

        return lexer;
    }

    public static Lexer Integer() {
        var re_num = Pattern.compile("\\s*(?<token>[+-]?\\d+)");

        Lexer lexer = (InputString source) -> {
            var input = source.input();
            var index = source.index();

            var matcher = re_num.matcher(input);
            matcher.region(index, input.length());

            if(!matcher.lookingAt())
                return new Fail<>("No number could be match");

            var token = new TokenString(TokenType.Num, matcher.group("token"));
            var newSource = new InputString(input, matcher.end());
            return new Ok<>(token, newSource);
        };
        return lexer;
    }

    public static Lexer Keywords() {
        var re_keywords = Pattern.compile("\\s*(?<token>[a-z]+)", Pattern.CASE_INSENSITIVE);

        Set<String> cypherKeywords = Set.of(
                "match", "where", "create", "set", "detach", "delete", "remove", "return",
                "distinct", "as", "order", "by", "ascending", "descending", "skip", "limit",
                "or", "and", "not", "is", "null", "true", "false"
        );

        Lexer lexer = (InputString source) -> {
            var index = source.index();
            var input = source.input();

            var matcher = re_keywords.matcher(input);
            matcher.region(index, input.length());

            if (!matcher.lookingAt()) {
                return new Fail<>("No keyword could get matched"); // aqui se podria tirar columna
            }

            var token = matcher.group("token").toLowerCase();

            if (!cypherKeywords.contains(token)) {
                return new Fail<>("No keyword could get matched");
            }

            return new Ok(new TokenString(TokenType.KEYWORD, token), new InputString(input, matcher.end()));
        };

        return lexer;
    }

    public static Lexer Operators() {
        var re_operators = Pattern.compile("\\s*(?<token><=|>=|<>|[=<>+*/\\-])");

        Lexer lexer = (InputString source) -> {
            var input = source.input();
            var index = source.index();

            var matcher = re_operators.matcher(input);
            matcher.region(index, input.length());

            if (!matcher.lookingAt())
                return new Fail<>("No operator could get matched");

            var token = matcher.group("token");

            return new Ok<>(new TokenString(TokenType.OPERATOR, token), new InputString(input, matcher.end()));
        };

        return lexer;
    }

    public static Lexer Literal(String literalInput) {
        var re_operators = Pattern.compile("\\s*(?<token>\\w+)");

        Lexer lexer = (InputString source) -> {
            var input = source.input();
            var index = source.index();

            var matcher = re_operators.matcher(input);
            matcher.region(index, input.length());

            if (!matcher.lookingAt())
                return new Fail<>("No literal could get matched");

            var token = matcher.group("token");

            if(!token.equals(literalInput))
                return new Fail<>("No literal could get matched");

            return new Ok<>(new TokenString(TokenType.OPERATOR, token), new InputString(input, matcher.end()));
        };

        return lexer;
    }

    public static Lexer Double() {
        var re_double = Pattern.compile("\\s*(?<token>[+-]?\\d+\\.\\d+)");

        Lexer lexer = (InputString source) -> {
            var input = source.input();
            var index = source.index();

            var matcher = re_double.matcher(input);
            matcher.region(0, input.length());

            if (!matcher.lookingAt())
                return new Fail("No double could get matched");

            var token = matcher.group("token");
            return new Ok(new TokenString(TokenType.Num, token), new InputString(input, matcher.end()));
        };

        return lexer;
    }


    /*
        // .tree {query}
        if (keyword == match)

        nodePattern: "(" variable labels? ")";
        variable: Identifier;
        labels: (":" variable) +;
     */

}
