package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Ayudas compartidas por las pruebas de la gramatica del sprint P2.
 *
 * @author Alejandro Miranda
 * @version P2
 */
final class GrammarTestSupport {

    private GrammarTestSupport() {
    }

    static Result<InputTokens, AstNode, String> parse(String source) {
        return Grammar.parse(assertOk(Tokenizer.tokenize(source)).value());
    }

    static Query query(String source) {
        return assertInstanceOf(Query.class, assertOk(parse(source)).value());
    }

    static Leaf leaf(TokenType type, String value) {
        return new Leaf(new TokenString(type, value));
    }

    static Leaf id(String name) {
        return leaf(TokenType.IDENTIFIER, name);
    }

    static Leaf num(String value) {
        return leaf(TokenType.NUMBER, value);
    }

    static Leaf text(String value) {
        return leaf(TokenType.STRING_LITERAL, value);
    }

    static PropertyAccess access(String variable, String key) {
        return new PropertyAccess(id(variable), id(key));
    }

    static AstNode firstPattern(String source) {
        return query(source).matches().getFirst().patterns().getFirst();
    }

    static AstNode whereOf(String source) {
        return query(source).matches().getFirst().where().getFirst().condition();
    }

    static AstNode projectionOf(String source) {
        return ((SimpleProjection) query(source).ret().getFirst().projections().getFirst()).expression();
    }
}
