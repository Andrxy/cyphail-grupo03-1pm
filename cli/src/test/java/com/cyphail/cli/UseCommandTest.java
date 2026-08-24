package com.cyphail.cli;

import org.junit.jupiter.api.*;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class UseCommandTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void sinArgumento_muestraListaDeGrafos() {
        CommandLine cmd = new CommandLine(new UseCommand());

        int exitCode = cmd.execute();

        assertEquals(0, exitCode);
        assertTrue(outContent.toString().contains("Graph"));
        assertTrue(outContent.toString().contains("Amigos"));
    }

    @Test
    void grafoValido_conectaYRetornaCeroP() {
        CommandLine cmd = new CommandLine(new UseCommand());

        int exitCode = cmd.execute("Amigos");

        assertEquals(0, exitCode);
        assertTrue(outContent.toString().contains("OK Amigos"));
    }

    @Test
    void grafoInexistente_retornaError() {
        CommandLine cmd = new CommandLine(new UseCommand());

        int exitCode = cmd.execute("marcianos");

        assertNotEquals(0, exitCode);
        assertTrue(outContent.toString().contains("Error"));
    }
}