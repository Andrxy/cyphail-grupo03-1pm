package cr.ac.una.eif400.cyphail.cli.repl.actions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas del comando .tree")
public class TreeCommandTest extends CommandTestSupport {

    @Test
    @DisplayName("Termina con codigo 0")
    void terminaConExito() {
        assertEquals(0, ejecutar(".tree", "MATCH", "(m:Movie)", "RETURN", "m"));
    }

    @Test
    @DisplayName("Une las palabras recibidas y muestra el arbol de la consulta")
    void muestraElArbol() {
        ejecutar(".tree", "MATCH", "(m:Movie)", "RETURN", "m.title");

        assertTrue(salida().contains("Query {"), salida());
        assertTrue(salida().contains("labels: [Movie]"), salida());
        assertTrue(salida().contains("(. m title)"), salida());
        assertTrue(salida().contains("Success"), salida());
    }

    @Test
    @DisplayName("Con error semantico muestra el arbol y el error")
    void errorSemantico() {
        ejecutar(".tree", "MATCH", "(m:Movie)", "RETURN", "q");

        assertTrue(salida().contains("Query {"), salida());
        assertTrue(salida().contains("Semantic error: Undefined variable 'q' in RETURN"), salida());
        assertFalse(salida().contains("Success"), salida());
    }

    @Test
    @DisplayName("Con error sintactico muestra solo el error")
    void errorSintactico() {
        ejecutar(".tree", "MATCH", "(m:Movie", "RETURN", "m");

        assertTrue(salida().contains("Syntax error: "), salida());
        assertFalse(salida().contains("Query {"), salida());
    }

    @Test
    @DisplayName("Con error lexico muestra solo el error")
    void errorLexico() {
        ejecutar(".tree", "MATCH", "(m:Movie)", "@");

        assertTrue(salida().contains("Lexical Error"), salida());
        assertFalse(salida().contains("Query {"), salida());
    }
}
