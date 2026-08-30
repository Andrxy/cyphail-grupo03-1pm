package com.cyphail.cli.repl.actions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comando ".help", que lista los comandos punto del REPL.
 *
 * @author Sebastian Gonzalez y Alejandro Miranda - Grupo 03-1pm
 * @version 1.1
 */
@DisplayName("Pruebas del comando .help")
public class HelpCommandTest extends CommandTestSupport {

    @Test
    @DisplayName("Termina con codigo 0")
    void terminaConExito() {
        assertEquals(0, ejecutar(".help"));
    }

    @Test
    @DisplayName("Lista los cuatro comandos punto")
    void listaTodosLosComandos() {
        ejecutar(".help");
        assertTrue(salida().contains(".use"), "Lista .use");
        assertTrue(salida().contains(".about"), "Lista .about");
        assertTrue(salida().contains(".help"), "Lista .help");
        assertTrue(salida().contains(".exit"), "Lista .exit");
    }

    @Test
    @DisplayName("Muestra el encabezado y como escribir un comando")
    void muestraElEncabezadoYLaAyuda() {
        ejecutar(".help");
        assertTrue(salida().contains("Cyphail REPL - available commands:"), "Muestra el encabezado");
        assertTrue(salida().contains("Type a command starting with"), "Explica como escribir un comando");
    }

    @Test
    @DisplayName("Los comandos salen en el orden definido por el listado")
    void mantieneElOrdenDelListado() {
        ejecutar(".help");
        String salida = salida();
        assertTrue(salida.indexOf(".help") < salida.indexOf(".exit"), ".help antes que .exit");
        assertTrue(salida.indexOf(".exit") < salida.indexOf(".about"), ".exit antes que .about");
        assertTrue(salida.indexOf(".about") < salida.indexOf(".use"), ".about antes que .use");
    }
}
