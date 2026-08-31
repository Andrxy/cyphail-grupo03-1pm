package cr.ac.una.eif400.broker;

import cr.ac.una.eif400.broker.fake.FakeGraphs;
import cr.ac.una.eif400.broker.fake.FakeQueries;
import cr.ac.una.eif400.cyphail.common.QueryResponse;

/**
 * Gestor principal de conexiones.
 *
 * <p>Implementa un Singleton para actuar como un intermediario entre la interfaz de usuario,
 * el compilador y los proveedores de datos. Actualmente es fake para efectos del primer sprint P1.1</p>
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @author Sebastián González
 * @version P1.1
 */
public class Broker {

    private static final Broker INSTANCE = new Broker();
    private String currentGraph;

    private Broker() {
    }

    /**
     * Obtiene la instancia global única del Broker
     *
     * @return la instancia única del Broker
     */
    public static Broker getInstance() {
        return INSTANCE;
    }

    /**
     * Obtiene los grafos disponibles fake.
     *
     * @return una matriz bidimensional con los nombres y descripciones de los grafos
     */
    public String[][] getAvailableGraphs() {
        return FakeGraphs.availableGraphs;
    }

    /**
     * Obtiene las cabeceras para la visualización del listado cuando se tabule
     *
     * @return un arreglo de cadenas con las cabeceras
     */
    public String[] getGraphHeaders() {
        return FakeGraphs.headers;
    }

    /**
     * Valida si un grafo específico existe dentro de los grafos fake
     *
     * @param name el nombre del grafo a verificar
     * @return {@code true} si el grafo existe, si no {@code false}
     */
    public boolean graphExists(String name) {
        return FakeGraphs.exists(name);
    }

    /**
     * Obtiene el nombre del grafo actualmente seleccionado en la sesión
     *
     * @return el nombre del grafo activo o {@code null} si no hay ninguno seleccionado
     */
    public String getCurrentGraph() {
        return currentGraph;
    }

    /**
     * Establece el grafo activo para la sesión
     *
     * @param graphName el nombre del grafo que pasará a estar activo
     */
    public void setCurrentGraph(String graphName) {
        this.currentGraph = graphName;
    }

    /**
     * Resuelve una consulta de código fuente. Actualmente, como todo es
     * fake, delega la resolución a la clase {@link FakeQueries}. En un futuro
     * se establece la comunicación con Prolog
     *
     * @param target la cadena de texto de la consulta a procesar
     * @return el objeto {@link QueryResponse} resultante
     */
    public QueryResponse resolveQuery(String target) {
        return FakeQueries.resolve(target);
    }
}