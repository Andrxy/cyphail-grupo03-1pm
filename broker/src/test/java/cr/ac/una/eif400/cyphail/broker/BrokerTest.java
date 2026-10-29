package cr.ac.una.eif400.cyphail.broker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
    @DisplayName("Expone las cabeceras del catalogo")
    void exponeLasCabecerasDelCatalogo() {
        assertArrayEquals(new String[]{"Graph", "Description"}, broker.getGraphHeaders());
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

}
