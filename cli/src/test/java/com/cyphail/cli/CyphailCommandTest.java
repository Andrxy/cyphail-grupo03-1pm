package com.cyphail.cli;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CyphailCommandTest {
    @Test
    void testCyphailCommand() {
        CyphailCommand cyphail = new CyphailCommand();
        CommandLine cmd = new CommandLine(cyphail);

        int exitCode = cmd.execute();
        assertEquals(0, exitCode, "Comando ejecutado con codigo 0");
    }
}
