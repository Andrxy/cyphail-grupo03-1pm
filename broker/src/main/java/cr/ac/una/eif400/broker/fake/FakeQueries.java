package cr.ac.una.eif400.broker.fake;

import java.util.Map;

/**
 * Catálogo de fake queries reconocidas por el Broker.

 * Alambrado puro: el texto exacto de la
 * consulta (tal como lo compone el flujo Repl -> Compiler -> Broker) mapea
 * a una tabla de resultado ya formateada. No hay analisis sintáctico real
 * todavía, eso es justamente lo que el pipeline de compiler.* va a hacer
 * en un futuro (ver Compiler.compileWithFullPipeline())

 * @author Sebastián González
 * @version 1.0
 */
public class FakeQueries {
    private static final Map<String, String> RESPUESTAS = Map.of(
            "MATCH (p:Persona) RETURN p.nombre, p.edad",
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

            "MATCH (p1:Persona)-[r:AMIGO_DE]->(p2:Persona) RETURN p1.nombre AS Persona, type(r) AS Relacion, p2.nombre AS AmigoDe",
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

            "MATCH (t:Tarea) RETURN t.titulo, t.estado",
            """
            t.titulo               t.estado
            -----------------------------------
            "Escribir README"      "pending"
            "Implementar parser"   "pending"
            "Setup Maven"          "done"
            "Comandos REPL"        "in-progress"
            """,

            "MATCH (pl:Planeta) RETURN pl.nombre, pl.distanciaSolUA",
            """
            pl.nombre    pl.distanciaSolUA
            -------------------------------
            "Mercurio"   0.39
            "Venus"      0.72
            "Tierra"     1.0
            "Marte"      1.52
            """
    );

    private FakeQueries() {
        // clase de utilidad, no se instancia
    }

    /**
     * Busca la respuesta fake de una query por texto exacto (recortando
     * espacios al inicio/final, sin normalizar nada más).
     *
     * @param sourceCode texto tal como lo tecleo el usuario en el REPL
     * @return la tabla ya formateada si la query es conocida, o null si no
     */
    public static String resolve(String sourceCode) {
        return RESPUESTAS.get(sourceCode.trim());
    }
}