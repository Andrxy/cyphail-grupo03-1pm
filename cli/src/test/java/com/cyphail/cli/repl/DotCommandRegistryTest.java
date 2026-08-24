package com.cyphail.cli.repl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas del dispatcher de comandos punto")
public class DotCommandRegistryTest {

    @Test
    @DisplayName("Un comando desconocido reporta error en ingles y no detiene el REPL")
    void testUnknownCommandReportsError() {
        DotCommandRegistry registry = new DotCommandRegistry();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);

        boolean shouldContinue = registry.dispatch(".foo", out);

        assertTrue(shouldContinue, "Un comando desconocido no debe tumbar el REPL");
        assertTrue(buffer.toString().toLowerCase().contains("unknown command"),
                "Debe reportar el comando como desconocido, en ingles");
    }

    @Test
    @DisplayName("El dispatch es insensible a mayusculas/minusculas")
    void testDispatchIsCaseInsensitive() {
        DotCommandRegistry registry = new DotCommandRegistry();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);

        boolean shouldContinue = registry.dispatch(".EXIT", out);

        assertFalse(shouldContinue, "\".EXIT\" en mayusculas debe reconocerse igual que \".exit\"");
    }

    @Test
    @DisplayName("dispatch separa correctamente el nombre del comando de sus argumentos")
    void testDispatchSeparatesArgsFromName() {
        DotCommandRegistry registry = new DotCommandRegistry();
        final String[] capturedArgs = new String[1];

        registry.register(new DotCommand() {
            @Override
            public String name() {
                return "echo";
            }

            @Override
            public String description() {
                return "Test command used only in this unit test";
            }

            @Override
            public boolean execute(String[] args, PrintStream out) {
                capturedArgs[0] = String.join(",", args);
                return true;
            }
        });

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);
        registry.dispatch(".echo amigos", out);

        assertTrue(capturedArgs[0].equals("amigos"),
                "Los argumentos despues del nombre del comando deben llegar separados correctamente");
    }
}
