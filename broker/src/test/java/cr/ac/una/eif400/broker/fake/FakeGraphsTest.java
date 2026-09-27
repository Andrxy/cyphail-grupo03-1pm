package cr.ac.una.eif400.broker.fake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del catalogo de grafos quemados del sprint P1.1.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas de FakeGraphs")
public class FakeGraphsTest {

    @Test
    @DisplayName("Las cabeceras son Graph y Description")
    void lasCabecerasSonLasEsperadas() {
        assertArrayEquals(new String[]{"Graph", "Description"}, FakeGraphs.headers);
    }

    @Test
    @DisplayName("El catalogo trae los cinco grafos del sprint")
    void elCatalogoTraeLosCincoGrafos() {
        assertEquals(5, FakeGraphs.getAvailableGraphs().length);
    }

    @Test
    @DisplayName("Cada grafo tiene nombre y descripcion")
    void cadaGrafoTieneNombreYDescripcion() {
        for (String[] graph : FakeGraphs.toRows()) {
            assertEquals(2, graph.length, "El grafo tiene tantas columnas como cabeceras");
        }
    }

    @Test
    @DisplayName("Reconoce un grafo del catalogo")
    void reconoceUnGrafoDelCatalogo() {
        assertTrue(FakeGraphs.exists("Amigos"));
        assertTrue(FakeGraphs.exists("Tasks"));
        assertTrue(FakeGraphs.exists("teams"));
        assertTrue(FakeGraphs.exists("planets"));
    }

    @Test
    @DisplayName("La busqueda ignora mayusculas y minusculas")
    void laBusquedaIgnoraMayusculas() {
        assertTrue(FakeGraphs.exists("amigos"), "Busca en minuscula");
        assertTrue(FakeGraphs.exists("TEAMS"), "Busca en mayuscula");
    }

    @Test
    @DisplayName("No reconoce un grafo fuera del catalogo")
    void noReconoceUnGrafoDesconocido() {
        assertFalse(FakeGraphs.exists("NoExiste"));
    }

    @Test
    @DisplayName("No reconoce un nombre vacio")
    void noReconoceUnNombreVacio() {
        assertFalse(FakeGraphs.exists(""));
    }
}
