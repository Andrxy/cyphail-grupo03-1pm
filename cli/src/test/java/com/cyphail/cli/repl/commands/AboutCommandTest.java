package com.cyphail.cli.repl.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comando ".about".
 *
 * @author Sebastian Gonzalez y Alejandro Miranda - Grupo 03-1pm
 * @version 1.0
 */
@DisplayName("Pruebas del comando .about")
public class AboutCommandTest extends CommandTestSupport {

    @Test
    @DisplayName("Termina con codigo 0")
    void terminaConExito() {
        assertEquals(0, ejecutar(".about"));
    }

    @Test
    @DisplayName("Describe que es cyphail")
    void describeLaHerramienta() {
        ejecutar(".about");
        assertTrue(salida().contains("cyphail"), "La salida menciona cyphail");
        assertTrue(salida().contains("grafos"), "La salida menciona grafos");
    }
}
