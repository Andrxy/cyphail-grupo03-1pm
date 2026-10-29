package cr.ac.una.eif400.cyphail.broker;

import cr.ac.una.eif400.cyphail.common.QueryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas del Broker contra el engine real por MQI. Se omiten si swipl no está instalado.
 *
 * <p>No dependen de los datos concretos de los grafos: solo de que exista al menos uno.</p>
 *
 * @author Sebastián González
 *
 * <p>Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 */
@DisplayName("Pruebas del Broker con MQI (necesitan swipl)")
@Timeout(value = 20, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class BrokerEngineTest {

    private final Broker broker = Broker.getInstance();

    @BeforeAll
    static void requiereSwipl() {
        assumeTrue(swiplInstalado(), "swipl no está en el PATH");
    }

    static boolean swiplInstalado() {
        try {
            return new ProcessBuilder("swipl", "--version").start().waitFor() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @AfterEach
    void limpiar() {
        broker.setCurrentGraph(null);
    }

    @Test
    @DisplayName("Lista los grafos del engine con cy_graphs")
    void listaLosGrafos() {
        String[][] graphs = broker.getAvailableGraphs();

        assertTrue(graphs.length > 0);
        assertEquals(2, graphs[0].length, "Cada grafo trae id y descripcion");
    }

    @Test
    @DisplayName("graphExists no distingue mayusculas")
    void graphExistsSinMayusculas() {
        String id = broker.getAvailableGraphs()[0][0];

        assertTrue(broker.graphExists(id.toUpperCase()));
        assertFalse(broker.graphExists("grafo-que-no-existe"));
    }

    @Test
    @DisplayName("Una consulta devuelve encabezados, filas y el tiempo")
    void consultaConTabla() {
        QueryResponse response = broker.resolveQuery("Rows = '\"x\" \"y\"\\n\"1\" \"dos\"'");

        assertTrue(response.isOk(), response.getMessage());
        assertArrayEquals(new String[]{"x", "y"}, response.getHeaders());
        assertArrayEquals(new String[][]{{"1", "dos"}}, response.getRows());
        assertTrue(response.getMessage().matches("OK\\. Query resolved after \\d+ ms\\."));
    }

    @Test
    @DisplayName("Usa el grafo activo para resolver")
    void usaElGrafoActivo() {
        String id = broker.getAvailableGraphs()[0][0];
        broker.setCurrentGraph(id.toUpperCase());

        QueryResponse response = broker.resolveQuery(
                "get_current_graph(G), format(atom(Rows), '\"g\"~n\"~w\"', [G])");

        assertTrue(response.isOk(), response.getMessage());
        assertEquals(id, response.getRows()[0][0]);
    }

    @Test
    @DisplayName("Sin grafo activo el engine responde con su cy_error")
    void sinGrafoActivo() {
        QueryResponse response = broker.resolveQuery("throw(cy_error('No graph selected'))");

        assertFalse(response.isOk());
        assertEquals("Error: No graph selected", response.getMessage());
    }

    @Test
    @DisplayName("Un goal que falla da el mensaje del contrato")
    void goalQueFalla() {
        QueryResponse response = broker.resolveQuery("fail");

        assertFalse(response.isOk());
        assertEquals(Broker.ENGINE_FAILED_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Un texto que no es Prolog da error y la sesion sigue viva")
    void textoQueNoEsProlog() {
        QueryResponse error = broker.resolveQuery("MATCH (p) RETURN p");
        QueryResponse despues = broker.resolveQuery("Rows = ''");

        assertFalse(error.isOk());
        assertTrue(error.getMessage().startsWith("Error: syntax_error"));
        assertTrue(despues.isOk());
    }

    @Test
    @DisplayName("Si el goal no deja Rows se avisa")
    void sinRows() {
        QueryResponse response = broker.resolveQuery("X = 1");

        assertFalse(response.isOk());
        assertEquals("Error: the engine did not return Rows", response.getMessage());
    }

    @Test
    @DisplayName("Despues de close el broker vuelve a arrancar swipl solo")
    void reiniciaDespuesDeClose() {
        broker.close();
        QueryResponse response = broker.resolveQuery("Rows = ''");

        assertTrue(response.isOk(), response.getMessage());
    }
}
