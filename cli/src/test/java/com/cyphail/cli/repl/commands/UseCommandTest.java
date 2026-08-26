package com.cyphail.cli.repl.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comando ".use" en sus dos formas: sin argumento lista los
 * grafos disponibles, con argumento selecciona uno.
 *
 * @author Sebastian Gonzalez y Alejandro Miranda - Grupo 03-1pm
 * @version 1.0
 */
@DisplayName("Pruebas del comando .use")
public class UseCommandTest extends CommandTestSupport {

    @Test
    @DisplayName("Sin argumento termina con codigo 0")
    void sinArgumentoTerminaConExito() {
        assertEquals(0, ejecutar(".use"));
    }

    @Test
    @DisplayName("Sin argumento muestra el encabezado de la tabla")
    void sinArgumentoMuestraEncabezado() {
        ejecutar(".use");
        assertTrue(salida().contains("Graph"), "La tabla tiene columna Graph");
        assertTrue(salida().contains("Description"), "La tabla tiene columna Description");
    }

    @Test
    @DisplayName("Sin argumento lista todos los grafos disponibles")
    void sinArgumentoListaLosGrafos() {
        ejecutar(".use");
        assertTrue(salida().contains("Amigos"), "Lista Amigos");
        assertTrue(salida().contains("Tasks"), "Lista Tasks");
        assertTrue(salida().contains("teams"), "Lista teams");
        assertTrue(salida().contains("planets"), "Lista planets");
    }

    @Test
    @DisplayName("Con un grafo existente confirma la seleccion")
    void conGrafoExistenteConfirma() {
        assertEquals(0, ejecutar(".use", "Amigos"));
        assertTrue(salida().contains("OK Amigos available"), "Confirma el grafo elegido");
    }

    @Test
    @DisplayName("Con un grafo inexistente reporta error")
    void conGrafoInexistenteReportaError() {
        assertNotEquals(0, ejecutar(".use", "NoExiste"));
        assertTrue(salida().contains("Error"), "Informa que el grafo no esta en la lista");
    }
}
