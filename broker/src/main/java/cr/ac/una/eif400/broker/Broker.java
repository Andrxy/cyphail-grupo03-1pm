package cr.ac.una.eif400.broker;

import cr.ac.una.eif400.broker.fake.FakeGraphs;
import cr.ac.una.eif400.broker.fake.FakeQueries;

/**
 * Broker fake que hace de puente entre el Compiler y los datos
 * alambrados del proyecto (grafos y queries fake)
 *
 * @author Andrey Solano
 * @author Sebastián González
 * @version 2.0
 */

public class Broker {
    private static final Broker INSTANCE = new Broker();
    private String currentGraph;

    private Broker() {
        // cosntructor privado
    }

    public static Broker getInstance() {
        return INSTANCE;
    }

    public String[][] getAvailableGraphs() {
        return FakeGraphs.availableGraphs;
    }

    public String[] getGraphHeaders() {
        return FakeGraphs.headers;
    }

    public boolean graphExists(String name) {
        for (String[] graph : FakeGraphs.availableGraphs) {
            if (graph[0].equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public String getCurrentGraph() {
        return currentGraph;
    }


    public void setCurrentGraph(String graphName) {
        this.currentGraph = graphName;
    }

    /**
     * Resuelve una fake query contra el catálogo alambrado.
     *
     * @param sourceCode texto de la query tal como lo compilo/reconocio el Compiler
     * @return la tabla de resultado ya formateada, o null si la query no es conocida
     */
    public String resolveQuery(String sourceCode) {
        return FakeQueries.resolve(sourceCode);
    }
}