package cr.ac.una.eif400.broker;

import cr.ac.una.eif400.broker.mqi.PrologEngine;
import cr.ac.una.eif400.broker.mqi.PrologResult;
import cr.ac.una.eif400.broker.mqi.RowsParser;
import cr.ac.una.eif400.cyphail.common.QueryResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Gestor principal de conexiones.
 *
 * <p>Implementa un Singleton para actuar como intermediario entre el compilador y el engine de
 * Prolog. Recibe el texto Prolog que genera el Codegen, lo ejecuta por MQI en un proceso
 * {@code swipl} con {@code engine/engine.pl} cargado y convierte la variable {@code Rows} de la
 * respuesta en un {@link QueryResponse}.</p>
 *
 * <p>El proceso se levanta la primera vez que se necesita (arranque perezoso), así {@code .help}
 * o {@code .about} no lo lanzan, y un <i>shutdown hook</i> lo termina al salir de la JVM. Si la
 * conexión se cae, el siguiente uso vuelve a arrancar el proceso.</p>
 *
 * <p>Ningún error se propaga como excepción hacia el REPL: todos se devuelven como
 * {@code QueryResponse.error("Error: ...")}.</p>
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @author Sebastián González
 *
 * <p>La versión P2 reemplaza el broker falso de P1 por la conexión con MQI, siguiendo el Plan #3
 * del equipo y sin cambiar la interfaz pública.
 * Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 *
 * @version P2
 */
public class Broker {

    private static final Broker INSTANCE = new Broker();

    private static final String[] GRAPH_HEADERS = {"Graph", "Description"};
    private static final String ROWS = "Rows";

    /**
     * MQI solo conserva los argumentos de las excepciones con forma {@code error(Termino, _)}: de
     * {@code cy_error('No graph selected')} enviaría solo {@code cy_error}. Por eso el goal se
     * envuelve para relanzar {@code cy_error(M)} como {@code error(cy_error(M), _)} y no perder
     * el mensaje.
     */
    private static final String CY_ERROR_GUARD =
            "catch((%s), cy_error(CyMessage), throw(error(cy_error(CyMessage), _)))";

    static final String ENGINE_FAILED_MESSAGE = "Error: the engine could not resolve the query";
    static final String SWIPL_NOT_STARTED_MESSAGE = "Error: SWI-Prolog (swipl) could not be started";
    static final String CONNECTION_LOST_MESSAGE = "Error: the connection with SWI-Prolog was lost, try again";

    private String currentGraph;
    private PrologEngine engine;
    private String[][] graphs;

