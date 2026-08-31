package cr.ac.una.eif400.cyphail.cli.commands;

import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.compiler.support.FlowLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    private static final String LOG_FILE_NAME = "cyphail-flow.log";

    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream captured;
    private String registroPrevio;

    @BeforeEach
    void prepararEntorno() throws IOException {
        originalOut = System.out;
        originalIn = System.in;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        System.setIn(new ByteArrayInputStream(".exit\n".getBytes(StandardCharsets.UTF_8)));

        registroPrevio = Files.exists(archivoDeRegistro()) ? Files.readString(archivoDeRegistro()) : null;

        Broker.getInstance().setCurrentGraph(null);
    }

    @AfterEach
    void restaurarEntorno() throws IOException {
        System.setOut(originalOut);
        System.setIn(originalIn);
        Broker.getInstance().setCurrentGraph(null);

        if (registroPrevio == null) {
            Files.deleteIfExists(archivoDeRegistro());
        } else {
            Files.writeString(archivoDeRegistro(), registroPrevio);
        }
    }

    @Test
    @DisplayName("Abre el shell y termina con codigo 0 al recibir .exit")
    void abreElShellYTerminaConExito() {
        int exitCode = new CommandLine(new CyphailCommand()).execute("repl");

        assertEquals(0, exitCode);
        assertTrue(captured.toString().contains("Welcome to Cyphail-03-1pm"), "Abrio el shell");
        assertTrue(captured.toString().contains("Bye!"), "Cerro la sesion con .exit");
    }

    /**
     * Ubica el registro del flujo donde lo deja FlowLog, que lo reinicia cada
     * vez que se abre el repl.
     */
    private static Path archivoDeRegistro() {
        try {
            Path ubicacion = Path.of(FlowLog.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path raiz = ubicacion.getParent().getParent().getParent();

            return ((raiz != null && Files.exists(raiz)) ? raiz : Path.of("").toAbsolutePath()).resolve(LOG_FILE_NAME);
        } catch (URISyntaxException | NullPointerException e) {
            return Path.of("").toAbsolutePath().resolve(LOG_FILE_NAME);
        }
    }
}
