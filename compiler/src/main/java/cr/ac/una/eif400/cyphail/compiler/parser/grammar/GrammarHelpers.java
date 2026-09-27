package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.IParser;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.parser.Combinators.*;

public class GrammarHelpers {

    static Result<InputTokens, TokenString, String> eat(TokenType type, InputTokens source) {
        if (source.isEmpty())
            return new Fail<>(String.format("Expecting token %s. Found empty source", type));
        var token = source.first();
        if (token.type() != type)
            return new Fail<>(String.format("Expecting token %s. Found %s", type, token.type()));
        return new Ok<>(source.next(), token);
    }

    static IParser<InputTokens, AstNode, String> parsify(TokenType type) {
        return (InputTokens source) -> switch (eat(type, source)) {
            case Fail(String reason) -> new Fail<>(reason);
            case Ok(InputTokens rest, TokenString token) -> new Ok<>(rest, new Leaf(token));
        };
    }

    static IParser<InputTokens, AstNode, String> EOF() {
        return (InputTokens source) -> {
            if (source.isEmpty())
                return new Ok<>(source, new Leaf(new TokenString(TokenType.EOF, "")));
            return new Fail<>(String.format("Expecting end of input. Found %s", source.first().type()));
        };
    }

    static IParser<InputTokens, AstNode, String> asNode(IParser<InputTokens, List<AstNode>, String> parser) {
        return TFlat(parser, items -> new Node(items));
    }

    static IParser<InputTokens, AstNode, String> orEmpty(IParser<InputTokens, AstNode, String> listParser) {
        return OrFlat(concatStringList, listParser, (InputTokens source) -> new Ok<>(source, new Node(List.of())));
    }
}
