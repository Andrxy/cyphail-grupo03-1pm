package cr.ac.una.eif400.cyphail.cli.repl;

import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del bucle del REPL: banner, prompt, enrutamiento de la linea escrita
 * y ejecucion de consultas.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas del Repl")
public class ReplTest {

    private static final String TAREAS = "MATCH (t:Tarea) RETURN t.titulo, t.estado";

    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream captured;

    private Repl repl;

    @BeforeEach
    void prepararRepl() {
        originalOut = System.out;
        originalIn = System.in;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));

        Broker.getInstance().setCurrentGraph(null);
        repl = new Repl(Compiler.getInstance(), Broker.getInstance());
    }

    @AfterEach
    void restaurarEntorno() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        Broker.getInstance().setCurrentGraph(null);
    }

    @Test
    @DisplayName("Arranca activo y guarda el broker recibido")
    void arrancaActivoConSuBroker() {
        assertTrue(repl.isRunning(), "El bucle arranca activo");
        assertSame(Broker.getInstance(), repl.getBroker(), "Conserva el broker recibido");
    }

    @Test
    @DisplayName("stop marca el bucle como terminado")
    void stopDetieneElBucle() {
        repl.stop();
        assertFalse(repl.isRunning());
    }

    @Test
    @DisplayName("Da la bienvenida y muestra el prompt")
    void daLaBienvenidaYMuestraElPrompt() {
        escribir(".exit");
        repl.open();

        assertTrue(salida().contains("Welcome to Cyphail-03-1pm"), "Muestra el banner");
        assertTrue(salida().contains("Type \".help\""), "Explica como pedir ayuda");
        assertTrue(salida().contains("cyphail>>> "), "Muestra el prompt sin grafo activo");
    }

    @Test
    @DisplayName("El prompt muestra el grafo activo despues de un .use")
    void elPromptMuestraElGrafoActivo() {
        escribir(".use Amigos", ".exit");
        repl.open();

        assertTrue(salida().contains("cyphail (Amigos) >>> "), "El prompt incluye el grafo elegido");
    }

    @Test
    @DisplayName("Un comando punto conocido se ejecuta")
    void ejecutaUnComandoPuntoConocido() {
        escribir(".about", ".exit");
        repl.open();

        assertTrue(salida().contains("What is cyphail?"), "Corrio el comando .about");
        assertFalse(repl.isRunning(), "El .exit final cerro el bucle");
    }

    @Test
    @DisplayName("Un comando punto desconocido se reporta como tal")
    void avisaDeUnComandoPuntoDesconocido() {
        escribir(".nope", ".exit");
        repl.open();

        assertTrue(salida().contains("Unknown command '.nope'"), "Nombra el comando desconocido");
        assertTrue(salida().contains(".help"), "Sugiere pedir ayuda");
    }

    @Test
    @DisplayName("Las lineas vacias se ignoran sin reportar error")
    void ignoraLasLineasVacias() {
        escribir("", "   ", ".exit");
        repl.open();

        assertFalse(salida().contains("Unknown command"), "No las trata como comando");
        assertFalse(salida().contains("Error"), "No las trata como consulta");
    }

    @Test
    @DisplayName("Una linea que no empieza con punto se compila como consulta")
    void compilaLaLineaComoConsulta() {
        escribir(TAREAS + ";", ".exit");
        repl.open();

        assertTrue(salida().contains("Escribir README"), "Muestra los datos de la consulta");
        assertTrue(salida().contains("t.titulo"), "Muestra los encabezados de la consulta");
    }

    @Test
    @DisplayName("Se cierra cuando la entrada se termina")
    void seCierraCuandoLaEntradaSeTermina() {
        escribir();
        repl.open();

        assertTrue(salida().contains("cyphail>>> "), "Alcanzo a pedir la primera linea");
    }

    @Test
    @DisplayName("Una consulta conocida imprime la tabla y el mensaje de exito")
    void unaConsultaConocidaImprimeLaTabla() {
        repl.executeQuery(TAREAS);

        assertTrue(salida().contains("Setup Maven"), "Muestra las filas del resultado");
        assertTrue(salida().contains("OK. Query resolved after 666 ms."), "Confirma la ejecucion");
    }

    @Test
    @DisplayName("Una consulta sin respuesta imprime el error y ninguna tabla")
    void unaConsultaSinRespuestaImprimeElError() {
        repl.executeQuery("MATCH (x:Desconocido) RETURN x");

        assertTrue(salida().contains("Error: invalid input."), "Explica que no se pudo resolver");
        assertFalse(salida().contains("|"), "No dibuja tabla alguna");
    }

    private void escribir(String... lineas) {
        String entrada = String.join(System.lineSeparator(), lineas);
        if (lineas.length > 0) {
            entrada += System.lineSeparator();
        }
        System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void acumulaConsultaMultilineaYDespachaComandosSinPuntoYComa() {
        List<String> queries = capturarConsultas();
        escribir(".about", "MATCH (n)", "", "RETURN n;", ".exit");
        repl.open();

        assertEquals(List.of("MATCH (n)\n\nRETURN n;"), queries);
        assertTrue(salida().contains("     ...> "));
        assertTrue(salida().contains("What is cyphail?"));
        assertFalse(repl.isRunning());
    }

    @Test
    void ejecutaVariasConsultasYConservaElFragmentoPendiente() {
        List<String> queries = capturarConsultas();
        escribir("RETURN 1; RETURN 2; MATCH (n)", "RETURN n;", ".exit");
        repl.open();

        assertEquals(List.of("RETURN 1; RETURN 2; MATCH (n)\nRETURN n;"), queries);
    }

    @Test
    void despachaLoLeidoAlLlegarAlFinDeEntrada() {
        List<String> queries = capturarConsultas();
        escribir("MATCH (n)", "RETURN n");
        repl.open();

        assertEquals(List.of("MATCH (n)\nRETURN n"), queries);
        assertFalse(salida().contains("Incomplete statement"));
    }

    @Test
    void muestraUnErrorLexicoSinEsperarMasLineas() {
        escribir("RETURN 'first", ".exit", "last'; // ;", ".exit");
        repl.open();

        assertTrue(salida().contains("Lexical Error"), salida());
        assertFalse(salida().contains("Incomplete statement"));
        assertFalse(repl.isRunning());
    }

    private List<String> capturarConsultas() {
        List<String> queries = new ArrayList<>();
        repl = new Repl(Compiler.getInstance(), Broker.getInstance()) {
            @Override
            public boolean executeQuery(String sourceCode) {
                queries.add(sourceCode);
                return true;
            }
        };
        return queries;
    }

    private String salida() {
        return captured.toString();
    }
}
