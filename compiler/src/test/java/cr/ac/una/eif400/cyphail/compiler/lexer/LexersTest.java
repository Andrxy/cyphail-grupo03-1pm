package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertFail;
import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alejandro Miranda
 */
@DisplayName("Pruebas de los lexers individuales")
public class LexersTest {

    private static TokenString lex(Lexer lexer, String source) {
        return assertOk(lexer.parse(new InputString(source))).value();
    }

    @ParameterizedTest(name = "{0} es la palabra reservada {1}")
    @CsvSource({
            "MATCH, MATCH", "WHERE, WHERE", "CREATE, CREATE", "DELETE, DELETE",
            "DETACH, DETACH", "RETURN, RETURN", "AS, AS", "AND, AND", "NULL, NULL"
    })
    @DisplayName("Keywords reconoce las palabras reservadas")
    void keywordsReconocePalabrasReservadas(String source, TokenType expected) {
        assertEquals(new TokenString(expected, source), lex(Lexers.Keywords, source));
    }

    @Test
    @DisplayName("Keywords rechaza una palabra que no es reservada")
    void keywordsRechazaIdentificadores() {
        var failure = assertFail(Lexers.Keywords.parse(new InputString("Movie")));

        assertEquals("Unexpected token 'Movie' at index 0", failure.reason());
    }

    @Test
    @DisplayName("Identifier reconoce nombres con letras, digitos y guion bajo")
    void identifierReconoceNombres() {
        assertEquals(new TokenString(TokenType.IDENTIFIER, "_persona1"), lex(Lexers.Identifier, "_persona1"));
    }

    @Test
    @DisplayName("Identifier no acepta nombres que empiezan con digito")
    void identifierRechazaDigitoInicial() {
        var failure = assertFail(Lexers.Identifier.parse(new InputString("1abc")));

        assertEquals("Error at index 0", failure.reason());
    }

    @ParameterizedTest(name = "{0} es un numero")
    @CsvSource({"42", "0", "3.14"})
    @DisplayName("Numbers reconoce enteros y decimales")
    void numbersReconoceEnterosYDecimales(String source) {
        assertEquals(new TokenString(TokenType.NUMBER, source), lex(Lexers.Numbers, source));
    }

    @Test
    @DisplayName("StringLiteral reconoce comillas dobles y simples conservando las comillas")
    void stringLiteralReconoceAmbasComillas() {
        assertEquals(new TokenString(TokenType.STRING_LITERAL, "\"hola mundo\""), lex(Lexers.StringLiteral, "\"hola mundo\""));
        assertEquals(new TokenString(TokenType.STRING_LITERAL, "'hola'"), lex(Lexers.StringLiteral, "'hola'"));
    }

    @Test
    @DisplayName("StringLiteral falla si la cadena no se cierra")
    void stringLiteralSinCerrar() {
        assertFail(Lexers.StringLiteral.parse(new InputString("\"abierta")));
    }

    @ParameterizedTest(name = "{0} es {1}")
    @CsvSource({
            "=, EQUALS", "<>, NOTEQUALS", "<, LESS_THAN", ">, GREATER_THAN",
            "<=, LESS_THAN_OR_EQUAL", ">=, GREATER_THAN_OR_EQUAL"
    })
    @DisplayName("Comparison reconoce los operadores de comparacion")
    void comparisonReconoceOperadores(String source, TokenType expected) {
        assertEquals(new TokenString(expected, source), lex(Lexers.Comparison, source));
    }

    @ParameterizedTest(name = "{0} es {1}")
    @CsvSource({"+, PLUS", "-, MINUS", "*, MULTIPLY", "/, DIVIDE", "%, MODULO"})
    @DisplayName("Arithmetic reconoce los operadores aritmeticos")
    void arithmeticReconoceOperadores(String source, TokenType expected) {
        assertEquals(new TokenString(expected, source), lex(Lexers.Arithmetic, source));
    }

    @ParameterizedTest(name = "{0} es {1}")
    @CsvSource(delimiter = ' ', value = {
            "( LPAREN", ") RPAREN", "[ LBRACKET", "] RBRACKET", "{ LBRACE", "} RBRACE",
            ". DOT", "',' COMMA", ": COLON", "| PIPE", "; SEMICOLON"
    })
    @DisplayName("Delimiter reconoce los signos de puntuacion")
    void delimiterReconoceSignos(String source, TokenType expected) {
        assertEquals(new TokenString(expected, source), lex(Lexers.Delimiter, source));
    }

    @ParameterizedTest(name = "{0} es {1}")
    @CsvSource(delimiter = ' ', value = {"-> RIGHT_ARROW", "<- LEFT_ARROW", ".. RANGE_DOTS"})
    @DisplayName("Relationship reconoce flechas y rangos")
    void relationshipReconoceFlechas(String source, TokenType expected) {
        assertEquals(new TokenString(expected, source), lex(Lexers.Relationship, source));
    }

    @Test
    @DisplayName("Un lexer ignora los espacios antes del token y avanza hasta su final")
    void ignoraEspaciosPrevios() {
        var ok = assertOk(Lexers.Keywords.parse(new InputString("   MATCH (")));

        assertEquals(new TokenString(TokenType.MATCH, "MATCH"), ok.value());
        assertEquals(8, ok.rest().index());
    }

    @Test
    @DisplayName("Un lexer empieza a leer desde el indice de la entrada")
    void empiezaDesdeElIndice() {
        var ok = assertOk(Lexers.Identifier.parse(new InputString("MATCH movie", 5)));

        assertEquals(new TokenString(TokenType.IDENTIFIER, "movie"), ok.value());
        assertEquals(11, ok.rest().index());
    }

    @Test
    @DisplayName("El mensaje de error indica el indice donde fallo")
    void errorIndicaElIndice() {
        var failure = assertFail(Lexers.Numbers.parse(new InputString("MATCH x", 5)));

        assertEquals("Error at index 5", failure.reason());
    }
}
