package cr.ac.una.eif400.broker.fake;

import com.google.gson.Gson;
import cr.ac.una.eif400.broker.model.Graph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Proveedor que gestiona los grafos quemados para las pruebas
 *
 * <p>Contiene definiciones de cabeceras, estructuras de datos estáticas y métodos de validación
 * implementados temporalmente para efectos del primer sprint P1.1</p>
 *
 * @author Wilson Umaña
 * @version P1.1
 */
public class FakeGraphs {

    private static final Graph[] AVAILABLE_GRAPHS = loadGraphs();

    public static String[] headers = {"Graph", "Description"};

    private static Graph[] loadGraphs() {
        String cyphailHome = System.getProperty("cyphail.home");
        Path filePath = cyphailHome != null
                ? Paths.get(cyphailHome, "broker", "data", "graphJ.json")
                : Paths.get("data", "graphJ.json");

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, Graph[].class);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar graphJ.json", e);
        }
    }

    public static Graph[] getAvailableGraphs() {
        return AVAILABLE_GRAPHS;
    }

    public static String[][] toRows() {
        return Arrays.stream(getAvailableGraphs())
                .map(graph -> new String[]{ graph.id(), graph.description() })
                .toList()
                .toArray(new String[0][]);
    }

    public static boolean exists(String name) {
        return Arrays.stream(AVAILABLE_GRAPHS)
                .anyMatch(graph -> graph.id().equalsIgnoreCase(name));
    }


}
