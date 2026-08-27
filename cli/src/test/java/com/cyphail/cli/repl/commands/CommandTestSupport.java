package com.cyphail.cli.repl.commands;

import cr.ac.una.eif400.cyphail.cli.commands.ReplCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Base de las pruebas de los comandos punto del REPL.
 *
 * Los comandos escriben con IO.println, que resuelve System.out en cada
 * llamada, de modo que redirigir la salida estandar alcanza para capturarlos.
 * Cada comando se ejecuta a traves de ReplCommand y no de forma aislada,
 * porque de ahi obtienen el padre y la sesion que necesitan.
 *
 * @author Sebastian Gonzalez y Alejandro Miranda - Grupo 03-1pm
 * @version 1.0
 */
abstract class CommandTestSupport {

    private PrintStream originalOut;
    private ByteArrayOutputStream captured;

    protected ReplCommand repl;
    protected CommandLine commandLine;

    @BeforeEach
    void redirigirSalida() {
        originalOut = System.out;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));

        repl = new ReplCommand();
        commandLine = new CommandLine(repl);
    }

    @AfterEach
    void restaurarSalida() {
        System.setOut(originalOut);
    }

    /**
     * @param args linea ya separada en tokens, como la envia el ciclo del REPL
     * @return codigo de salida que devolvio el comando
     */
    protected int ejecutar(String... args) {
        return commandLine.execute(args);
    }

    /**
     * @return todo lo que el comando escribio en la salida estandar
     */
    protected String salida() {
        return captured.toString();
    }
}
