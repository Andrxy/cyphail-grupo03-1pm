package cr.ac.una.eif400.broker;

import cr.ac.una.eif400.broker.fake.FakeGraphs;
import cr.ac.una.eif400.broker.fake.FakeQueries;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del broker: instancia unica, catalogo de grafos, grafo activo de la
 * sesion y resolucion de consultas.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas del Broker")
public class BrokerTest {

    private Broker broker;

    @BeforeEach
    void prepararBroker() {
        broker = Broker.getInstance();
        broker.setCurrentGraph(null);
    }

    @AfterEach
    void limpiarGrafoActual() {
        broker.setCurrentGraph(null);
    }

    @Test
    @DisplayName("getInstance siempre devuelve la misma instancia")
    void getInstanceDevuelveLaMismaInstancia() {
        assertSame(Broker.getInstance(), Broker.getInstance());
    }

    @Test
    @DisplayName("Expone el catalogo de grafos disponibles")
    void exponeElCatalogoDeGrafos() {
        assertArrayEquals(FakeGraphs.availableGraphs, broker.getAvailableGraphs());
    }

    @Test
    @DisplayName("Expone las cabeceras del catalogo")
    void exponeLasCabecerasDelCatalogo() {
        assertArrayEquals(FakeGraphs.headers, broker.getGraphHeaders());
    }

    @Test
    @DisplayName("Confirma la existencia de un grafo del catalogo")
    void confirmaUnGrafoDelCatalogo() {
        assertTrue(broker.graphExists("Amigos"));
        assertTrue(broker.graphExists("amigos"), "Sin importar mayusculas");
    }

    @Test
    @DisplayName("Niega la existencia de un grafo fuera del catalogo")
    void niegaUnGrafoDesconocido() {
        assertFalse(broker.graphExists("NoExiste"));
    }

    @Test
    @DisplayName("La sesion arranca sin grafo activo")
    void laSesionArrancaSinGrafoActivo() {
        assertNull(broker.getCurrentGraph());
    }

    @Test
    @DisplayName("Recuerda el grafo activo de la sesion")
    void recuerdaElGrafoActivo() {
        broker.setCurrentGraph("Amigos");
        assertEquals("Amigos", broker.getCurrentGraph());
    }

    @Test
    @DisplayName("Cambiar de grafo reemplaza el anterior")
    void cambiarDeGrafoReemplazaElAnterior() {
        broker.setCurrentGraph("Amigos");
        broker.setCurrentGraph("planets");

        assertEquals("planets", broker.getCurrentGraph());
    }

    @Test
    @DisplayName("Resuelve una consulta conocida")
    void resuelveUnaConsultaConocida() {
        QueryResponse response = broker.resolveQuery("MATCH (t:Tarea) RETURN t.titulo, t.estado");

        assertTrue(response.isOk());
        assertArrayEquals(new String[]{"t.titulo", "t.estado"}, response.getHeaders());
    }

    @Test
    @DisplayName("Reporta error ante una consulta desconocida")
    void reportaErrorAnteConsultaDesconocida() {
        QueryResponse response = broker.resolveQuery("MATCH (x:Desconocido) RETURN x");

        assertFalse(response.isOk());
        assertEquals(FakeQueries.NOT_RESOLVED_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Resolver una consulta no cambia el grafo activo")
    void resolverNoCambiaElGrafoActivo() {
        broker.setCurrentGraph("Amigos");
        broker.resolveQuery("MATCH (t:Tarea) RETURN t.titulo, t.estado");

        assertEquals("Amigos", broker.getCurrentGraph());
    }
}