    private Broker() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "cyphail-swipl-shutdown"));
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
     * Obtiene los grafos que ofrece el engine con {@code cy_graphs(Rows)}.
     *
     * <p>Se piden una sola vez y se guardan. Si el engine no responde se devuelve un listado vacío
     * y se vuelve a intentar en la siguiente llamada.</p>
     *
     * @return una matriz con el id y la descripción de cada grafo
     */
    public String[][] getAvailableGraphs() {
        if (graphs == null) {
            QueryResponse response = execute("cy_graphs(Rows)");
            if (!response.isOk()) {
                return new String[0][];
            }
            graphs = response.getRows();
        }
        return graphs;
    }

    /**
     * Obtiene las cabeceras para la visualización del listado cuando se tabule
     *
     * @return un arreglo de cadenas con las cabeceras
     */
    public String[] getGraphHeaders() {
        return GRAPH_HEADERS.clone();
    }

    /**
     * Valida si un grafo existe en el engine, sin distinguir mayúsculas.
     *
     * @param name el nombre del grafo a verificar
     * @return {@code true} si el grafo existe, si no {@code false}
     */
    public boolean graphExists(String name) {
        return name != null && Arrays.stream(getAvailableGraphs())
                .anyMatch(graph -> graph[0].equalsIgnoreCase(name));
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
     * Establece el grafo activo para la sesión, tal como lo escribió el usuario.
     *
     * @param graphName el nombre del grafo que pasará a estar activo
     */
    public void setCurrentGraph(String graphName) {
        this.currentGraph = graphName;
    }

    /**
     * Resuelve en el engine el texto Prolog que generó el Codegen.
     *
     * <p>Si hay un grafo activo, antes del goal se agrega {@code set_current_graph(grafo)} en
     * minúscula, para que el engine siempre consulte el grafo elegido con {@code .use}, aunque
     * el proceso se haya reiniciado.</p>
     *
     * @param target el goal de Prolog, sin el punto final, que deja el resultado en {@code Rows}
     * @return los encabezados y filas con el tiempo real, o el error
     */
    public QueryResponse resolveQuery(String target) {
        String goal = currentGraph == null
                ? target
                : "set_current_graph(%s), (%s)".formatted(atom(currentGraph.toLowerCase()), target);
        return execute(goal);
    }

    /**
     * Ejecuta un goal que deja su resultado en {@code Rows} y mide cuánto tardó.
     *
     * @param goal el goal de Prolog
     * @return la respuesta lista para el REPL
     */
    private QueryResponse execute(String goal) {
        long start = System.nanoTime();
        PrologResult result;
        try {
            result = engine().run(CY_ERROR_GUARD.formatted(goal));
        } catch (EngineNotStarted e) {
            return QueryResponse.error(SWIPL_NOT_STARTED_MESSAGE);
        } catch (IOException e) {
            close();
            return QueryResponse.error(CONNECTION_LOST_MESSAGE);
        }
        long millis = (System.nanoTime() - start) / 1_000_000;

        return switch (result) {
            case PrologResult.Success(List<Map<String, Object>> solutions) -> toResponse(solutions, millis);
            case PrologResult.Failure() -> QueryResponse.error(ENGINE_FAILED_MESSAGE);
            case PrologResult.PrologError(Object term) -> QueryResponse.error("Error: " + describe(term));
        };
    }

    /**
     * Toma {@code Rows} de la primera solución y lo pasa a tabla.
     *
     * @param solutions las soluciones del goal
     * @param millis    tiempo que tardó el engine
     * @return la respuesta con la tabla, o un error si {@code Rows} no viene o no tiene el formato
     */
    private static QueryResponse toResponse(List<Map<String, Object>> solutions, long millis) {
        if (!(solutions.getFirst().get(ROWS) instanceof String rows)) {
            return QueryResponse.error("Error: the engine did not return Rows");
        }
        try {
            RowsParser.Table table = RowsParser.parse(rows);
            return QueryResponse.ok(table.headers(), table.rows(),
                    "OK. Query resolved after " + millis + " ms.");
        } catch (IllegalArgumentException e) {
            return QueryResponse.error("Error: " + e.getMessage());
        }
    }

    /**
     * Texto de una excepción de Prolog. Para {@code cy_error(Mensaje)} es solo el mensaje.
     *
     * @param term el término de la excepción como lo dejó Gson
     * @return el texto para mostrar
     */
    private static String describe(Object term) {
        if (term instanceof Map<?, ?> compound) {
            List<?> args = (List<?>) compound.get("args");
            if ("cy_error".equals(compound.get("functor")) && args.size() == 1) {
                return String.valueOf(args.getFirst());
            }
            return compound.get("functor") + args.stream()
                    .map(Broker::describe)
                    .reduce((left, right) -> left + ", " + right)
                    .map(inner -> "(" + inner + ")")
                    .orElse("");
        }
        return String.valueOf(term);
    }

    /**
     * Escribe un texto como átomo de Prolog entre comillas simples.
     *
     * @param text el texto
     * @return el átomo, por ejemplo {@code 'amigos'}
     */
    private static String atom(String text) {
        return "'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    /**
     * Devuelve el engine, arrancándolo si todavía no existe o si el proceso murió.
     *
     * @return el engine listo
     * @throws EngineNotStarted si {@code swipl} no se pudo arrancar
     */
    private synchronized PrologEngine engine() throws EngineNotStarted {
        if (engine == null || !engine.isAlive()) {
            close();
            try {
                engine = PrologEngine.start(enginePath());
            } catch (IOException e) {
                throw new EngineNotStarted(e);
            }
        }
        return engine;
    }

    /**
     * Ruta del engine: {@code <cyphail.home>/engine/engine.pl}, o {@code engine/engine.pl}
     * relativo si la propiedad no existe.
     *
     * @return la ruta del {@code engine.pl}
     */
    private static Path enginePath() {
        String cyphailHome = System.getProperty("cyphail.home");
        return cyphailHome != null
                ? Paths.get(cyphailHome, "engine", "engine.pl")
                : Paths.get("engine", "engine.pl");
    }

    /**
     * Cierra la conexión y termina {@code swipl}. Se puede llamar varias veces; el siguiente uso
     * del broker vuelve a arrancar el proceso.
     */
    public synchronized void close() {
        if (engine != null) {
            engine.close();
            engine = null;
        }
    }

    /**
     * {@code swipl} no se pudo arrancar. Se distingue de una caída de la conexión porque el
     * mensaje para el usuario es distinto.
     */
    private static class EngineNotStarted extends Exception {
        EngineNotStarted(Throwable cause) {
            super(cause);
        }
    }
}
