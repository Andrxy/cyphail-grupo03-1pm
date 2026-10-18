package cr.ac.una.eif400.broker.fake;

import cr.ac.una.eif400.broker.model.Graph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas de FakeGraphs")
public class FakeGraphsTest {

    @Test
    @DisplayName("Las cabeceras son Graph y Description")
    void lasCabecerasSonLasEsperadas() {
        assertArrayEquals(new String[]{"Graph", "Description"}, FakeGraphs.headers);
    }

    @Test
    @DisplayName("El catalogo trae al menos un grafo")
    void elCatalogoTraeAlMenosUnGrafo() {
        assertTrue(FakeGraphs.getAvailableGraphs().length > 0);
    }

    @Test
    @DisplayName("Cada grafo tiene nombre y descripcion")
    void cadaGrafoTieneNombreYDescripcion() {
        for (Graph graph : FakeGraphs.getAvailableGraphs()) {
            assertFalse(graph.id().isBlank());
            assertFalse(graph.description().isBlank());
        }
    }

    @Test
    @DisplayName("toRows refleja cada grafo del catalogo")
    void toRowsReflejaCadaGrafoDelCatalogo() {
        Graph[] graphs = FakeGraphs.getAvailableGraphs();
        String[][] rows = FakeGraphs.toRows();

        assertEquals(graphs.length, rows.length);
        for (int i = 0; i < graphs.length; i++) {
            assertArrayEquals(new String[]{graphs[i].id(), graphs[i].description()}, rows[i]);
        }
    }

    @Test
    @DisplayName("Reconoce cada grafo del catalogo")
    void reconoceCadaGrafoDelCatalogo() {
        for (Graph graph : FakeGraphs.getAvailableGraphs()) {
            assertTrue(FakeGraphs.exists(graph.id()));
        }
    }

    @Test
    @DisplayName("La busqueda ignora mayusculas y minusculas")
    void laBusquedaIgnoraMayusculas() {
        for (Graph graph : FakeGraphs.getAvailableGraphs()) {
            assertTrue(FakeGraphs.exists(graph.id().toLowerCase()));
            assertTrue(FakeGraphs.exists(graph.id().toUpperCase()));
        }
    }

    @Test
    @DisplayName("No reconoce un grafo fuera del catalogo")
    void noReconoceUnGrafoDesconocido() {
        String desconocido = FixtureLoader.unknownGraphName(FakeGraphs.getAvailableGraphs());

        assertFalse(FakeGraphs.exists(desconocido));
    }

    @Test
    @DisplayName("No reconoce un nombre vacio")
    void noReconoceUnNombreVacio() {
        assertFalse(FakeGraphs.exists(""));
    }
}
