package cr.ac.una.eif400.cyphail.cli.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas del subcomando "run" del CLI de Cyphail.
 *
 * @author Andrey Solano - Grupo 03-1pm
 * @version 1.1
 */
@DisplayName("Pruebas del subcomando run")
public class RunCommandTest {
    private CommandLine cmd;

    @TempDir
    private Path carpeta;

    @Test
    void testSinPath() {
        cmd = new CommandLine(new CyphailCommand());
        int exitCode = cmd.execute("run");
        assertEquals(2, exitCode);
    }

    @Test
    @DisplayName("Con un archivo existente termina con codigo 0")
    void conArchivoExistenteTerminaConExito() throws IOException {
        Path fuente = Files.writeString(
                carpeta.resolve("consulta.cyphail"),
                "MATCH (t:Tarea) RETURN t.titulo, t.estado");
        cmd = new CommandLine(new CyphailCommand());

        assertEquals(0, cmd.execute("run", fuente.toString()));
    }

    @Test
    @DisplayName("Con un archivo inexistente termina con codigo 1")
    void conArchivoInexistenteReportaError() {
        Path fuente = carpeta.resolve("noExiste.cyphail");
        cmd = new CommandLine(new CyphailCommand());

        assertEquals(1, cmd.execute("run", fuente.toString()));
    }
}
