package cr.ac.una.eif400.broker;

import cr.ac.una.eif400.broker.fake.FakeGraphs;
import cr.ac.una.eif400.broker.fake.FakeQueries;
import cr.ac.una.eif400.cyphail.common.QueryResponse;

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
        return FakeGraphs.exists(name);
    }

    public String getCurrentGraph() {
        return currentGraph;
    }

    public void setCurrentGraph(String graphName) {
        this.currentGraph = graphName;
    }

    public QueryResponse resolveQuery(String target) {
        String[] resolved = FakeQueries.resolve(target);

        if (resolved == null) {
            return QueryResponse.error(FakeQueries.NOT_RESOLVED_MESSAGE);
        }

        return QueryResponse.ok(resolved[0], resolved[1]);
    }
}