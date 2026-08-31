package cr.ac.una.eif400.cyphail.cli.commands;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas del comando raiz del CLI de Cyphail.
 *
 * @author Andrey Solano - Grupo 03-1pm
 * @version 1.0
 */
@DisplayName("Pruebas de la CLI")
public class CyphailCLITest {
    @Test
    void testCyphailCommand() {
        CyphailCommand cyphail = new CyphailCommand();
        CommandLine cmd = new CommandLine(cyphail);

        int exitCode = cmd.execute();
        assertEquals(0, exitCode, "Comando ejecutado con codigo 0");
    }
}
