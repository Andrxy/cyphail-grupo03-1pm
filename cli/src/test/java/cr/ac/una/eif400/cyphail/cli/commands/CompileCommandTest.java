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
 * Pruebas del subcomando "compile" del CLI de Cyphail.
 *
 * @author Andrey Solano - Grupo 03-1pm
 * @version 1.1
 */
@DisplayName("Pruebas del subcomando compile")
public class CompileCommandTest {
    private CommandLine cmd;

    @TempDir
    private Path carpeta;

    @Test
    void testSinPath() {
        cmd = new CommandLine(new CyphailCommand());
        int exitCode = cmd.execute("compile");
        assertEquals(2, exitCode);
    }

    @Test
    @DisplayName("Con un archivo existente termina con codigo 0")
    void conArchivoExistenteTerminaConExito() throws IOException {
        Path fuente = crearFuente();
        cmd = new CommandLine(new CyphailCommand());

        assertEquals(0, cmd.execute("compile", fuente.toString()));
    }

    @Test
    @DisplayName("Acepta la ruta de salida con --out")
    void aceptaLaRutaDeSalida() throws IOException {
        Path fuente = crearFuente();
        Path salida = carpeta.resolve("consulta.pl");
        cmd = new CommandLine(new CyphailCommand());

        assertEquals(0, cmd.execute("compile", fuente.toString(), "--out", salida.toString()));
    }

    @Test
    @DisplayName("Con un archivo inexistente termina con codigo 1")
    void conArchivoInexistenteReportaError() {
        Path fuente = carpeta.resolve("noExiste.cyphail");
        cmd = new CommandLine(new CyphailCommand());

        assertEquals(1, cmd.execute("compile", fuente.toString()));
    }

    private Path crearFuente() throws IOException {
        return Files.writeString(
                carpeta.resolve("consulta.cyphail"),
                "MATCH (t:Tarea) RETURN t.titulo, t.estado");
    }
}
