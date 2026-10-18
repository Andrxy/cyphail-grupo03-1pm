package cr.ac.una.eif400.broker;

import cr.ac.una.eif400.broker.fake.FakeGraphs;
import cr.ac.una.eif400.broker.fake.FakeQueries;
import cr.ac.una.eif400.broker.fake.FixtureLoader;
import cr.ac.una.eif400.broker.model.QueryEntry;
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
        assertArrayEquals(FakeGraphs.toRows(), broker.getAvailableGraphs());
    }

    @Test
    @DisplayName("Expone las cabeceras del catalogo")
    void exponeLasCabecerasDelCatalogo() {
        assertArrayEquals(FakeGraphs.headers, broker.getGraphHeaders());
    }

    @Test
    @DisplayName("Confirma la existencia de un grafo del catalogo")
    void confirmaUnGrafoDelCatalogo() {
        String nombre = FakeGraphs.getAvailableGraphs()[0].id();

        assertTrue(broker.graphExists(nombre));
        assertTrue(broker.graphExists(nombre.toLowerCase()), "Sin importar mayusculas");
    }

    @Test
    @DisplayName("Niega la existencia de un grafo fuera del catalogo")
    void niegaUnGrafoDesconocido() {
        String desconocido = FixtureLoader.unknownGraphName(FakeGraphs.getAvailableGraphs());

        assertFalse(broker.graphExists(desconocido));
    }

    @Test
    @DisplayName("La sesion arranca sin grafo activo")
    void laSesionArrancaSinGrafoActivo() {
        assertNull(broker.getCurrentGraph());
    }

    @Test
    @DisplayName("Recuerda el grafo activo de la sesion")
    void recuerdaElGrafoActivo() {
        broker.setCurrentGraph("sesion-grafo-1");
        assertEquals("sesion-grafo-1", broker.getCurrentGraph());
    }

    @Test
    @DisplayName("Cambiar de grafo reemplaza el anterior")
    void cambiarDeGrafoReemplazaElAnterior() {
        broker.setCurrentGraph("sesion-grafo-1");
        broker.setCurrentGraph("sesion-grafo-2");

        assertEquals("sesion-grafo-2", broker.getCurrentGraph());
    }

    @Test
    @DisplayName("Resuelve una consulta conocida")
    void resuelveUnaConsultaConocida() {
        QueryEntry entrada = FixtureLoader.loadQueries()[0];
        QueryResponse response = broker.resolveQuery(entrada.query());

        assertTrue(response.isOk());
        assertArrayEquals(entrada.headers(), response.getHeaders());
    }

    @Test
    @DisplayName("Reporta error ante una consulta desconocida")
    void reportaErrorAnteConsultaDesconocida() {
        String desconocida = FixtureLoader.unknownQuery(FixtureLoader.loadQueries());
        QueryResponse response = broker.resolveQuery(desconocida);

        assertFalse(response.isOk());
        assertEquals(FakeQueries.NOT_RESOLVED_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Resolver una consulta no cambia el grafo activo")
    void resolverNoCambiaElGrafoActivo() {
        QueryEntry entrada = FixtureLoader.loadQueries()[0];
        broker.setCurrentGraph("sesion-grafo-1");
        broker.resolveQuery(entrada.query());

        assertEquals("sesion-grafo-1", broker.getCurrentGraph());
    }
}
