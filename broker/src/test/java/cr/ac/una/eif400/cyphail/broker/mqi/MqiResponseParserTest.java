package cr.ac.una.eif400.cyphail.broker.mqi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas del MqiResponseParser con respuestas reales copiadas de MQI.
 *
 * @author Sebastián González
 *
 * <p>Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 */
@DisplayName("Pruebas del MqiResponseParser")
public class MqiResponseParserTest {

    @Test
    @DisplayName("\"false\" es un Failure")
    void falseEsFailure() {
        assertInstanceOf(PrologResult.Failure.class, MqiResponseParser.parse("\"false\"\n"));
    }

    @Test
    @DisplayName("true trae una entrada por solucion con sus variables")
    void trueTraeLasSoluciones() {
        String json = """
                {"functor":"true", "args":[[
                    [ {"functor":"=", "args":["X", "a"]} ],
                    [ {"functor":"=", "args":["X", "b"]} ]
                ]]}
                """;

        PrologResult.Success success = assertInstanceOf(PrologResult.Success.class, MqiResponseParser.parse(json));
        assertEquals(List.of(Map.of("X", "a"), Map.of("X", "b")), success.solutions());
    }

    @Test
    @DisplayName("Toma la variable Rows de una solucion")
    void tomaRows() {
        String json = """
                {"functor":"true", "args":[[[
                    {"functor":"=", "args":["N", "_"]},
                    {"functor":"=", "args":["Rows", "\\"x\\"\\n\\"ana\\""]}
                ]]]}
                """;

        PrologResult.Success success = (PrologResult.Success) MqiResponseParser.parse(json);
        assertEquals("\"x\"\n\"ana\"", success.solutions().getFirst().get("Rows"));
    }

    @Test
    @DisplayName("exception con un atomo")
    void excepcionAtomo() {
        PrologResult.PrologError error = assertInstanceOf(PrologResult.PrologError.class,
                MqiResponseParser.parse("{\"functor\":\"exception\", \"args\":[\"instantiation_error\"]}"));
        assertEquals("instantiation_error", error.term());
    }

    @Test
    @DisplayName("exception con un termino compuesto")
    void excepcionCompuesta() {
        PrologResult.PrologError error = assertInstanceOf(PrologResult.PrologError.class, MqiResponseParser.parse(
                "{\"functor\":\"exception\", \"args\":[{\"functor\":\"cy_error\", \"args\":[\"No graph selected\"]}]}"));
        assertEquals(Map.of("functor", "cy_error", "args", List.of("No graph selected")), error.term());
    }

    @Test
    @DisplayName("Rechaza un texto que no es JSON de MQI")
    void rechazaTextoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> MqiResponseParser.parse("{no es json"));
        assertThrows(IllegalArgumentException.class, () -> MqiResponseParser.parse("42"));
    }
}
