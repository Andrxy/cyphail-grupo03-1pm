package com.cyphail.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RunCommandTest {
    private CommandLine cmd;

    @Test
    void testSinPath() {
        cmd = new CommandLine(new CyphailCommand());
        int exitCode = cmd.execute("run");
        assertEquals(2, exitCode);
    }
}
