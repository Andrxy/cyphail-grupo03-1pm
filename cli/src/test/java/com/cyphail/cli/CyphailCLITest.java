package com.cyphail.cli;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Pruebas de la CLI")
public class CyphailCLITest {
    @Test
    void testCyphailCommand() {
        Cyphail cyphail = new Cyphail();
        CommandLine cmd = new CommandLine(cyphail);

        int exitCode = cmd.execute();
        assertEquals(0, exitCode, "Comando ejecutado con codigo 0");
    }
}
