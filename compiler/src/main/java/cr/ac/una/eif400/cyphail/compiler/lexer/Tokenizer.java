package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.IParser;
import cr.ac.una.eif400.cyphail.compiler.parser.Combinators;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;

import java.util.List;

public class Tokenizer {

    public static List<TokenString> tokenize(String sourceCode) {
        InputString source = new InputString(sourceCode, 0);

        IParser<InputString, TokenString> tokenParser = Combinators.Or(
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

        IParser<InputString, List<TokenString>> fullTokenizer = Combinators.Star(tokenParser);

        var re = fullTokenizer.parse(source);
        if (re instanceof Ok<InputString, List<TokenString>>(InputString input, List<TokenString> tokens)) {
            return tokens.stream().filter(x -> x.type() != TokenType.WHITESPACE && x.type() != TokenType.LINECOMMENT && x.type() != TokenType.BLOCKCOMMENT ).toList();
        }
        throw new RuntimeException("Lexical analysis failed");
    }
}