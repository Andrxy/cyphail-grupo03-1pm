package cr.ac.una.eif400.cyphail.compiler.parser.input;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alejandro Miranda
 */
@DisplayName("Pruebas de las entradas del parser")
public class InputTokensTest {

    private static final TokenString MATCH = new TokenString(TokenType.MATCH, "MATCH");
    private static final TokenString LPAREN = new TokenString(TokenType.LPAREN, "(");

    @Test
    @DisplayName("InputTokens empieza en el indice 0 por defecto")
    void empiezaEnCero() {
        assertEquals(0, new InputTokens(List.of(MATCH)).index());
    }

    @Test
    @DisplayName("first devuelve el token en el indice actual")
    void firstDevuelveElTokenActual() {
        assertEquals(LPAREN, new InputTokens(List.of(MATCH, LPAREN), 1).first());
    }

    @Test
    @DisplayName("next avanza una posicion sin modificar la entrada original")
    void nextAvanzaUnaPosicion() {
        InputTokens original = new InputTokens(List.of(MATCH, LPAREN));

        InputTokens siguiente = original.next();

        assertEquals(1, siguiente.index());
        assertEquals(LPAREN, siguiente.first());
        assertEquals(0, original.index());
    }

    @Test
    @DisplayName("isEmpty es falso mientras queden tokens")
    void noEstaVaciaConTokens() {
        assertFalse(new InputTokens(List.of(MATCH)).isEmpty());
    }

    @Test
    @DisplayName("isEmpty es verdadero al consumir todos los tokens")
    void estaVaciaAlConsumirTodo() {
        assertTrue(new InputTokens(List.of(MATCH)).next().isEmpty());
        assertTrue(new InputTokens(List.of()).isEmpty());
    }

    @Test
    @DisplayName("InputString empieza en el indice 0 por defecto")
    void inputStringEmpiezaEnCero() {
        InputString input = new InputString("MATCH");

        assertEquals("MATCH", input.input());
        assertEquals(0, input.index());
    }
}
