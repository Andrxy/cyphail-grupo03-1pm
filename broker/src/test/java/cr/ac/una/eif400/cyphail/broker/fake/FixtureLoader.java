package cr.ac.una.eif400.cyphail.broker.fake;

import com.google.gson.Gson;
import cr.ac.una.eif400.cyphail.broker.model.Graph;
import cr.ac.una.eif400.cyphail.broker.model.QueryEntry;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public final class FixtureLoader {

    private FixtureLoader() {
    }

    public static Graph[] loadGraphs() {
        return load("graphJ.json", Graph[].class);
    }

    public static QueryEntry[] loadQueries() {
        return load("queries.json", QueryEntry[].class);
    }

    private static <T> T load(String fileName, Class<T> type) {
        String cyphailHome = System.getProperty("cyphail.home");
        Path filePath = cyphailHome != null
                ? Paths.get(cyphailHome, "broker", "data", fileName)
                : Paths.get("data", fileName);

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar " + fileName + " para las pruebas", e);
        }
    }

    public static String unknownGraphName(Graph[] graphs) {
        String candidate;
        do {
            candidate = "grafo-inexistente-" + UUID.randomUUID();
        } while (containsGraphId(graphs, candidate));
        return candidate;
    }

    private static boolean containsGraphId(Graph[] graphs, String name) {
        for (Graph graph : graphs) {
            if (graph.id().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static String unknownQuery(QueryEntry[] queries) {
        String candidate;
        do {
            candidate = "MATCH (x:Inexistente" + UUID.randomUUID() + ") RETURN x";
        } while (containsQuery(queries, candidate));
        return candidate;
    }

    private static boolean containsQuery(QueryEntry[] queries, String query) {
        for (QueryEntry entry : queries) {
            if (entry.query().equals(query)) {
                return true;
            }
        }
        return false;
    }
}
