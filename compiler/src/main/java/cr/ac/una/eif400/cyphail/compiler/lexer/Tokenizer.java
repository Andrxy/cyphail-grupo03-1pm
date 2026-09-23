package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.parser.Parser;
import cr.ac.una.eif400.cyphail.compiler.parser.Combinators;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.List;

public class Tokenizer {

    public static List<TokenString> tokenize(String sourceCode) {
        InputString source = new InputString(sourceCode, 0);

        Parser<InputString, TokenString> tokenParser = Combinators.Or(
                Lexers.Whitespaces,
                Lexers.LineComment,
                Lexers.CommentBlock,
                Lexers.StringLiteral,
                Lexers.Keywords,
                Lexers.Identifier,
                Lexers.Number,
                Lexers.Relationship,
                Lexers.Comparison,
                Lexers.Arithmetic,
                Lexers.Delimiters
        );

        Parser<InputString, List<TokenString>> fullTokenizer = Combinators.Star(tokenParser);

        var re = fullTokenizer.parse(source);
        if (re instanceof Ok<InputString, List<TokenString>>(InputString input, List<TokenString> tokens)) {
            return tokens;
        }
        throw new RuntimeException("Lexical analysis failed");

    }
}