package cr.ac.una.eif400.broker.fake;

import cr.ac.una.eif400.cyphail.common.QueryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del conjunto de consultas quemadas que responde el broker en el sprint P1.1.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas de FakeQueries")
public class FakeQueriesTest {

    private static final String PERSONAS = "MATCH (p:Persona) RETURN p.nombre, p.edad";
    private static final String TAREAS = "MATCH (t:Tarea) RETURN t.titulo, t.estado";

    @Test
    @DisplayName("Una consulta quemada se resuelve con exito")
    void unaConsultaQuemadaSeResuelve() {
        QueryResponse response = FakeQueries.resolve(PERSONAS);

        assertTrue(response.isOk());
        assertEquals(FakeQueries.OK_RESOLVED_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Una consulta quemada devuelve sus encabezados y filas")
    void unaConsultaQuemadaDevuelveSusDatos() {
        QueryResponse response = FakeQueries.resolve(PERSONAS);

        assertArrayEquals(new String[]{"p.nombre", "p.edad"}, response.getHeaders());
        assertEquals(6, response.getRows().length, "Devuelve las seis personas");
        assertArrayEquals(new String[]{"Ana", "28"}, response.getRows()[0]);
    }

    @Test
    @DisplayName("Cada fila trae tantas columnas como encabezados")
    void cadaFilaCalzaConLosEncabezados() {
        QueryResponse response = FakeQueries.resolve(TAREAS);

        for (String[] row : response.getRows()) {
            assertEquals(response.getHeaders().length, row.length);
        }
    }

    @Test
    @DisplayName("Los espacios alrededor de la consulta no afectan la busqueda")
    void ignoraLosEspaciosAlrededor() {
        QueryResponse response = FakeQueries.resolve("   " + TAREAS + "  ");

        assertTrue(response.isOk(), "Resuelve igual que sin espacios");
    }

    @Test
    @DisplayName("Una consulta desconocida devuelve error")
    void unaConsultaDesconocidaDevuelveError() {
        QueryResponse response = FakeQueries.resolve("MATCH (x:Desconocido) RETURN x");

        assertFalse(response.isOk());
        assertEquals(FakeQueries.NOT_RESOLVED_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("Una consulta desconocida no trae datos tabulares")
    void unaConsultaDesconocidaNoTraeDatos() {
        QueryResponse response = FakeQueries.resolve("cualquier cosa");

        assertNull(response.getHeaders(), "Sin encabezados");
        assertNull(response.getRows(), "Sin filas");
    }

    @Test
    @DisplayName("La busqueda distingue mayusculas y minusculas")
    void laBusquedaDistingueMayusculas() {
        QueryResponse response = FakeQueries.resolve(PERSONAS.toLowerCase());

        assertFalse(response.isOk(), "La consulta debe escribirse tal cual esta quemada");
    }
}
