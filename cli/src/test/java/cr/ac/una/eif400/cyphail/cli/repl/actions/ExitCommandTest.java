package cr.ac.una.eif400.cyphail.cli.repl.actions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comando ".exit".
 *
 * @author Sebastian Gonzalez y Alejandro Miranda - Grupo 03-1pm
 * @version 1.1
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
    @DisplayName("El repl arranca activo")
    void elReplArrancaActivo() {
        assertTrue(repl.isRunning());
    }

    @Test
    @DisplayName("Marca el repl como terminado")
    void detieneElRepl() {
        ejecutar(".exit");
        assertFalse(repl.isRunning(), "El repl queda marcado para salir");
    }
}
