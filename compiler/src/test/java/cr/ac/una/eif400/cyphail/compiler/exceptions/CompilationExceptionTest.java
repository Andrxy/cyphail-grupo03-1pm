package cr.ac.una.eif400.cyphail.compiler.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas del error de compilacion del lenguaje.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas de CompilationException")
public class CompilationExceptionTest {

    @Test
    @DisplayName("Conserva el mensaje del fallo")
    void conservaElMensaje() {
        CompilationException exception = new CompilationException("Token inesperado");
        assertEquals("Token inesperado", exception.getMessage());
    }

    @Test
    @DisplayName("Es una excepcion no verificada")
    void esUnaExcepcionNoVerificada() {
        assertInstanceOf(RuntimeException.class, new CompilationException("Token inesperado"));
    }

    @Test
    @DisplayName("Se puede lanzar y atrapar por su tipo")
    void seLanzaYSeAtrapaPorSuTipo() {
        CompilationException lanzada = assertThrows(
                CompilationException.class,
                () -> { throw new CompilationException("Falta RETURN"); });

        assertEquals("Falta RETURN", lanzada.getMessage());
    }
}
