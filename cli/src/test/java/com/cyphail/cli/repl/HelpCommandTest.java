package com.cyphail.cli.repl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas del comando .help")
public class HelpCommandTest {

    @Test
    @DisplayName(".help no debe detener el REPL")
    void testHelpDoesNotStopTheRepl() {
        DotCommandRegistry registry = new DotCommandRegistry();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);

        boolean shouldContinue = registry.dispatch(".help", out);

        assertTrue(shouldContinue, "El comando .help nunca debe detener el REPL");
    }

    @Test
    @DisplayName(".help debe listar los comandos ya registrados")
    void testHelpListsRegisteredCommands() {
        DotCommandRegistry registry = new DotCommandRegistry();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);

        registry.dispatch(".help", out);
        String output = buffer.toString();

        assertTrue(output.contains(".exit"), "La ayuda debe listar .exit");
        assertTrue(output.contains(".help"), "La ayuda debe listarse a si misma");
    }

    @Test
    @DisplayName(".help se actualiza solo cuando se registra un comando nuevo")
    void testHelpReflectsNewlyRegisteredCommands() {
        DotCommandRegistry registry = new DotCommandRegistry();
        registry.register(new DotCommand() {
            @Override
            public String name() {
                return "ping";
            }

            @Override
            public String description() {
                return "Test command used only in this unit test";
            }

            @Override
            public boolean execute(String[] args, PrintStream out) {
                out.println("pong");
                return true;
            }
        });

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);
        registry.dispatch(".help", out);

        assertTrue(buffer.toString().contains(".ping"),
                "La ayuda debe incluir comandos registrados despues de crear el registry, sin tocar HelpCommand");
    }
}
