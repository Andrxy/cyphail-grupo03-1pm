package cr.ac.una.eif400.cyphail.compiler.tokenizer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pruebas del analisis lexico. En el sprint P1.1 el tokenizer devuelve el mismo
 * codigo fuente que recibe, asi que las pruebas fijan ese contrato.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas del Tokenizer")
public class TokenizerTest {

    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    @DisplayName("Devuelve el codigo fuente recibido")
    void devuelveElCodigoFuenteRecibido() {
        String source = "MATCH (p:Persona) RETURN p.nombre, p.edad";
        assertEquals(source, tokenizer.tokenize(source));
    }

    @Test
    @DisplayName("Respeta los espacios del codigo fuente")
    void respetaLosEspacios() {
        assertEquals("  MATCH  ", tokenizer.tokenize("  MATCH  "));
    }

    @Test
    @DisplayName("Una entrada vacia produce una salida vacia")
    void entradaVaciaProduceSalidaVacia() {
        assertEquals("", tokenizer.tokenize(""));
    }

    @Test
    @DisplayName("Una entrada nula produce una salida nula")
    void entradaNulaProduceSalidaNula() {
        assertNull(tokenizer.tokenize(null));
    }
}
