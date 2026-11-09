package cr.ac.una.eif400.cyphail.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import cr.ac.una.eif400.cyphail.broker.Broker;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.Compiler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public final class WebApp {
    private record GraphChoice(String id, String description) {
    }

    private record QueryResult(boolean ok, String message, String[] headers, String[][] rows,
                               GraphProjection.Graph graph) {
    }

    private static final Gson JSON = new Gson();
    private static final Broker BROKER = Broker.getInstance();
    private static final Object QUERY_LOCK = new Object();

    private WebApp() {
    }

    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8090 : Integer.parseInt(args[0]);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/graphs", WebApp::graphs);
        server.createContext("/api/query", WebApp::query);
        server.start();
        System.out.println("Cyphail API available at http://127.0.0.1:" + port);
    }

    private static void graphs(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            respond(exchange, 405, new ErrorResponse("Method not allowed"));
            return;
        }
        synchronized (QUERY_LOCK) {
            List<GraphChoice> choices = Arrays.stream(BROKER.getAvailableGraphs())
                    .map(row -> new GraphChoice(row[0], row[1]))
                    .toList();
            respond(exchange, 200, choices);
        }
    }

    private static void query(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            respond(exchange, 405, new ErrorResponse("Method not allowed"));
            return;
        }

        JsonObject request;
        try {
            request = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
        } catch (RuntimeException error) {
            respond(exchange, 400, new ErrorResponse("Invalid JSON body"));
            return;
        }

        String graph = string(request, "graph");
        String source = string(request, "query");
        if (graph == null || source == null || source.isBlank()) {
            respond(exchange, 400, new ErrorResponse("Graph and query are required"));
            return;
        }

        synchronized (QUERY_LOCK) {
            if (!BROKER.graphExists(graph)) {
                respond(exchange, 400, new ErrorResponse("Graph does not exist"));
                return;
            }
            try {
                BROKER.setCurrentGraph(graph);
                QueryResponse response = Compiler.getInstance().compile(source);
                GraphProjection.Graph projection = GraphProjection.from(BROKER, response);
                respond(exchange, 200, new QueryResult(response.isOk(), response.getMessage(),
                        response.getHeaders(), response.getRows(), projection));
            } catch (RuntimeException error) {
                respond(exchange, 500, new ErrorResponse(error.getMessage()));
            }
        }
    }

    private static String string(JsonObject request, String key) {
        return request.has(key) && request.get(key).isJsonPrimitive() && request.get(key).getAsJsonPrimitive().isString()
                ? request.get(key).getAsString()
                : null;
    }

    private static void respond(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JSON.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private record ErrorResponse(String error) {
    }
}
