package com.cyphail.cli.repl.actions;

import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author Sebastian Gonzalez y Alejandro Miranda - Grupo 03-1pm
 * @version 1.1
 */
abstract class CommandTestSupport {

    private PrintStream originalOut;
    private ByteArrayOutputStream captured;

    protected Repl repl;
    protected CommandLine commandLine;

    @BeforeEach
    void prepararRepl() {
        originalOut = System.out;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));

        Broker.getInstance().setCurrentGraph(null);
        repl = new Repl();
        commandLine = new CommandLine(repl);
    }

    @AfterEach
    void restaurarSalida() {
        System.setOut(originalOut);
        Broker.getInstance().setCurrentGraph(null);
    }

    protected int ejecutar(String... args) {
        return commandLine.execute(args);
    }

    protected String salida() {
        return captured.toString();
    }
}
