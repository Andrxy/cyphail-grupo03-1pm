package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

public class Utils {
    public static Parser<InputTokens, TokenString, String> matchToken(TokenType expected) {
        return (InputTokens source) -> {
            var tokens = source.input();
            var index = source.index();

            var current = tokens.get(index);

            if (current.type() != expected)
                return new Fail<>("Se esperaba un " + expected);

            return new Ok<>(current, new InputTokens(tokens, index + 1));
        };
    }
}
