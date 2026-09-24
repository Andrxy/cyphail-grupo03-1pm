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

/*
* @author Alejandro Miranda
* @author Andrey Solano
* */
public class Lexers {

    private static Lexer createLexer(String re, TokenType type) {
        return (InputString input) -> {
            String source = input.input();
            int index = input.index();

            var re_ = Pattern.compile("(?<token>" + re + ")");
            var matcher = re_.matcher(source);
            matcher.region(index, source.length());
       if (!matcher.lookingAt())
                return new Fail<>("No token was found at index " + index);

            var token = matcher.group("token");
            return new Ok<>(new InputString(source, matcher.end()), new TokenString(type, token));
        };
    }

    private static final  List<String> KEYWORDS = List.of(
            "MATCH", "WHERE", "CREATE", "SET", "DETACH", "DELETE", "REMOVE", "RETURN",
            "DISTINCT", "AS", "ORDER", "BY", "ASCENDING", "DESCENDING", "SKIP", "LIMIT",
            "OR", "AND", "NOT", "IS"
    );

    private static String keywordsMegaOr() {
        return KEYWORDS.stream().collect(Collectors.joining("|", "(", ")"));
    }

    public static Lexer Identifier = createLexer("[a-zA-Z_]\\w*", TokenType.IDENTIFIER);

    public static Lexer Number = createLexer("[+-]?\\d+", TokenType.NUMBER);

    public static Lexer Keywords = createLexer(
            "(?i)" + keywordsMegaOr() + "\\b",
            TokenType.KEYWORD
    );

    public static Lexer Comparison = createLexer("<=|>=|<>|[=<>]", TokenType.COMPARISON_OPERATOR);

    public static Lexer Arithmetic = createLexer("[+\\-*/%]", TokenType.ARITHMETIC_OPERATOR);

    public static Lexer Delimiters = createLexer("[()\\[\\]{}.,:|;]", TokenType.DELIMITER);

    public static Lexer Relationship = createLexer("->|<-|\\.\\.", TokenType.RELATIONSHIP);

    public static Lexer StringLiteral = createLexer("\"[^\"]*\"|'[^']*'", TokenType.STRING_LITERAL);

    public static Lexer Whitespaces = createLexer("\\s+", TokenType.WHITESPACE);

    public static Lexer LineComment = createLexer("//[^\\r\\n]*", TokenType.LINECOMMENT);

    public static Lexer CommentBlock = createLexer("/\\*.*?\\*/", TokenType.BLOCKCOMMENT);
}
