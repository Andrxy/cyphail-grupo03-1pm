package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.Map;
import java.util.regex.Pattern;

public class Lexers {

    private static Lexer createLexer(String re) {
        return (InputString input) -> {
            String source = input.input();
            int index = input.index();

            var re_ = Pattern.compile(re);
            var matcher = re_.matcher(source);
            matcher.region(index, source.length());

            if (!matcher.lookingAt())
                return new Fail<>("No token was found at index " + matcher.end());

            var token = matcher.group("token");
            return new Ok<>(new InputString(source, matcher.end()), new TokenString(TokenType.AS, token));
        };
    }

    public static Lexer Identifier = createLexer("\\s*(?<token>[a-zA-Z_]\\w*)");

    public static Lexer Number = createLexer("\\s*(?<token>[+-]?\\d+)");

    public static Lexer Keywords = createLexer("\\s*(?<token>[a-zA-z]+)");

    public static Lexer Comparison = createLexer("\\s*(?<token><=|>=|<>|[=<>])");

    public static Lexer Arithmetic = createLexer("\\s*(?<token>[+\\-*/%])");

    public static Lexer Delimiters = createLexer("\\s*(?<token>[()\\[\\]{}.,:|])");

    public static Lexer Relationship = createLexer("\\s*(?<token>->|<-|\\.\\.)");

    public static Lexer StringLiteral = createLexer("\\s*(?<token>\"[^\"]*\"|'[^']*')");
}
