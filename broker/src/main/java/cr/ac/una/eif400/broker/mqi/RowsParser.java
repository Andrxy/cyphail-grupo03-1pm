package cr.ac.una.eif400.broker.mqi;

import java.util.Arrays;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convierte el texto {@code Rows} que devuelve el engine en encabezados y filas.
 *
 * <p>Formato acordado con el engine:</p>
 * <ul>
 *     <li>Una línea por fila, separadas por un salto de línea real. La primera son los encabezados.</li>
 *     <li>Cada celda va entre comillas dobles y las celdas se separan con un solo espacio.</li>
 *     <li>Dentro de una celda, {@code \"} es una comilla, {@code \\} una barra invertida y
 *     {@code \n} un salto de línea (dos caracteres).</li>
 *     <li>{@code null} sin comillas es el valor ausente y se muestra como el texto {@code null}.</li>
 *     <li>El texto vacío significa éxito sin tabla.</li>
 * </ul>
 *
 * <p>Es una función pura hecha con streams: cada línea se valida con una expresión regular y
 * sus celdas se extraen con {@link Matcher#results()}.</p>
 *
 * @author Sebastián González
 *
 * <p>Implementa el formato {@code Rows} acordado con el engine en el Plan #3 del equipo.
 * Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 *
 * @version P2
 */
public final class RowsParser {

    /** Una celda: texto entre comillas con escapes, o {@code null} sin comillas. */
    private static final String CELL = "\"((?:[^\"\\\\]|\\\\.)*)\"|(null)";
    private static final Pattern CELL_PATTERN = Pattern.compile(CELL);
    private static final Pattern LINE_PATTERN = Pattern.compile("(?:" + CELL + ")(?: (?:" + CELL + "))*");
    private static final Pattern ESCAPE = Pattern.compile("\\\\(.)");

    private RowsParser() {
    }

    /**
     * Resultado tabular: encabezados y filas.
     *
     * @param headers los nombres de las columnas
     * @param rows    una fila por solución
     */
    public record Table(String[] headers, String[][] rows) {
    }

    /**
     * Interpreta el texto {@code Rows}.
     *
     * @param rows el texto que devolvió el engine
     * @return la tabla equivalente; vacía si el texto es vacío
     * @throws IllegalArgumentException si alguna línea no respeta el formato
     */
    public static Table parse(String rows) {
        if (rows.isEmpty()) {
            return new Table(new String[0], new String[0][]);
        }

        String[][] lines = rows.lines().map(RowsParser::cells).toArray(String[][]::new);
        String[] headers = lines[0];

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].length != headers.length) {
                throw new IllegalArgumentException("Row " + i + " has " + lines[i].length
                        + " cells but there are " + headers.length + " headers");
            }
        }
        return new Table(headers, Arrays.copyOfRange(lines, 1, lines.length));
    }

    /**
     * Separa una línea en sus celdas, ya sin comillas ni escapes.
     *
     * @param line una línea de {@code Rows}
     * @return las celdas
     */
    private static String[] cells(String line) {
        if (!LINE_PATTERN.matcher(line).matches()) {
            throw new IllegalArgumentException("Invalid Rows line: " + line);
        }
        return CELL_PATTERN.matcher(line).results()
                .map(RowsParser::cellText)
                .toArray(String[]::new);
    }

    /**
     * Texto de una celda encontrada por {@link #CELL_PATTERN}.
     *
     * @param cell la coincidencia: grupo 1 si venía entre comillas, grupo 2 si era {@code null}
     * @return el texto de la celda
     */
    private static String cellText(MatchResult cell) {
        return cell.group(2) != null ? "null" : unescape(cell.group(1));
    }

    /**
     * Deshace los escapes {@code \"}, {@code \\} y {@code \n}.
     *
     * @param text el contenido de una celda entre comillas
     * @return el texto real
     */
    private static String unescape(String text) {
        return ESCAPE.matcher(text).replaceAll(escape ->
                Matcher.quoteReplacement(escape.group(1).equals("n") ? "\n" : escape.group(1)));
    }
}
