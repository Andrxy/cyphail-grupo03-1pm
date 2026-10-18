package cr.ac.una.eif400.broker.fake;

import cr.ac.una.eif400.broker.model.QueryEntry;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas de FakeQueries")
public class FakeQueriesTest {

    private static QueryEntry[] entradas;

    @BeforeAll
    static void cargarEntradas() {
        entradas = FixtureLoader.loadQueries();
    }

    @Test
    @DisplayName("Una consulta quemada se resuelve con exito")
    void unaConsultaQuemadaSeResuelve() {
        QueryEntry entrada = entradas[0];
        QueryResponse response = FakeQueries.resolve(entrada.query());

        assertTrue(response.isOk());
        assertEquals(FakeQueries.OK_RESOLVED_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Una consulta quemada devuelve sus encabezados y filas")
    void unaConsultaQuemadaDevuelveSusDatos() {
        QueryEntry entrada = entradas[0];
        QueryResponse response = FakeQueries.resolve(entrada.query());

        assertArrayEquals(entrada.headers(), response.getHeaders());
        assertEquals(entrada.rows().length, response.getRows().length);
        assertArrayEquals(entrada.rows()[0], response.getRows()[0]);
    }

    @Test
    @DisplayName("Cada fila trae tantas columnas como encabezados")
    void cadaFilaCalzaConLosEncabezados() {
        for (QueryEntry entrada : entradas) {
            QueryResponse response = FakeQueries.resolve(entrada.query());

            for (String[] row : response.getRows()) {
                assertEquals(response.getHeaders().length, row.length);
            }
        }
    }

    @Test
    @DisplayName("Los espacios alrededor de la consulta no afectan la busqueda")
    void ignoraLosEspaciosAlrededor() {
        String query = entradas[0].query();
        QueryResponse response = FakeQueries.resolve("   " + query + "  ");

        assertTrue(response.isOk());
    }

    @Test
    @DisplayName("Una consulta desconocida devuelve error")
    void unaConsultaDesconocidaDevuelveError() {
        String desconocida = FixtureLoader.unknownQuery(entradas);
        QueryResponse response = FakeQueries.resolve(desconocida);

        assertFalse(response.isOk());
        assertEquals(FakeQueries.NOT_RESOLVED_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Una consulta desconocida no trae datos tabulares")
    void unaConsultaDesconocidaNoTraeDatos() {
        String desconocida = FixtureLoader.unknownQuery(entradas);
        QueryResponse response = FakeQueries.resolve(desconocida);

        assertNull(response.getHeaders());
        assertNull(response.getRows());
    }

    @Test
    @DisplayName("La busqueda distingue mayusculas y minusculas")
    void laBusquedaDistingueMayusculas() {
        String query = entradas[0].query();
        String queryEnMinusculas = query.toLowerCase();
        Assumptions.assumeTrue(!query.equals(queryEnMinusculas));

        QueryResponse response = FakeQueries.resolve(queryEnMinusculas);

        assertFalse(response.isOk());
    }
}
