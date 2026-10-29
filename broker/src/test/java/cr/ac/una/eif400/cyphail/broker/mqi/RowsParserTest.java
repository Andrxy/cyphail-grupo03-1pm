package cr.ac.una.eif400.cyphail.broker.mqi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Casos de la tabla 3.2 del contrato de {@code Rows}.
 *
 * @author Sebastián González
 *
 * <p>Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 */
@DisplayName("Pruebas del RowsParser")
public class RowsParserTest {

    @Test
    @DisplayName("Separa encabezados y filas")
    void separaEncabezadosYFilas() {
        RowsParser.Table table = RowsParser.parse("\"Persona\" \"Relacion\"\n\"Ana\" \"AMIGO_DE\"");

        assertArrayEquals(new String[]{"Persona", "Relacion"}, table.headers());
        assertArrayEquals(new String[][]{{"Ana", "AMIGO_DE"}}, table.rows());
    }

    @Test
    @DisplayName("El espacio dentro de las comillas no separa celdas")
    void espacioDentroDeComillas() {
        RowsParser.Table table = RowsParser.parse("\"t.titulo\" \"t.estado\"\n\"Escribir README\" \"pending\"");

        assertArrayEquals(new String[][]{{"Escribir README", "pending"}}, table.rows());
    }

    @Test
    @DisplayName("Deshace los escapes de comilla, barra y salto de linea")
    void deshaceEscapes() {
        RowsParser.Table table = RowsParser.parse("\"x\"\n\"di \\\"hola\\\" C:\\\\ a\\nb\"");

        assertEquals("di \"hola\" C:\\ a\nb", table.rows()[0][0]);
    }

    @Test
    @DisplayName("null sin comillas es el texto null")
    void nullSinComillas() {
        RowsParser.Table table = RowsParser.parse("\"a\" \"b\"\n\"x\" null");

        assertArrayEquals(new String[][]{{"x", "null"}}, table.rows());
    }

    @Test
    @DisplayName("Solo encabezados: tabla sin filas")
    void soloEncabezados() {
        RowsParser.Table table = RowsParser.parse("\"nombre\" \"edad\"");

        assertArrayEquals(new String[]{"nombre", "edad"}, table.headers());
        assertEquals(0, table.rows().length);
    }

    @Test
    @DisplayName("Texto vacio: exito sin tabla")
    void textoVacio() {
        RowsParser.Table table = RowsParser.parse("");

        assertEquals(0, table.headers().length);
        assertEquals(0, table.rows().length);
    }

    @Test
    @DisplayName("Acepta tildes y enie")
    void tildes() {
        assertEquals("Ñandú canción", RowsParser.parse("\"n\"\n\"Ñandú canción\"").rows()[0][0]);
    }

    @Test
    @DisplayName("Rechaza celdas sin comillas o con dos espacios")
    void rechazaFormatoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> RowsParser.parse("\"a\" b"));
        assertThrows(IllegalArgumentException.class, () -> RowsParser.parse("\"a\"  \"b\""));
    }

    @Test
    @DisplayName("Rechaza filas con distinta cantidad de celdas")
    void rechazaFilaIncompleta() {
        assertThrows(IllegalArgumentException.class, () -> RowsParser.parse("\"a\" \"b\"\n\"x\""));
    }
}
