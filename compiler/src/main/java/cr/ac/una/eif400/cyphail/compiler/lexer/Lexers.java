package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Andrey Solano
 * @author Alejandro Miranda
 */
public class Lexers {

    private static Lexer specificLexer(String regex, TokenType type) {
        return (InputString source) -> {
            var sourceString = source.source();
            var index = source.index();

            var regularExpr = Pattern.compile("\\s*(?<token>" + regex + ")");
            var matcher = regularExpr.matcher(sourceString);
            matcher.region(index, sourceString.length());

            if (!matcher.lookingAt())
                return new Fail<>("Error at index " + index);

            var token = matcher.group("token");

            return new Ok<>(
                    new InputString(sourceString, matcher.end()),
                    new TokenString(type, token)
            );
        };
    }

    private static Lexer sneakyLexer(String regex, Map<String, TokenType> map) {
        return (InputString source) -> {
            var sourceString = source.source();
            var index = source.index();

            var regularExpr = Pattern.compile("\\s*(?<token>" + regex + ")");
            var matcher = regularExpr.matcher(sourceString);
            matcher.region(index, sourceString.length());

            if (!matcher.lookingAt())
                return new Fail<>("Error at index " + index);

            var token = matcher.group("token");

            TokenType type = map.get(token.toUpperCase());
            if (type == null) {
                return new Fail<>("Unexpected token '" + token + "' at index " + index);
            }

            return new Ok<>(new InputString(sourceString, matcher.end()),
                            new TokenString(type, token));
        };
    }

    public static Lexer Keywords = sneakyLexer("(?i)[a-z]+", KEYWORDS);

    public static Lexer Arithmetic = sneakyLexer("[+\\-*/%]", ARITHMETIC);

    public static Lexer Comparison = sneakyLexer("<=|>=|<>|[=<>]", COMPARISON);

    public static Lexer Delimiter = sneakyLexer("[()\\[\\]{}.,:|;]", DELIMITER);

    public static Lexer Relationship = sneakyLexer("->|<-|\\.\\.", RELATIONSHIP);

    public static Lexer StringLiteral = specificLexer("\"[^\"]*\"|'[^']*'", TokenType.STRING_LITERAL);

    public static Lexer Identifier = specificLexer("[a-zA-Z_]\\w*", TokenType.IDENTIFIER);

    public static Lexer Numbers = specificLexer("\\d+(\\.\\d+)?", TokenType.NUMBER);
}

