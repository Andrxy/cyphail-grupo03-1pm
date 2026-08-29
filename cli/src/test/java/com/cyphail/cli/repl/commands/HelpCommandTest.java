package com.cyphail.cli.repl.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comando ".help", que arma su listado leyendo los subcomandos
 * registrados en el comando padre.
 *
 * @author Sebastian Gonzalez y Alejandro Miranda - Grupo 03-1pm
 * @version 1.0
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
    @DisplayName("Muestra la descripcion de cada comando")
    void muestraLasDescripciones() {
        ejecutar(".help");
        assertTrue(salida().contains("Salir de Cyphail REPL"), "Describe .exit");
        assertTrue(salida().contains("Lista los grafos disponibles"), "Describe .use");
    }

    @Test
    @DisplayName("Los comandos salen en orden alfabetico")
    void ordenaAlfabeticamente() {
        ejecutar(".help");
        String salida = salida();
        assertTrue(salida.indexOf(".about") < salida.indexOf(".help"), ".about antes que .help");
        assertTrue(salida.indexOf(".help") < salida.indexOf(".use"), ".help antes que .use");
    }

    @Test
    @DisplayName("El comando .exit se lista de ultimo")
    void listaExitAlFinal() {
        ejecutar(".help");
        String salida = salida();
        assertTrue(salida.indexOf(".about") < salida.indexOf(".exit"), ".exit va despues de .about");
        assertTrue(salida.indexOf(".help") < salida.indexOf(".exit"), ".exit va despues de .help");
        assertTrue(salida.indexOf(".use") < salida.indexOf(".exit"), ".exit va despues de .use");
    }
}
