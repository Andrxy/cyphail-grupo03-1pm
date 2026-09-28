package cr.ac.una.eif400.cyphail.cli.output;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del formateo tabular que usa el REPL para mostrar resultados.
 *
 * @author Alejandro Miranda
 * @version P1.1
 */
@DisplayName("Pruebas del Formatter")
public class FormatterTest {

    private static final String[] HEADERS = {"Graph", "Description"};
    private static final String[][] ROWS = {
            {"Amigos", "Social Network"},
            {"planets", "Planets in Solar System"}
    };

    @Test
    @DisplayName("Muestra los encabezados de las columnas")
    void muestraLosEncabezados() {
        String tabla = Formatter.format(HEADERS, ROWS);

        assertTrue(tabla.contains("Graph"), "Muestra la columna Graph");
        assertTrue(tabla.contains("Description"), "Muestra la columna Description");
    }

    @Test
    @DisplayName("Muestra el valor de cada celda")
    void muestraCadaCelda() {
        String tabla = Formatter.format(HEADERS, ROWS);

        for (String[] fila : ROWS) {
            for (String celda : fila) {
                assertTrue(tabla.contains(celda), "Muestra la celda " + celda);
            }
        }
    }

    @Test
    @DisplayName("Dibuja los bordes de la tabla")
    void dibujaLosBordes() {
        String tabla = Formatter.format(HEADERS, ROWS);

        assertTrue(tabla.contains("|"), "Separa las columnas");
        assertTrue(tabla.contains("-"), "Separa el encabezado de las filas");
    }

    @Test
    @DisplayName("El encabezado va antes de las filas y en el orden dado")
    void respetaElOrdenDeColumnasYFilas() {
        String tabla = Formatter.format(HEADERS, ROWS);

        assertTrue(tabla.indexOf("Graph") < tabla.indexOf("Description"), "Graph antes que Description");
        assertTrue(tabla.indexOf("Description") < tabla.indexOf("Amigos"), "El encabezado va antes de las filas");
        assertTrue(tabla.indexOf("Amigos") < tabla.indexOf("planets"), "Las filas mantienen su orden");
    }

    @Test
    @DisplayName("Una sola fila tambien se formatea")
    void formateaUnaSolaFila() {
        String tabla = Formatter.format(new String[]{"Graph"}, new String[][]{{"teams"}});

        assertTrue(tabla.contains("Graph"));
        assertTrue(tabla.contains("teams"));
    }
}
