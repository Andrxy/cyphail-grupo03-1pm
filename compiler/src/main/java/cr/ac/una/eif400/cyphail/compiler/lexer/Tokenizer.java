    package cr.ac.una.eif400.cyphail.compiler.lexer;

    import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
    import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
    import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
    import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;
    import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;

    import java.util.List;

    import static cr.ac.una.eif400.cyphail.compiler.lexer.Lexers.*;
    import static cr.ac.una.eif400.cyphail.compiler.parser.Combinators.*;

    public class Tokenizer {

        public static Result<InputString, List<TokenString>, String> tokenize(String sourceCode) {
            var anyToken = OrFlat(
                    concatStringList,
                    Relationship,
                    Comparison,
                    Keywords,
                    Identifier,
                    StringLiteral,
                    Numbers,
                    Arithmetic,
                    Delimiter
            );

            var fullTokenizer = Star(anyToken);
            var result = fullTokenizer.parse(new InputString(sourceCode));

            if (result instanceof Ok(var rest, var tokens)) {
                if (rest.index() < sourceCode.length()) {
                    return new Fail<>("Lexical Error: Unexpected character at index " + rest.index());
                }
                return new Ok<>(rest, tokens);
            }

            if (result instanceof Fail(var error)) {
                return new Fail<>(error);
            }

            throw new RuntimeException("Error");
        }
    }