package cr.ac.una.eif400.cyphail.cli.output;

import com.github.freva.asciitable.AsciiTable;

/**
 * Punto único de formateo de salidas tabulares del REPL.
 * Centraliza la llamada a la librería encargada de construir las tablas
 *
 * <p>Para efectos de esta versión P1.1, es una fachada delgada sobre
 * {@link AsciiTable#getTable(String[], Object[][])}: si el formateo de tablas necesita
 * más lógica en el futuro (columnas alineadas por tipo, truncado de valores largos,
 * anchos configurables, etc.), este es el único lugar donde debe cambiarse.</p>
 *
 * @author Andrey Solano
 * @author Sebastián González
 * @version P1.1
 */
public class Formatter {
    /**
     * Formatea encabezados y filas como una tabla ASCII lista para imprimir.
     *
     * @param headers nombres de columna, en el orden en que deben mostrarse.
     * @param rows filas de datos; cada fila debe tener el mismo número de columnas que headers.
     * @return la tabla ya formateada como texto.
     */
    public static String format(String[] headers, String[][] rows) {
        return AsciiTable.getTable(headers, rows);
    }
}