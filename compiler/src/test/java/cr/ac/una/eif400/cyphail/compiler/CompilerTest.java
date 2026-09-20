package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.broker.fake.FakeQueries;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del compilador: instancia unica y recorrido del pipeline hasta el
 * broker.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas del Compiler")
public class CompilerTest {

    private static final String TAREAS = "MATCH (t:Tarea) RETURN t.titulo, t.estado";

    private final Compiler compiler = Compiler.getInstance();

    @Test
    @DisplayName("getInstance siempre devuelve la misma instancia")
    void getInstanceDevuelveLaMismaInstancia() {
        assertSame(Compiler.getInstance(), Compiler.getInstance());
    }

    @Test
    @DisplayName("Compila una consulta conocida y devuelve sus datos")
    void compilaUnaConsultaConocida() {
        QueryResponse response = compiler.compile(TAREAS);

        assertTrue(response.isOk());
        assertArrayEquals(new String[]{"t.titulo", "t.estado"}, response.getHeaders());
        assertEquals(4, response.getRows().length, "Devuelve las cuatro tareas");
    }

    @Test
    @DisplayName("Una consulta que el broker no resuelve devuelve error")
    void unaConsultaSinRespuestaDevuelveError() {
        QueryResponse response = compiler.compile("MATCH (x:Desconocido) RETURN x");

        assertFalse(response.isOk());
        assertEquals(FakeQueries.NOT_RESOLVED_MESSAGE, response.getMessage());
    }

    @Test
    @DisplayName("El codigo fuente llega al broker tal como se escribio")
    void elCodigoFuenteLlegaAlBroker() {
        QueryResponse response = compiler.compile("   " + TAREAS + "   ");

        assertTrue(response.isOk(), "El broker recorta los espacios y resuelve igual");
    }
}