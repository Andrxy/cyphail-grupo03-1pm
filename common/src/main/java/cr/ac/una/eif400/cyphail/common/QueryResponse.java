package cr.ac.una.eif400.cyphail.common;

/**
 * Respuesta estándar devuelta tras la ejecución o procesamiento de una consulta.
 * Encapsula el estado de éxito como la estructura tabular de datos resultante (encabezados y filas)
 * o los mensajes de error generados durante el proceso.
 *
 * <p>Para efectos de esta versión P1.1, se establece como la estructura fundamental de transferencia de datos (DTO en web)
 * para validar la arquitectura y la comunicación entre los componentes del sistema.</p>
 *
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @version P1.1
 */
public class QueryResponse {
    private final boolean ok;
    private final String[] headers;
    private final String[][] rows;
    private final String message;

    /**
     * Constructor privado para inicializar la respuesta con los atributos especificados
     *
     * @param ok si la operación fue exitosa
     * @param headers encabezados de las columnas de la respuesta
     * @param rows filas de datos tabulares obtenidos
     * @param message mensaje descriptivo del resultado u operación
     */
    private QueryResponse(boolean ok, String[] headers, String[][] rows, String message) {
        this.ok = ok;
        this.headers = headers;
        this.rows = rows;
        this.message = message;
    }

    /**
     * Crea y retorna una respuesta exitosa conteniendo los datos y un mensaje descriptivo
     *
     * @param headers arreglo con los nombres de las columnas
     * @param rows matriz bidimensional con los registros de datos
     * @param message mensaje informativo sobre el éxito de la operación
     * @return una nueva instancia de QueryResponse con estado positivo
     */
    public static QueryResponse ok(String[] headers, String[][] rows, String message) {
        return new QueryResponse(true, headers, rows, message);
    }

    /**
     * Crea y retorna una respuesta de error con el mensaje descriptivo del fallo ocurrido.
     *
     * @param message mensaje que detalla el error generado
     * @return una nueva instancia de QueryResponse con estado negativo
     */
    public static QueryResponse error(String message) {
        return new QueryResponse(false, null, null, message);
    }

    /**
     * Indica si la consulta o procesamiento se completó de forma exitosa
     *
     * @return true si la operación fue exitosa, sino, devuelve false
     */
    public boolean isOk() {
        return ok;
    }

    /**
     * Obtiene los encabezados de las columnas correspondientes al resultado tabular
     *
     * @return un arreglo de cadenas con los encabezados
     */
    public String[] getHeaders() {
        return headers;
    }

    /**
     * Obtiene las filas de datos correspondientes al resultado
     *
     * @return una matriz de cadenas con los registros obtenidos
     */
    public String[][] getRows() {
        return rows;
    }

    /**
     * Obtiene el mensaje asociado a la respuesta
     *
     * @return una cadena con el mensaje
     */
    public String getMessage() {
        return message;
    }
}
