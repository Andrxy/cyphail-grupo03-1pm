package cr.ac.una.eif400.broker.fake;

import java.util.Map;

/**
 * Catálogo de fake queries reconocidas por el Broker.

 * Alambrado puro: el texto exacto de la
 * consulta (tal como lo compone el flujo Repl -> Compiler -> Broker) mapea
 * a un par {tabla de resultado ya formateada, mensaje de estado}. No hay
 * analisis sintáctico real todavía, eso es justamente lo que el pipeline de
 * compiler.* va a hacer en un futuro (ver Compiler.compileWithFullPipeline())

 * @author Sebastián González
 * @author Alejandro Miranda
 * @version 2.0
 */
public class FakeQueries {
    public static final String NOT_RESOLVED_MESSAGE =
            "Error: invalid input. The given MATCH query could not be resolved against the current graph.";

    private static final Map<String, String[]> RESPUESTAS = Map.of(
            "MATCH (p:Persona) RETURN p.nombre, p.edad",
            new String[]{
                    """
                    p.nombre   p.edad
                    ---------------------
                    "Ana"      28
                    "Luis"     31
                    "Carlos"   25
                    "Beatriz"  34
                    "David"    29
                    "Elena"    22
                    """,
                    "OK. Query resolved after 666 ms."
            },

            "MATCH (p1:Persona)-[r:AMIGO_DE]->(p2:Persona) RETURN p1.nombre AS Persona, type(r) AS Relacion, p2.nombre AS AmigoDe",
            new String[]{
                    """
                    Persona    Relacion       AmigoDe
                    -------------------------------------
                    "Ana"      "AMIGO_DE"     "Luis"
                    "Ana"      "AMIGO_DE"     "Beatriz"
                    "Luis"     "AMIGO_DE"     "Carlos"
                    "Luis"     "AMIGO_DE"     "David"
                    "Carlos"   "AMIGO_DE"     "Elena"
                    "Beatriz"  "AMIGO_DE"     "Elena"
                    """,
                    "OK. Query resolved after 1274 ms."
            },

            "MATCH (t:Tarea) RETURN t.titulo, t.estado",
            new String[]{
                    """
                    t.titulo               t.estado
                    -----------------------------------
                    "Escribir README"      "pending"
                    "Implementar parser"   "pending"
                    "Setup Maven"          "done"
                    "Comandos REPL"        "in-progress"
                    """,
                    "OK. Query resolved after 312 ms."
            },

            "MATCH (pl:Planeta) RETURN pl.nombre, pl.distanciaSolUA",
            new String[]{
                    """
                    pl.nombre    pl.distanciaSolUA
                    -------------------------------
                    "Mercurio"   0.39
                    "Venus"      0.72
                    "Tierra"     1.0
                    "Marte"      1.52
                    """,
                    "OK. Query resolved after 87 ms."
            }
    );

    private FakeQueries() {
    }

    /**
     * Busca la respuesta fake de una query por texto exacto (recortando
     * espacios al inicio/final, sin normalizar nada más).
     *
     * @param target texto tal como lo tecleo el usuario en el REPL
     * @return par {tabla formateada, mensaje} si la query es conocida, o null si no
     */
    public static String[] resolve(String target) {
        return RESPUESTAS.get(target.trim());
    }
}