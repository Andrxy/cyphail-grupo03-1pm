package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertFail;
import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alejandro Miranda
 */
@DisplayName("Pruebas del Tokenizer")
public class TokenizerTest {

    private static List<TokenString> tokens(String source) {
        return assertOk(Tokenizer.tokenize(source)).value();
    }

    private static List<TokenType> types(String source) {
        return tokens(source).stream().map(TokenString::type).toList();
    }

    @Test
    @DisplayName("Tokeniza una consulta completa en el orden en que aparece")
    void tokenizaUnaConsultaCompleta() {
        assertEquals(
                List.of(MATCH, LPAREN, IDENTIFIER, COLON, IDENTIFIER, RPAREN,
                        RETURN, IDENTIFIER, DOT, IDENTIFIER, AS, IDENTIFIER),
                types("MATCH (m:Movie) RETURN m.title AS title"));
    }

    @Test
    @DisplayName("Conserva el texto original de cada token")
    void conservaElTexto() {
        assertEquals(
                List.of(
                        new TokenString(IDENTIFIER, "m"),
                        new TokenString(DOT, "."),
                        new TokenString(IDENTIFIER, "year"),
                        new TokenString(GREATER_THAN_OR_EQUAL, ">="),
                        new TokenString(NUMBER, "1999")),
                tokens("m.year >= 1999"));
    }

    @Test
    @DisplayName("Una palabra reservada se reconoce antes que un identificador")
    void palabraReservadaAntesQueIdentificador() {
        assertEquals(List.of(WHERE, IDENTIFIER), types("WHERE WHEREVER"));
    }

    @Test
    @DisplayName("Los operadores de dos caracteres no se parten en dos tokens")
    void operadoresDeDosCaracteres() {
        assertEquals(List.of(RIGHT_ARROW, LEFT_ARROW, NOTEQUALS, LESS_THAN_OR_EQUAL, RANGE_DOTS),
                types("-> <- <> <= .."));
    }

    @Test
    @DisplayName("Un numero decimal es un solo token")
    void numeroDecimal() {
        assertEquals(List.of(new TokenString(NUMBER, "0.39")), tokens("0.39"));
    }

    @Test
    @DisplayName("Una cadena con espacios es un solo token")
    void cadenaConEspacios() {
        assertEquals(List.of(new TokenString(STRING_LITERAL, "\"hola mundo\"")), tokens("\"hola mundo\""));
    }

    @Test
    @DisplayName("Tokeniza un mapa de propiedades")
    void tokenizaPropiedades() {
        assertEquals(
                List.of(LBRACE, IDENTIFIER, COLON, NUMBER, COMMA, IDENTIFIER, COLON, STRING_LITERAL, RBRACE),
                types("{id: 1, status: \"cancelled\"}"));
    }

    @Test
    @DisplayName("Una consulta vacia produce una lista vacia de tokens")
    void consultaVacia() {
        assertEquals(List.of(), tokens(""));
        assertEquals(List.of(), tokens("   "));
    }

    @Test
    @DisplayName("Los espacios y saltos de linea al final no son un error lexico")
    void espaciosAlFinal() {
        assertEquals(List.of(MATCH, LPAREN, IDENTIFIER, RPAREN), types("MATCH (n)   \n\t"));
    }

    @Test
    @DisplayName("Los saltos de linea entre tokens se ignoran")
    void saltosDeLineaEntreTokens() {
        assertEquals(List.of(MATCH, LPAREN, IDENTIFIER, RPAREN, RETURN, IDENTIFIER), types("MATCH (n)\nRETURN n"));
    }

    @Test
    @DisplayName("Un caracter desconocido produce un error lexico con su indice")
    void caracterDesconocido() {
        var failure = assertFail(Tokenizer.tokenize("MATCH (n)@"));

        assertEquals("Lexical Error: Unexpected character at index 9", failure.reason());
    }

    @Test
    @DisplayName("El error lexico se reporta aunque el caracter este en medio de la consulta")
    void caracterDesconocidoEnMedio() {
        var failure = assertFail(Tokenizer.tokenize("MATCH # RETURN n"));

        assertTrue(failure.reason().startsWith("Lexical Error"));
    }

    @Test
    @DisplayName("Una cadena sin cerrar produce un error lexico")
    void cadenaSinCerrar() {
        assertFail(Tokenizer.tokenize("MATCH (n {name: \"Ana})"));
    }
}
