package cr.ac.una.eif400.cyphail.compiler.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la utilidad que guarda el registro del flujo de compilacion.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas de FlowLog")
public class FlowLogTest extends FlowLogTestSupport {

    @Test
    @DisplayName("Escribe el bloque recibido en el archivo de registro")
    void escribeElBloqueRecibido() throws IOException {
        FlowLog.write("[Tokenizer] Iniciando el proceso de tokenizacion...\n");

        assertTrue(registro().contains("[Tokenizer] Iniciando el proceso de tokenizacion..."));
    }

    @Test
    @DisplayName("Cada bloque se agrega al final, sin borrar los anteriores")
    void agregaAlFinalSinBorrar() throws IOException {
        FlowLog.write("primera corrida\n");
        FlowLog.write("segunda corrida\n");

        assertEquals("primera corrida\nsegunda corrida\n", registro());
    }

    @Test
    @DisplayName("Escribir un bloque vacio deja el registro sin cambios")
    void unBloqueVacioNoAgregaNada() throws IOException {
        FlowLog.write("una corrida\n");
        FlowLog.write("");

        assertEquals("una corrida\n", registro());
    }
}
