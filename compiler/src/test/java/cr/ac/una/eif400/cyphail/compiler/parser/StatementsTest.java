package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas de la separacion de un script en sentencias por el token {@code ;}.
 *
 * @version P2
 */
@DisplayName("Pruebas de Statements")
public class StatementsTest {

    private static List<String> split(String script) {
        List<TokenString> tokens = assertOk(Tokenizer.tokenize(script)).value();
        return Statements.split(tokens).stream()
                .map(statement -> statement.stream().map(TokenString::value).reduce("", (left, right) -> left + right + " ").trim())
                .toList();
    }

    @Test
    @DisplayName("Dos sentencias terminadas en punto y coma quedan separadas")
    void dosSentencias() {
        assertEquals(List.of("MATCH ( a ) RETURN a", "MATCH ( b ) RETURN b"),
                split("MATCH (a) RETURN a; MATCH (b) RETURN b;"));
    }

    @Test
    @DisplayName("La ultima sentencia no necesita punto y coma")
    void sinPuntoYComaFinal() {
        assertEquals(List.of("RETURN 1", "RETURN 2"), split("RETURN 1; RETURN 2"));
    }

    @Test
    @DisplayName("Las sentencias vacias se descartan")
    void descartaLasVacias() {
        assertEquals(List.of("RETURN 1", "RETURN 2"), split(";; RETURN 1;; ; RETURN 2 ;"));
    }

    @Test
    @DisplayName("Un script vacio no tiene sentencias")
    void scriptVacio() {
        assertEquals(List.of(), split(""));
        assertEquals(List.of(), split(" ; ; "));
    }

    @Test
    @DisplayName("Un punto y coma dentro de un string no parte la sentencia")
    void puntoYComaEnUnString() {
        assertEquals(List.of("RETURN 'a;b'", "RETURN 1"), split("RETURN 'a;b'; RETURN 1"));
    }

    @Test
    @DisplayName("Un punto y coma dentro de un comentario no parte la sentencia")
    void puntoYComaEnUnComentario() {
        assertEquals(List.of("RETURN 1"), split("// una nota; con punto y coma\nRETURN 1"));
        assertEquals(List.of("RETURN 1"), split("/* a; b */ RETURN 1;"));
    }

    @Test
    @DisplayName("Una sentencia puede ocupar varias lineas")
    void sentenciaMultilinea() {
        assertEquals(List.of("MATCH ( a ) WHERE a . x > 1 RETURN a"),
                split("MATCH (a)\n  WHERE a.x > 1\n  RETURN a;"));
    }

    @Test
    void conservaElTextoSinConfundirTokensDentroDeComentariosOStrings() {
        String first = "RETURN 'a;b' AS texto,\r\n\r\n  'dos  espacios' AS otro /* ; RETURN */;";
        String second = "RETURN 2 // ; RETURN 9\n;";
        String last = "RETURN 3";
        String script = "// RETURN 0;\n;; " + first + " /* RETURN 5; */ " + second + last;
        var tokens = assertOk(Tokenizer.tokenizeWithComments(script)).value();
        var statements = Statements.withSource(script, tokens);

        assertEquals(List.of(first, second, last), statements.stream()
                .map(statement -> statement.source()).toList());
        assertEquals(Statements.split(assertOk(Tokenizer.tokenize(script)).value()),
                statements.stream().map(statement -> statement.tokens()).toList());
    }

    @Test
    void ignoraLosComentariosYSeparadoresSinConsulta() {
        String script = "// RETURN 0;\n; /* RETURN 1; */ ;;";
        assertEquals(List.of(), Statements.withSource(script,
                assertOk(Tokenizer.tokenizeWithComments(script)).value()));
    }
}
