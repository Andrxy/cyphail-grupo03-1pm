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
        // queries como las del correo
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