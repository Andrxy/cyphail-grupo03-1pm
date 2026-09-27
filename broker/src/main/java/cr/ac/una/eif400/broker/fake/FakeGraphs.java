package cr.ac.una.eif400.broker.fake;

import com.google.gson.Gson;
import cr.ac.una.eif400.broker.model.Graph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Proveedor que gestiona los grafos quemados para las pruebas
 *
 * <p>Contiene definiciones de cabeceras, estructuras de datos estáticas y métodos de validación
 * implementados temporalmente para efectos del primer sprint P1.1</p>
 *
 * @author Wilson Umaña
 * Version 1.0
 */


public class FakeGraphs {

    private static final Graph[] AVAILABLE_GRAPHS = loadGraphs();

    public static String[] headers = {"Graph", "Description"};

    private static Graph[] loadGraphs() {
        Path filePath = DataFileLocator.resolveDataFile("graphJ.json");

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
        Graph[] graphs = getAvailableGraphs();
        String[][] rows = new String[graphs.length][2];
        for (int i = 0; i < graphs.length; i++) {
            rows[i][0] = graphs[i].id();
            rows[i][1] = graphs[i].description();
        }
        return rows;
    }

    public static boolean exists(String name) {
        for (Graph graph : AVAILABLE_GRAPHS) {
            if (graph.id().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }


}
