package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.Map;
import java.util.regex.Pattern;
/*
* @author Alejandro Miranda
* @author Andrey Solano
* */
public class Lexers {

    private static Lexer createLexer(String re, TokenType type) {
        return (InputString input) -> {
            String source = input.input();
            int index = input.index();

            var re_ = Pattern.compile(re);
            var matcher = re_.matcher(source);
            matcher.region(index, source.length());

            if (!matcher.lookingAt())
                return new Fail<>("No token was found at index " + index);

            var token = matcher.group("token");
            return new Ok<>(new InputString(source, matcher.end()), new TokenString(type, token));
        };
    }

    public static Lexer Identifier = createLexer("(?<token>[a-zA-Z_]\\w*)", TokenType.IDENTIFIER);

    public static Lexer Number = createLexer("(?<token>[+-]?\\d+)", TokenType.NUMBER);

    public static Lexer Keywords = createLexer(
            "(?i)(?<token>MATCH|WHERE|CREATE|SET|DETACH|DELETE|REMOVE|RETURN|DISTINCT|AS|ORDER|BY|ASCENDING|DESCENDING|SKIP|LIMIT|OR|AND|NOT|IS)\\b",
            TokenType.KEYWORD
    );

    public static Lexer Comparison = createLexer("(?<token><=|>=|<>|[=<>])", TokenType.COMPARISON_OPERATOR);

    public static Lexer Arithmetic = createLexer("(?<token>[+\\-*/%])", TokenType.ARITHMETIC_OPERATOR);

    public static Lexer Delimiters = createLexer("(?<token>[()\\[\\]{}.,:|;])", TokenType.DELIMITER);

    public static Lexer Relationship = createLexer("(?<token>->|<-|\\.\\.)", TokenType.RELATIONSHIP);

    public static Lexer StringLiteral = createLexer("(?<token>\"[^\"]*\"|'[^']*')", TokenType.STRING_LITERAL);

    public static Lexer Whitespaces = createLexer("(?<token>\\s+)", TokenType.WHITESPACE);

    public static Lexer LineComment = createLexer("(?<token>//[^\\r\\n]*)", TokenType.LINECOMMENT);

    public static Lexer CommentBlock = createLexer("(?<token>/\\*.*?\\*/)", TokenType.BLOCKCOMMENT);

}
