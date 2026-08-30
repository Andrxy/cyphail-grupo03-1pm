package cr.ac.una.eif400.broker.fake;

import cr.ac.una.eif400.cyphail.common.QueryResponse;

import java.util.Map;

/**
 * @author Sebastián González
 * @author Alejandro Miranda
 * @version 3.0
 */
public class FakeQueries {
    public static final String NOT_RESOLVED_MESSAGE =
            "Error: invalid input. The given MATCH query could not be resolved against the current graph.";

    public static final String OK_RESOLVED_MESSAGE = "OK. Query resolved after 666 ms.";

    private static final Map<String, QueryResponse> RESPUESTAS = Map.of(
            "MATCH (p:Persona) RETURN p.nombre, p.edad",
            QueryResponse.ok(
                    new String[]{"p.nombre", "p.edad"},
                    new String[][]{
                            {"\"Ana\"", "28"},
                            {"\"Luis\"", "31"},
                            {"\"Carlos\"", "25"},
                            {"\"Beatriz\"", "34"},
                            {"\"David\"", "29"},
                            {"\"Elena\"", "22"}
                    },
                    OK_RESOLVED_MESSAGE
            ),

            "MATCH (p1:Persona)-[r:AMIGO_DE]->(p2:Persona) RETURN p1.nombre AS Persona, type(r) AS Relacion, p2.nombre AS AmigoDe",
            QueryResponse.ok(
                    new String[]{"Persona", "Relacion", "AmigoDe"},
                    new String[][]{
                            {"\"Ana\"", "\"AMIGO_DE\"", "\"Luis\""},
                            {"\"Ana\"", "\"AMIGO_DE\"", "\"Beatriz\""},
                            {"\"Luis\"", "\"AMIGO_DE\"", "\"Carlos\""},
                            {"\"Luis\"", "\"AMIGO_DE\"", "\"David\""},
                            {"\"Carlos\"", "\"AMIGO_DE\"", "\"Elena\""},
                            {"\"Beatriz\"", "\"AMIGO_DE\"", "\"Elena\""}
                    },
                    OK_RESOLVED_MESSAGE
            ),

            "MATCH (t:Tarea) RETURN t.titulo, t.estado",
            QueryResponse.ok(
                    new String[]{"t.titulo", "t.estado"},
                    new String[][]{
                            {"\"Escribir README\"", "\"pending\""},
                            {"\"Implementar parser\"", "\"pending\""},
                            {"\"Setup Maven\"", "\"done\""},
                            {"\"Comandos REPL\"", "\"in-progress\""}
                    },
                    OK_RESOLVED_MESSAGE
            ),

            "MATCH (pl:Planeta) RETURN pl.nombre, pl.distanciaSolUA",
            QueryResponse.ok(
                    new String[]{"pl.nombre", "pl.distanciaSolUA"},
                    new String[][]{
                            {"\"Mercurio\"", "0.39"},
                            {"\"Venus\"", "0.72"},
                            {"\"Tierra\"", "1.0"},
                            {"\"Marte\"", "1.52"}
                    },
                    OK_RESOLVED_MESSAGE
            )
    );

    private FakeQueries() {
    }

    public static QueryResponse resolve(String target) {
        return RESPUESTAS.getOrDefault(
                target.trim(),
                QueryResponse.error(NOT_RESOLVED_MESSAGE));
    }
}
