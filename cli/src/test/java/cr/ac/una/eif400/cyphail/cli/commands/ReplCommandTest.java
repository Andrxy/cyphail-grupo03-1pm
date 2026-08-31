package cr.ac.una.eif400.cyphail.cli.commands;

import cr.ac.una.eif400.broker.Broker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del subcomando "repl", que abre el shell interactivo.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas del subcomando repl")
public class ReplCommandTest {

    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void prepararEntorno() {
        originalOut = System.out;
        originalIn = System.in;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        System.setIn(new ByteArrayInputStream(".exit\n".getBytes(StandardCharsets.UTF_8)));

        Broker.getInstance().setCurrentGraph(null);
    }

    @AfterEach
    void restaurarEntorno() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        Broker.getInstance().setCurrentGraph(null);
    }

    @Test
    @DisplayName("Abre el shell y termina con codigo 0 al recibir .exit")
    void abreElShellYTerminaConExito() {
        int exitCode = new CommandLine(new CyphailCommand()).execute("repl");

        assertEquals(0, exitCode);
        assertTrue(captured.toString().contains("Welcome to Cyphail-03-1pm"), "Abrio el shell");
        assertTrue(captured.toString().contains("Bye!"), "Cerro la sesion con .exit");
    }
}
