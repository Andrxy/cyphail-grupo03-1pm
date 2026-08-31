package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.broker.fake.FakeQueries;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.support.FlowLogTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del compilador: instancia unica, recorrido del pipeline hasta el
 * broker y registro del flujo.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas del Compiler")
public class CompilerTest extends FlowLogTestSupport {

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

    @Test
    @DisplayName("Deja registradas las cuatro etapas del pipeline")
    void registraLasEtapasDelPipeline() throws IOException {
        compiler.compile(TAREAS);
        String registro = registro();

        assertTrue(registro.contains("[Tokenizer]"), "Registra la tokenizacion");
        assertTrue(registro.contains("[Parser]"), "Registra el parseo");
        assertTrue(registro.contains("[Analisis Semantico]"), "Registra el analisis semantico");
        assertTrue(registro.contains("[Generacion de codigo]"), "Registra la generacion de codigo");
        assertTrue(registro.contains("[Broker]"), "Registra el envio al broker");
    }

    @Test
    @DisplayName("Cada compilacion agrega su propio bloque al registro")
    void cadaCompilacionAgregaSuBloque() throws IOException {
        compiler.compile(TAREAS);
        int etapasDeUnaCorrida = contarApariciones(registro(), "[Tokenizer]");

        compiler.compile(TAREAS);
        int etapasDeDosCorridas = contarApariciones(registro(), "[Tokenizer]");

        assertEquals(etapasDeUnaCorrida * 2, etapasDeDosCorridas);
    }

    private int contarApariciones(String texto, String buscado) {
        int total = 0;
        int desde = texto.indexOf(buscado);

        while (desde >= 0) {
            total++;
            desde = texto.indexOf(buscado, desde + buscado.length());
        }
        return total;
    }
}
