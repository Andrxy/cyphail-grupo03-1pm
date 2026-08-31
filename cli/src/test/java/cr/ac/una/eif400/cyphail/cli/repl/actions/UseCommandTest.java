package cr.ac.una.eif400.cyphail.cli.repl.actions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comando ".use" en sus dos formas: sin argumento lista los
 * grafos disponibles, con argumento selecciona uno.
 *
 * @author Sebastian Gonzalez y Alejandro Miranda - Grupo 03-1pm
 * @version 1.1
 */
@DisplayName("Pruebas del comando .use")
public class UseCommandTest extends CommandTestSupport {

    @Test
    @DisplayName("Sin argumento termina con codigo 0")
    void sinArgumentoTerminaConExito() {
        assertEquals(0, ejecutar(".use"));
    }

    @Test
    @DisplayName("Sin argumento muestra el encabezado de la tabla")
    void sinArgumentoMuestraEncabezado() {
        ejecutar(".use");
        assertTrue(salida().contains("Graph"), "La tabla tiene columna Graph");
        assertTrue(salida().contains("Description"), "La tabla tiene columna Description");
    }

    @Test
    @DisplayName("Sin argumento lista todos los grafos disponibles")
    void sinArgumentoListaLosGrafos() {
        ejecutar(".use");
        assertTrue(salida().contains("Amigos"), "Lista Amigos");
        assertTrue(salida().contains("Tasks"), "Lista Tasks");
        assertTrue(salida().contains("teams"), "Lista teams");
        assertTrue(salida().contains("planets"), "Lista planets");
    }

    @Test
    @DisplayName("Sin argumento no selecciona ningun grafo")
    void sinArgumentoNoSeleccionaGrafo() {
        ejecutar(".use");
        assertNull(repl.getBroker().getCurrentGraph(), "El grafo actual sigue sin definirse");
    }

    @Test
    @DisplayName("Con un grafo existente confirma la seleccion")
    void conGrafoExistenteConfirma() {
        assertEquals(0, ejecutar(".use", "Amigos"));
        assertTrue(salida().contains("OK"), "Confirma con OK");
        assertTrue(salida().contains("Amigos"), "Nombra el grafo elegido");
        assertTrue(salida().contains("graph available"), "Indica que el grafo esta disponible");
    }

    @Test
    @DisplayName("Con un grafo existente actualiza el grafo actual del broker")
    void conGrafoExistenteActualizaElGrafoActual() {
        ejecutar(".use", "Amigos");
        assertEquals("Amigos", repl.getBroker().getCurrentGraph());
    }

    @Test
    @DisplayName("Con un grafo inexistente reporta error")
    void conGrafoInexistenteReportaError() {
        ejecutar(".use", "NoExiste");
        assertTrue(salida().contains("Error"), "Informa que el grafo no esta en la lista");
        assertTrue(salida().contains("NoExiste"), "Nombra el grafo pedido");
    }

    @Test
    @DisplayName("Con un grafo inexistente no cambia el grafo actual")
    void conGrafoInexistenteNoCambiaElGrafoActual() {
        ejecutar(".use", "NoExiste");
        assertNull(repl.getBroker().getCurrentGraph(), "El grafo actual no cambia");
    }
}
