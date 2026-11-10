package cr.ac.una.eif400.cyphail.cli.repl.actions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comando ".clear".
 *
 * @author Alejandro Miranda - Grupo 03-1pm
 * @version P2
 */
@DisplayName("Pruebas del comando .clear")
public class ClearCommandTest extends CommandTestSupport {

    @Test
    @DisplayName("Termina con código de éxito")
    void terminaConExito() {
        assertEquals(0, ejecutar(".clear"));
    }

    @Test
    @DisplayName("Imprime la secuencia que limpia la pantalla")
    void imprimeLaSecuenciaDeLimpieza() {
        ejecutar(".clear");
        assertTrue(salida().contains("\033[H\033[2J"), "La salida contiene la secuencia ANSI");
    }
}
