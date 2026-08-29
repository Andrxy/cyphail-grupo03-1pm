package com.cyphail.cli;

import cr.ac.una.eif400.cyphail.cli.commands.CyphailCommand;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas del subcomando "run" del CLI de Cyphail.
 *
 * @author Andrey Solano - Grupo 03-1pm
 * @version 1.0
 */
public class RunCommandTest {
    private CommandLine cmd;

    @Test
    void testSinPath() {
        cmd = new CommandLine(new CyphailCommand());
        int exitCode = cmd.execute("run");
        assertEquals(2, exitCode);
    }
}
