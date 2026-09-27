package cr.ac.una.eif400.broker.model;

/**
 * Representa una entrada de consulta ejecutada, almacenando la query,
 * los encabezados de las columnas y las filas de la respuesta
 *
 * @author Wilson Umaña
 * @param query la sentencia de la consulta
 * @param headers los encabezados de las columnas del resultado
 * @param rows las filas con los datos obtenidos
 */
public record QueryEntry(String query, String[] headers, String[][] rows) {
}