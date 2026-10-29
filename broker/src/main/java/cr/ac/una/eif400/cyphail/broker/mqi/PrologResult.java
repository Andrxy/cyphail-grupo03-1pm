package cr.ac.una.eif400.cyphail.broker.mqi;

import java.util.List;
import java.util.Map;

/**
 * Resultado de ejecutar un goal en SWI-Prolog por medio de MQI.
 *
 * <p>Es una interfaz sellada con tres casos posibles, para que quien la use tenga que
 * contemplarlos todos en un {@code switch} exhaustivo, igual que {@code Result} en el parser:</p>
 * <ul>
 *     <li>{@link Success}: el goal tuvo al menos una solución.</li>
 *     <li>{@link Failure}: el goal no tuvo soluciones ({@code false} en Prolog).</li>
 *     <li>{@link PrologError}: el goal lanzó una excepción.</li>
 * </ul>
 *
 * @author Sebastián González
 *
 * <p>Sigue el diseño de {@code PrologResult} del demo {@code MQI_ResponseDemo} del curso: la
 * misma interfaz sellada con los tres casos (aquí el campo se llama {@code solutions}).
 * Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 *
 * @version P2
 */
public sealed interface PrologResult {

    /**
     * El goal tuvo soluciones.
     *
     * @param solutions una entrada por solución; cada una asocia el nombre de cada variable
     *                  del goal con su valor
     */
    record Success(List<Map<String, Object>> solutions) implements PrologResult {
    }

    /**
     * El goal falló: no hubo ninguna solución.
     */
    record Failure() implements PrologResult {
    }

    /**
     * El goal lanzó una excepción.
     *
     * @param term el término de la excepción tal como llegó en el JSON: un {@code String} si es
     *             un átomo, o un {@code Map} con {@code functor} y {@code args} si es compuesto
     */
    record PrologError(Object term) implements PrologResult {
    }
}
