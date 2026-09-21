package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.cyphail.compiler.lexer.LexerFactory;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.Parser;
import cr.ac.una.eif400.cyphail.compiler.parser.ParsersFactory;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.lexer.LexerFactory.Keywords;
import static cr.ac.una.eif400.cyphail.compiler.parser.ParsersFactory.Plus;
import static cr.ac.una.eif400.cyphail.compiler.parser.ParsersFactory.Star;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParsersTest {
    @ParameterizedTest(name = "Debería parsear múltiples keywords con éxito para: \"{0}\"")
    @ValueSource(strings = {
            "match create",
            "WHERE RETURN",
            "  cReatE   sEt   DeTaCh  ",
            "skip limit order by"
    })
    @DisplayName("Prueba paramétrica de múltiples Keywords exitosas con @ValueSource")
    void testMultipleKeywordsSuccess(String inputTexto) {
        Parser<InputString, List<TokenString>, String> multipleKeywordsParser = Plus(Keywords());

        InputString source = new InputString(inputTexto, 0);
        var resultado = multipleKeywordsParser.parse(source);

        assertTrue(resultado instanceof Ok, "El parser debió tener éxito para el texto: " + inputTexto);
    }

    @ParameterizedTest(name = "Star debería parsear siempre con éxito (incluso vacíos) para: \"{0}\"")
    @ValueSource(strings = {
            "match create",   // 2 aciertos
            "where",          // 1 acierto
            "12345",          // 0 aciertos (debe retornar lista vacía, no fallar)
            ""                // 0 aciertos (vacío)
    })
    void testStarParser(String inputTexto) {
        // Star reutiliza tu Plus, y Plus reutiliza tu Keywords
        Parser<InputString, List<TokenString>, String> starKeywords = Star(Keywords());

        InputString source = new InputString(inputTexto, 0);
        var resultado = starKeywords.parse(source);

        // ¡Star NUNCA debe fallar (Fail)! Siempre debe retornar Ok (con elementos o lista vacía)
        assertTrue(resultado instanceof Ok, "Star no debería fallar jamás, incluso si el input no tiene keywords");
    }

    @ParameterizedTest(name = "Optional debería retornar el token o el valor por defecto para: \"{0}\"")
    @ValueSource(strings = {
            "match where",  // Caso 1: El lexer interno encuentra la keyword exitosamente
            "12345",        // Caso 2: El lexer interno falla, debe usar el valor por defecto
            ""              // Caso 3: Input vacío, debe usar el valor por defecto
    })
    @DisplayName("Prueba paramétrica para el combinador Optional con LexerFactory")
    void testOptionalWithLexer(String inputTexto) {
        // Creamos un valor por defecto para cuando falte la keyword
        TokenString tokenPorDefecto = new TokenString(TokenType.KEYWORD, "default_match");

        // Envolvemos tu LexerFactory.Keywords() en el Optional
        Parser<InputString, TokenString, String> optionalParser = ParsersFactory.Optional(LexerFactory.Keywords(), tokenPorDefecto);

        InputString source = new InputString(inputTexto, 0);
        var resultado = optionalParser.parse(source);

        // Optional NUNCA debe fallar (Fail)
        assertTrue(resultado instanceof Ok, "El combinador Optional jamás debe retornar Fail");

        var okResult = (Ok<InputString, TokenString, String>) resultado;

        // Verificamos el comportamiento esperado
        if (inputTexto.trim().startsWith("match")) {
            assertEquals("match", okResult.token().value(), "Debería retornar el token real parseado");
        } else {
            assertEquals(tokenPorDefecto, okResult.token(), "Debería retornar el valor por defecto al fallar el parser");
        }
    }
}
