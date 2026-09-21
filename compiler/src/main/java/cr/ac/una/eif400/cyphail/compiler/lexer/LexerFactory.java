package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.Map;
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
            return new Ok<>(new TokenString(TokenType.IDENTIFIER, token), new InputString(input, matcher.end()));
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

            var token = new TokenString(TokenType.INTEGER_LITERAL, matcher.group("token"));
            var newSource = new InputString(input, matcher.end());
            return new Ok<>(token, newSource);
        };
        return lexer;
    }

    public static Lexer Keywords() {
        var re_keywords = Pattern.compile("\\s*(?<token>[a-z]+)", Pattern.CASE_INSENSITIVE);

        Map<String, TokenType> keywords = Map.ofEntries(
                Map.entry("match", TokenType.MATCH),
                Map.entry("where", TokenType.WHERE),
                Map.entry("create", TokenType.CREATE),
                Map.entry("set", TokenType.SET),
                Map.entry("detach", TokenType.DETACH),
                Map.entry("delete", TokenType.DELETE),
                Map.entry("remove", TokenType.REMOVE),
                Map.entry("return", TokenType.RETURN),
                Map.entry("distinct", TokenType.DISTINCT),
                Map.entry("as", TokenType.AS),
                Map.entry("order", TokenType.ORDER),
                Map.entry("by", TokenType.BY),
                Map.entry("ascending", TokenType.ASCENDING),
                Map.entry("descending", TokenType.DESCENDING),
                Map.entry("skip", TokenType.SKIP),
                Map.entry("limit", TokenType.LIMIT),
                Map.entry("or", TokenType.OR),
                Map.entry("and", TokenType.AND),
                Map.entry("not", TokenType.NOT),
                Map.entry("is", TokenType.IS),
                Map.entry("null", TokenType.NULL_LITERAL),
                Map.entry("true", TokenType.BOOLEAN_LITERAL),
                Map.entry("false", TokenType.BOOLEAN_LITERAL)
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

            if (!keywords.containsKey(token)) {
                return new Fail<>("No keyword could get matched");
            }

            TokenType tokenType = keywords.get(token);
            return new Ok(new TokenString(tokenType, token), new InputString(input, matcher.end()));
        };

        return lexer;
    }

    public static Lexer ComparisonOperator() {
        var re_operators = Pattern.compile("\\s*(?<token><=|>=|<>|[=<>])");

        Map<String, TokenType> comparison_operators = Map.ofEntries(
                Map.entry("=", TokenType.EQUALS),
                Map.entry("<>", TokenType.NOT_EQUALS),
                Map.entry("<", TokenType.LESS_THAN),
                Map.entry(">", TokenType.GREATER_THAN),
                Map.entry("<=", TokenType.LESS_EQUALS),
                Map.entry(">=", TokenType.GREATER_EQUALS)
        );

        Lexer lexer = (InputString source) -> {
            var input = source.input();
            var index = source.index();

            var matcher = re_operators.matcher(input);
            matcher.region(index, input.length());

            if (!matcher.lookingAt())
                return new Fail<>("No operator could get matched");

            var token = matcher.group("token");

            TokenType tokenType = comparison_operators.get(token);

            return new Ok<>(new TokenString(tokenType, token), new InputString(input, matcher.end()));
        };

        return lexer;
    }

    public static Lexer Literal(String literalInput, TokenType tokenType) {
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

            return new Ok<>(new TokenString(tokenType, token), new InputString(input, matcher.end()));
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
            return new Ok(new TokenString(TokenType.DOUBLE_LITERAL, token), new InputString(input, matcher.end()));
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
