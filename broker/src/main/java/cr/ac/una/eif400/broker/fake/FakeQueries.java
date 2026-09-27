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
import java.util.HashMap;
import java.util.Map;

public class FakeQueries {

    public static final String NOT_RESOLVED_MESSAGE =
            "Error: invalid input. The given MATCH query could not be resolved against the current graph.";

    public static final String OK_RESOLVED_MESSAGE = "OK. Query resolved after 666 ms.";

    private static final Map<String, QueryResponse> RESPUESTAS = loadQueries();

    private static Map<String, QueryResponse> loadQueries() {
        Path filePath = DataFileLocator.resolveDataFile("queries.json");

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            QueryEntry[] entries = new Gson().fromJson(reader, QueryEntry[].class);
            Map<String, QueryResponse> respuestas = new HashMap<>();
            for (QueryEntry entry : entries) {
                respuestas.put(entry.query(), QueryResponse.ok(entry.headers(), entry.rows(), OK_RESOLVED_MESSAGE));
            }
            return respuestas;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar queries.json", e);
        }
    }

    public static QueryResponse resolve(String target) {
        return RESPUESTAS.getOrDefault(
                target.trim(),
                QueryResponse.error(NOT_RESOLVED_MESSAGE));
    }
}