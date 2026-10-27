package cr.ac.una.eif400.broker.mqi;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte la respuesta JSON de MQI en un {@link PrologResult}.
 *
 * <p>MQI responde a {@code run(Goal, Timeout)} con uno de estos JSON:</p>
 * <pre>
 * "false"                                               el goal falló
 * {"functor":"exception", "args":[Termino]}             el goal lanzó una excepción
 * {"functor":"true", "args":[[Sol1, Sol2, ...]]}        las soluciones
 * </pre>
 *
 * <p>Cada solución es una lista de términos {@code =(Variable, Valor)}, que aquí se pasan a un
 * {@code Map} de nombre de variable a valor. Es una función pura: no guarda estado.</p>
 *
 * @author Sebastián González
 *
 * <p>Sigue el {@code MqiResponseParser} del demo {@code MQI_ResponseDemo} del curso: las mismas
 * tres formas de respuesta y el mismo resultado, usando la conversión genérica de Gson.
 * Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 *
 * @version P2
 */
public final class MqiResponseParser {

    private static final Gson GSON = new Gson();

    private MqiResponseParser() {
    }

    /**
     * Interpreta una respuesta de MQI.
     *
     * @param json el texto JSON recibido por el socket
     * @return el resultado equivalente
     * @throws IllegalArgumentException si el texto no tiene la forma de una respuesta de MQI
     */
    public static PrologResult parse(String json) {
        Object response;
        try {
            response = GSON.fromJson(json, Object.class);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid MQI response: " + json, e);
        }

        if ("false".equals(response)) {
            return new PrologResult.Failure();
        }
        if (!(response instanceof Map<?, ?> term)) {
            throw new IllegalArgumentException("Invalid MQI response: " + json);
        }

        List<?> args = (List<?>) term.get("args");
        return switch (String.valueOf(term.get("functor"))) {
            case "true" -> new PrologResult.Success(((List<?>) args.getFirst()).stream()
                    .map(solution -> toBindings((List<?>) solution))
                    .toList());
            case "exception" -> new PrologResult.PrologError(args.getFirst());
            default -> new PrologResult.PrologError(response);
        };
    }

    /**
     * Pasa una solución, una lista de {@code =(Variable, Valor)}, a un mapa ordenado.
     *
     * @param equalities los términos {@code =} de una solución
     * @return el mapa de nombre de variable a valor
     */
    private static Map<String, Object> toBindings(List<?> equalities) {
        Map<String, Object> bindings = new LinkedHashMap<>();
        equalities.forEach(equality -> {
            List<?> pair = (List<?>) ((Map<?, ?>) equality).get("args");
            bindings.put(String.valueOf(pair.get(0)), pair.get(1));
        });
        return bindings;
    }
}
