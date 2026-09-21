package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.parser.Parser;
import cr.ac.una.eif400.cyphail.compiler.parser.ParsersFactory;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.List;

public class Tokenizer {

    public static List<TokenString> tokenize(String sourceCode) {
        InputString source = new InputString(sourceCode, 0);

        Parser<InputString, TokenString, String> tokenParser = ParsersFactory.Or(
                LexerFactory.Keywords(),
                LexerFactory.Identifier(),
                LexerFactory.ComparisonOperator(),
                LexerFactory.Integer()
        );

        Parser<InputString, List<TokenString>, String> fullTokenizer = ParsersFactory.Star(tokenParser);

        var result = fullTokenizer.parse(source);

        if (result instanceof Ok(List<TokenString> tokens, InputString rest)) {
            return tokens;
        }

        throw new RuntimeException("Fallo al tokenizar el input.");
    }
}
