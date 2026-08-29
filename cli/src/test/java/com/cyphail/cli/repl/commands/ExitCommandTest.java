package com.cyphail.cli.repl.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comando ".exit".
 *
 * @author Sebastian Gonzalez y Alejandro Miranda - Grupo 03-1pm
 * @version 1.0
 */
@DisplayName("Pruebas del comando .exit")
public class ExitCommandTest extends CommandTestSupport {

    @Test
    @DisplayName("Termina con codigo 0")
    void terminaConExito() {
        assertEquals(0, ejecutar(".exit"));
    }

    @Test
    @DisplayName("Se despide del usuario")
    void seDespide() {
        ejecutar(".exit");
        assertTrue(salida().contains("Bye!"), "La salida contiene la despedida");
    }

    @Test
    @DisplayName("La sesion arranca activa")
    void laSesionArrancaActiva() {
        assertTrue(repl.session().isRunning());
    }

    @Test
    @DisplayName("Marca la sesion como terminada")
    void detieneLaSesion() {
        ejecutar(".exit");
        assertFalse(repl.session().isRunning(), "La sesion queda marcada para salir");
    }
}
