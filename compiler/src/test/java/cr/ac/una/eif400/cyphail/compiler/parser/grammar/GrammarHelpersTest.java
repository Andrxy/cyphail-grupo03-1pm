package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.specializations.AstNode;
import cr.ac.una.eif400.cyphail.compiler.ast.specializations.Leaf;
import cr.ac.una.eif400.cyphail.compiler.ast.specializations.Node;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertFail;
import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static cr.ac.una.eif400.cyphail.compiler.parser.Combinators.Star;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Pruebas de GrammarHelpers")
public class GrammarHelpersTest {

    private static final TokenString MATCH = new TokenString(TokenType.MATCH, "MATCH");
    private static final TokenString LPAREN = new TokenString(TokenType.LPAREN, "(");

    private static InputTokens input(TokenString... tokens) {
        return new InputTokens(List.of(tokens));
    }

    @Test
    @DisplayName("eat consume el token si es del tipo esperado")
    void eatConsumeElTokenEsperado() {
        var ok = assertOk(GrammarHelpers.eat(TokenType.MATCH, input(MATCH, LPAREN)));

        assertEquals(MATCH, ok.value());
        assertEquals(1, ok.rest().index());
    }

    @Test
    @DisplayName("eat falla indicando el tipo esperado y el encontrado")
    void eatFallaConTipoDistinto() {
        var failure = assertFail(GrammarHelpers.eat(TokenType.RETURN, input(MATCH)));

        assertEquals("Expecting token RETURN. Found MATCH", failure.reason());
    }

    @Test
    @DisplayName("eat falla si ya no quedan tokens")
    void eatFallaSinTokens() {
        var failure = assertFail(GrammarHelpers.eat(TokenType.MATCH, input()));

        assertEquals("Expecting token MATCH. Found empty source", failure.reason());
    }

    @Test
    @DisplayName("parsify envuelve el token consumido en una hoja del AST")
    void parsifyDevuelveUnaHoja() {
        var ok = assertOk(GrammarHelpers.parsify(TokenType.MATCH).parse(input(MATCH)));

        assertEquals(new Leaf(MATCH), ok.value());
        assertEquals(1, ok.rest().index());
    }

    @Test
    @DisplayName("parsify propaga el error de eat")
    void parsifyPropagaElError() {
        var failure = assertFail(GrammarHelpers.parsify(TokenType.RETURN).parse(input(LPAREN)));

        assertEquals("Expecting token RETURN. Found LPAREN", failure.reason());
    }

    @Test
    @DisplayName("EOF tiene exito cuando no quedan tokens")
    void eofSinTokens() {
        var ok = assertOk(GrammarHelpers.EOF().parse(input(MATCH).next()));

        assertEquals(new Leaf(new TokenString(TokenType.EOF, "")), ok.value());
    }

    @Test
    @DisplayName("EOF falla si todavia quedan tokens")
    void eofConTokensRestantes() {
        var failure = assertFail(GrammarHelpers.EOF().parse(input(LPAREN)));

        assertEquals("Expecting end of input. Found LPAREN", failure.reason());
    }

    @Test
    @DisplayName("asNode envuelve la lista reconocida en un Node")
    void asNodeEnvuelveLaLista() {
        var ok = assertOk(GrammarHelpers.asNode(Star(GrammarHelpers.parsify(TokenType.MATCH))).parse(input(MATCH, MATCH, LPAREN)));

        assertEquals(new Node(List.of(new Leaf(MATCH), new Leaf(MATCH))), ok.value());
        assertEquals(2, ok.rest().index());
    }

    @Test
    @DisplayName("orEmpty devuelve el resultado del parser cuando tiene exito")
    void orEmptyConExito() {
        var ok = assertOk(GrammarHelpers.orEmpty(GrammarHelpers.parsify(TokenType.MATCH)).parse(input(MATCH)));

        assertEquals(new Leaf(MATCH), ok.value());
    }

    @Test
    @DisplayName("orEmpty devuelve un Node vacio sin consumir cuando el parser falla")
    void orEmptySinExito() {
        var ok = assertOk(GrammarHelpers.orEmpty(source -> new Fail<InputTokens, AstNode, String>("no")).parse(input(LPAREN)));

        assertEquals(new Node(List.of()), ok.value());
        assertEquals(0, ok.rest().index());
    }
}
