package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertFail;
import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas del tokenizer para comentarios, identificadores reservados, flechas y palabras clave del sprint P2.
 *
 * @version P2
 */
@DisplayName("Pruebas del tokenizer del sprint P2")
public class TokenizerP2Test {

    private static List<TokenType> types(String source) {
        return assertOk(Tokenizer.tokenize(source)).value().stream().map(TokenString::type).toList();
    }

    @Test
    @DisplayName("Un comentario de linea se ignora")
    void comentarioDeLinea() {
        assertEquals(List.of(MATCH, LPAREN, IDENTIFIER, RPAREN, RETURN, IDENTIFIER),
                types("MATCH (a) // lo demas se ignora\nRETURN a"));
    }

    @Test
    @DisplayName("Un comentario de bloque se ignora aunque ocupe varias lineas")
    void comentarioDeBloque() {
        assertEquals(List.of(MATCH, LPAREN, IDENTIFIER, RPAREN, RETURN, IDENTIFIER),
                types("MATCH /* un\ncomentario */ (a) RETURN a"));
    }

    @Test
    @DisplayName("Un texto de solo comentarios no produce tokens")
    void soloComentarios() {
        assertEquals(List.of(), types("// nada\n/* nada */"));
    }

    @Test
    @DisplayName("Un comentario dentro de un string no es comentario")
    void comentarioDentroDeString() {
        assertEquals(List.of(RETURN, STRING_LITERAL), types("RETURN 'a // b'"));
    }

    @Test
    @DisplayName("La division sigue siendo un token")
    void divisionNoEsComentario() {
        assertEquals(List.of(IDENTIFIER, DIVIDE, IDENTIFIER), types("a / b"));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"MATCH (__p) RETURN __p", "MATCH (___x) RETURN 1", "__a"})
    @DisplayName("Un identificador que empieza con dos guiones bajos se rechaza")
    void identificadorReservado(String source) {
        assertFail(Tokenizer.tokenize(source));
    }

    @Test
    @DisplayName("El error de un identificador reservado senala su posicion")
    void identificadorReservadoSenalaLaPosicion() {
        assertEquals("Lexical Error: Unexpected character at index 7",
                assertFail(Tokenizer.tokenize("MATCH (__p) RETURN __p")).reason());
    }

    @Test
    @DisplayName("Un guion bajo simple o dos en medio del nombre si se aceptan")
    void guionesBajosPermitidos() {
        assertEquals(List.of(IDENTIFIER, IDENTIFIER), types("a__b _x"));
    }

    @Test
    @DisplayName("Las flechas de relacion son tokens propios")
    void flechas() {
        assertEquals(List.of(LPAREN, IDENTIFIER, RPAREN, MINUS, LBRACKET, COLON, IDENTIFIER, RBRACKET, RIGHT_ARROW,
                        LPAREN, IDENTIFIER, RPAREN),
                types("(a)-[:K]->(b)"));
        assertEquals(List.of(LPAREN, IDENTIFIER, RPAREN, LEFT_ARROW, LBRACKET, COLON, IDENTIFIER, RBRACKET, MINUS,
                        LPAREN, IDENTIFIER, RPAREN),
                types("(a)<-[:K]-(b)"));
    }

    @Test
    @DisplayName("Los puntos de un rango se separan de los numeros")
    void puntosDeRango() {
        assertEquals(List.of(MULTIPLY, NUMBER, RANGE_DOTS, NUMBER), types("*1..3"));
    }

    @Test
    @DisplayName("El punto y coma es un token")
    void puntoYComa() {
        assertEquals(List.of(RETURN, NUMBER, SEMICOLON), types("RETURN 1;"));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"match (a) return a", "Match (a) Return a", "mAtCh (a) rEtUrN a"})
    @DisplayName("Las palabras clave se reconocen en mayusculas o minusculas")
    void palabrasClaveSinDistinguirMayusculas(String source) {
        assertEquals(List.of(MATCH, LPAREN, IDENTIFIER, RPAREN, RETURN, IDENTIFIER), types(source));
    }

    @Test
    @DisplayName("Una palabra que solo contiene una palabra clave sigue siendo un identificador")
    void palabraQueContieneUnaPalabraClave() {
        assertEquals(List.of(IDENTIFIER, IDENTIFIER, IDENTIFIER), types("matches order_id returned"));
    }
}
