package cr.ac.una.eif400.cyphail.cli.repl.actions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
    @DisplayName("Devuelve la señal de salida")
    void devuelveLaSenalDeSalida() {
        assertEquals(ExitCommand.EXIT_CODE, ejecutar(".exit"));
    }

    @Test
    @DisplayName("Se despide del usuario")
    void seDespide() {
        ejecutar(".exit");
        assertTrue(salida().contains("Bye!"), "La salida contiene la despedida");
    }

    @Test
    @DisplayName("Los demás comandos no devuelven la señal de salida")
    void distingueLosDemasComandos() {
        assertEquals(0, ejecutar(".about"));
    }

    @Test
    @DisplayName("El flujo termina sin despachar el comando siguiente")
    void detieneElRepl() {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream(".exit\n.about\n".getBytes(StandardCharsets.UTF_8)));
            repl.open();
        } finally {
            System.setIn(original);
        }
        assertTrue(salida().contains("Bye!"));
        assertFalse(salida().contains("What is cyphail?"));
    }
}
