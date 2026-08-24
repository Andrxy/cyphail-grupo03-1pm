package com.cyphail.cli.repl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas del comando .exit")
public class ExitCommandTest {

    @Test
    @DisplayName(".exit debe detener el loop del REPL")
    void testExitStopsTheReplLoop() {
        ExitCommand command = new ExitCommand();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);

        boolean shouldContinue = command.execute(new String[0], out);

        assertFalse(shouldContinue, "El comando .exit debe devolver false para detener el REPL");
    }

    @Test
    @DisplayName(".exit debe imprimir un mensaje de despedida")
    void testExitPrintsGoodbyeMessage() {
        ExitCommand command = new ExitCommand();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);

        command.execute(new String[0], out);

        assertTrue(buffer.toString().contains("Bye"), "El comando .exit debe imprimir un mensaje de despedida");
    }

    @Test
    @DisplayName("El nombre del comando debe ser exit")
    void testCommandName() {
        ExitCommand command = new ExitCommand();
        assertTrue(command.name().equals("exit"));
    }
}
