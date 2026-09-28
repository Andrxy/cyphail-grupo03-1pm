package cr.ac.una.eif400.cyphail.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la respuesta estandar que viaja entre el broker, el compilador y el CLI.
 *
 * @author Alejandro Miranda
 * @version P1.1
 */
@DisplayName("Pruebas de QueryResponse")
public class QueryResponseTest {

    private static final String[] HEADERS = {"nombre", "edad"};
    private static final String[][] ROWS = {{"Ana", "28"}, {"Luis", "31"}};

    @Test
    @DisplayName("Una respuesta ok queda marcada como exitosa")
    void okQuedaMarcadaComoExitosa() {
        QueryResponse response = QueryResponse.ok(HEADERS, ROWS, "OK");
        assertTrue(response.isOk());
    }

    @Test
    @DisplayName("Una respuesta ok conserva encabezados, filas y mensaje")
    void okConservaLosDatos() {
        QueryResponse response = QueryResponse.ok(HEADERS, ROWS, "OK. Query resolved.");

        assertArrayEquals(HEADERS, response.getHeaders(), "Conserva los encabezados");
        assertArrayEquals(ROWS, response.getRows(), "Conserva las filas");
        assertEquals("OK. Query resolved.", response.getMessage(), "Conserva el mensaje");
    }

    @Test
    @DisplayName("Una respuesta de error no queda marcada como exitosa")
    void errorNoQuedaMarcadaComoExitosa() {
        QueryResponse response = QueryResponse.error("Error: invalid input.");
        assertFalse(response.isOk());
    }

    @Test
    @DisplayName("Una respuesta de error no trae datos tabulares")
    void errorNoTraeDatos() {
        QueryResponse response = QueryResponse.error("Error: invalid input.");

        assertNull(response.getHeaders(), "Sin encabezados");
        assertNull(response.getRows(), "Sin filas");
    }

    @Test
    @DisplayName("Una respuesta de error conserva el mensaje del fallo")
    void errorConservaElMensaje() {
        QueryResponse response = QueryResponse.error("Error: invalid input.");
        assertEquals("Error: invalid input.", response.getMessage());
    }

    @Test
    @DisplayName("Una respuesta ok puede venir sin filas")
    void okAceptaResultadoVacio() {
        QueryResponse response = QueryResponse.ok(HEADERS, new String[0][], "OK. Sin resultados.");

        assertTrue(response.isOk());
        assertEquals(0, response.getRows().length, "La consulta no devolvio filas");
    }
}
