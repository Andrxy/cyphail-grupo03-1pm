package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static cr.ac.una.eif400.cyphail.compiler.lexer.LexerFactory.Keywords;
import static cr.ac.una.eif400.cyphail.compiler.lexer.LexerFactory.Operators;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LexersTest {
    @ParameterizedTest
    @DisplayName("Debería reconocer keywords válidas (case-insensitive) y avanzar el índice")
    @ValueSource(strings = {"match", "MATCH", "cReatE", "RETURN"})
    void testKeywordsExitosas(String textoKeywords) {
        InputString source = new InputString(textoKeywords, 0);

        Lexer lexer = Keywords();
        var resultado = lexer.parse(source);

        assertTrue(resultado instanceof Ok, "El lexer debió aceptar la keyword: " + textoKeywords);

        Ok okResult = (Ok) resultado;
        TokenString token = (TokenString) okResult.token();

        assertEquals(textoKeywords.toLowerCase(), token.value());
    }

    @ParameterizedTest
    @DisplayName("Debería reconocer operators validos")
    @ValueSource(strings = {"   >=", "   =   ", "<=   ", "    <>", "    *"})
    void testOperatorsExitosos(String texto) {
        InputString source = new InputString(texto, 0);

        Lexer lexer = Operators();
        var resultado = lexer.parse(source);

        assertTrue(resultado instanceof Ok, "El lexer debió aceptar la keyword: " + texto);

        Ok okResult = (Ok) resultado;
        TokenString token = (TokenString) okResult.token();

        assertEquals(texto.trim(), token.value());
    }
}
