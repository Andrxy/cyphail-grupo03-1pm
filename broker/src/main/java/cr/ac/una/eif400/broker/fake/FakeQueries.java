package cr.ac.una.eif400.broker.fake;

import com.google.gson.Gson;
import cr.ac.una.eif400.broker.model.QueryEntry;
import cr.ac.una.eif400.cyphail.common.QueryResponse;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simula consultas que carga y resuelve con respuestas predefinidas
 * desde un archivo JSON para el P1
 *
 * @author Wilson Umaña
 * @author Alejandro Miranda
 * @author Sebastián González
 */
public class FakeQueries {

    public static String NOT_RESOLVED_MESSAGE =
            "Error: invalid input. The given MATCH query could not be resolved against the current graph.";

    public static String OK_RESOLVED_MESSAGE = "OK. Query resolved after 666 ms.";

    private static Map<String, QueryResponse> RESPUESTAS = loadQueries();

    /**
     * Carga el archivo {@code queries.json} desde la ruta configurada (usando la propiedad del sistema
     * {@code cyphail.home}= y mapea cada consulta con su respuesta predefinida (esto para efectos del P1)
     *
     * @return un mapa con las consultas y sus respuestas simuladas
     * @throws RuntimeException si ocurre un error de lectura o el archivo no se encuentra
     */
    private static Map<String, QueryResponse> loadQueries() {
        String cyphailHome = System.getProperty("cyphail.home");
        Path filePath = cyphailHome != null
                ? Paths.get(cyphailHome, "broker", "data", "queries.json")
                : Paths.get("data", "queries.json");

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            QueryEntry[] entries = new Gson().fromJson(reader, QueryEntry[].class);

            Map<String, QueryResponse> respuestas = Arrays.stream(entries)
                    .collect(Collectors.toMap(
                            entry -> entry.query(),
                            entry -> QueryResponse.ok(entry.headers(), entry.rows(), OK_RESOLVED_MESSAGE)
                    ));
            return respuestas;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar queries.json", e);
        }
    }

    /**
     * Intenta resolver una consulta buscándola en los datos quemados
     *
     * @param query la sentencia de la consulta a resolver
     * @return un {@link QueryResponse} exitoso con los datos si existe, o un error en caso contrario
     */
    public static QueryResponse resolve(String query) {
        return RESPUESTAS.getOrDefault(
                query.trim(),
                QueryResponse.error(NOT_RESOLVED_MESSAGE));
    }
}
