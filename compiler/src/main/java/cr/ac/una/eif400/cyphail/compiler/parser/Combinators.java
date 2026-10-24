package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Clase auxiliar que agrupa los diferentes combinadores
 * utilizados para la construcción del Grammar <br><br>
 *
 * Prompt: Analice constructivamente mis combinadores, a la hora de extender la gramática a futuro, qué otro combinador se podría hacer mezclando los míos con el fin que el código sea DRY
 *
 * @author Andrey Solano
 * @author Alejandro Miranda
 */
public class Combinators {

    /**
     * Prueba varios parsers en orden hasta que uno peguem si todos fallan, junta las razones
     *
     * @param parsers los parsers candidatos a evaluar.
     * @return un parser que devuelve el primer resultado exitoso o una lista con todos los errores
     */
    @SafeVarargs
    public static <I, T, R> IParser<I, T, List<R>> Or(IParser<I, T, R>... parsers) {
        return (I source) -> {
            var reasons = new ArrayList<R>();

            for (var p : parsers) {
                switch (p.parse(source)) {
                    case Ok(I rest, T value) -> {
                        return new Ok<>(rest, value);
                    }
                    case Fail(R reason) -> {
                        reasons.add(reason);
                    }
                }
            }

            return new Fail<>(reasons);
        };
    }

    /**
     * Ejecuta una lista de parsers uno tras otro en secuencia estricta
     *
     * @param parsers los parsers que deben cumplirse en orden
     * @return un parser que devuelve una lista con los valores acumulados de cada paso
     */
    @SafeVarargs
    public static <I, T, R> IParser<I, List<T>, R> Sequence(IParser<I, T, R>... parsers) {
        record State<I, T, R>(List<T> tokens, I source, boolean isOk, R reason, boolean trying) {}

        return (I source) -> {
            var seed = new State<I, T, R>(List.<T>of(), source, true, null, true);

            var iterator = Arrays.stream(parsers).iterator();

            var finalState = Stream
                    .iterate(seed, acc -> {
                        if (!acc.isOk || !iterator.hasNext())
                            return new State<I, T, R>(acc.tokens, acc.source, acc.isOk, acc.reason, false);
                        return switch (iterator.next().parse(acc.source)) {
                            case Ok(I rest, T token) -> new State<I, T, R>(append(acc.tokens, token), rest, true, null, true);
                            case Fail(R reason) -> new State<I, T, R>(acc.tokens, acc.source, false, reason, true);
                        };
                    })
                    .takeWhile(acc -> acc.trying)
                    .reduce((acc, curr) -> curr)
                    .orElse(seed);

            return finalState.isOk
                    ? new Ok<>(finalState.source, finalState.tokens)
                    : new Fail<>(finalState.reason);
        };
    }

    /**
     * Aplica un parser cero o más veces el parser que recibe
     *
     * @param parser el parser a repetir
     * @return un parser con una lista de todos los elementos que logró recolectar
     */
    public static <I, T, R> IParser<I, List<T>, R> Star(IParser<I, T, R> parser){
        record State<I, T>(List<T> tokens, I source, boolean trying) {}

        return (I source) -> {

            var seed = new State<I, T>(List.<T>of(), source, true);

            var finalState = Stream.iterate(seed, acc ->
                        switch (parser.parse(acc.source)){
                            case Ok(I rest, T token) -> new State<I, T>(append(acc.tokens, token), rest, true);
                            case Fail(R reason) -> new State<I, T>(acc.tokens, acc.source, false);
                        }
                    )
                    .takeWhile(acc -> acc.trying())
                    .reduce((acc, curr) -> curr)
                    .orElse(seed);

            return new Ok<>(finalState.source, finalState.tokens);
        };
    }

    /**
     * Aplica un parser una o más veces, falla si no encuentra al menos una coincidencia
     *
     * @param parser el parser a repetir
     * @return un parser con la lista de elementos encontrados (mínimo uno)
     */
    public static <I, T, R> IParser<I, List<T>, R> Plus(IParser<I, T, R> parser) {
        return (I source) -> switch (parser.parse(source)) {
            case Fail(R reason) -> new Fail<>(reason);
            case Ok(I rest, T first) -> switch (Star(parser).parse(rest)) {
                case Ok(I restAfterStar, List<T> others) ->
                        new Ok<>(restAfterStar, Stream.concat(Stream.of(first), others.stream()).toList());
                case Fail(R reason) -> new Fail<>(reason);
            };
        };
    }

    /**
     * Hace que un parser sea opcional, si falla devuelve una lista vacía
     *
     * @param parser el parser opcional
     * @return un parser que envuelve el resultado en una lista o devuelve una vacía si no pegó
     */
    public static <I, T, R> IParser<I, List<T>, R> Optional(IParser<I, T, R> parser) {
        return (I source) -> switch (parser.parse(source)) {
            case Ok(I rest, T value) -> new Ok<>(rest, List.of(value));
            case Fail(R _) -> new Ok<>(source, List.of());
        };
    }

    /**
     * Transforma el resultado exitoso de un parser usando una función mapeadora. <br>
     * Tomado del documento Work de la sesión del 22 de setiembre
     *
     * @author Carlos Loría-Sáenz
     * @param parser el parser original que produce una lista.
     * @param mapper función para transformar la lista en un único valor.
     * @return un parser con el valor ya mapeado.
     */
    public static <I, T, R> IParser<I, T, R> TFlat(IParser<I, List<T>, R> parser, Function<List<T>, T> mapper) {
        return (I source) -> switch (parser.parse(source)) {
            case Ok(I rest, List<T> value) -> new Ok<>(rest, mapper.apply(value));
            case Fail(R reason) -> new Fail<>(reason);
        };
    }

    /**
     * Aplana y combina los errores de un parser cuando falla. <br>
     * Tomado del documento Work de la sesión del 22 de setiembre
     *
     * @author Carlos Loría-Sáenz
     * @param parser el parser cuyos errores se quieren aplanar.
     * @param concat función para unir la lista de errores en uno solo.
     * @return un parser con el error ya unificado si algo sale mal.
     */
    public static <I, T, R> IParser<I, T, R> RFlat(IParser<I, T, List<R>> parser, Function<List<R>, R> concat) {
        return (I source) -> switch (parser.parse(source)) {
            case Ok(I rest, T value) -> new Ok<>(rest, value);
            case Fail(List<R> reason) -> new Fail<>(concat.apply(reason));
        };
    }

    /**
     * Une las listas de mensajes de error. <br>
     * Tomado del documento Work de la sesión del 22 de setiembre
     *
     * @author Carlos Loría-Sáenz
     */
    public static Function<List<String>, String> concatStringList = (List<String> list) -> String.join(" & ", list);

    /**
     * <b>Sugerido por Claude con el fin que el código sea más DRY</b><br>
     *
     * Como un Or normal, pero aplanando los errores de golpe con una función de concatenación
     *
     * @param concat  función para unir los errores si todos fallan
     * @param parsers los parsers a evaluar
     * @return un parser combinando el Or con el aplanamiento de errores
     */
    @SafeVarargs
    public static <I, T, E> IParser<I, T, E> OrFlat(Function<List<E>, E> concat, IParser<I, T, E>...  parsers) {
        return RFlat(Or(parsers), concat);
    }

    /**
     * <b>Sugerido por Claude con el fin que el código sea más DRY</b><br>
     *
     * Ejecuta dos parsers en secuencia y se queda únicamente con el resultado del de la derecha
     *
     * @param left  el primer parser (resultado se ignora)
     * @param right el segundo parser (resultado se conserva)
     * @return un parser que retorna el valor de la derecha
     */
    public static <I, T, R> IParser<I, T, R> JustGetRight(IParser<I, T, R> left, IParser<I, T, R> right) {
        return TFlat(Sequence(left, right), list -> list.get(1));
    }

    /**
     * <b>Sugerido por Claude con el fin que el código sea más DRY</b><br>
     *
     * Ejecuta dos parsers en secuencia y se queda únicamente con el resultado del de la izquierda
     *
     * @param left  el primer parser (resultado se conserva)
     * @param right el segundo parser (resultado se ignora)
     * @return un parser que retorna el valor de la izquierda
     */
    public static <I, T, R> IParser<I, T, R> JustGetLeft(IParser<I, T, R> left, IParser<I, T, R> right) {
        return TFlat(Sequence(left, right), list -> list.get(0));
    }

    /**
     * <b>Sugerido por Claude con el fin que el código sea más DRY</b><br>
     *
     * Ejecuta tres parsers en secuencia y se queda únicamente con el resultado del de en medio
     *
     * @param left   el parser izquierdo
     * @param middle el parser central (cuyo resultado se conserva)
     * @param right  el parser derecho
     * @return un parser que retorna el valor del medio
     */
    public static <I, T, R> IParser<I, T, R> JustGetMiddle(IParser<I, T, R> left, IParser<I, T, R> middle, IParser<I, T, R> right) {
        return TFlat(Sequence(left, middle, right), list -> list.get(1));
    }

    /**
     * <b>Sugerido por Claude con el fin que el código sea más DRY</b><br>
     *
     * Permite definir reglas recursivas evaluándolas de manera lazy
     *
     * @param rule proveedor del parser real
     * @return un parser lazy que evalúa la regla al momento de aplicarla
     */
    public static <I, T, R> IParser<I, T, R> Lazy(Supplier<IParser<I, T, R>> rule) {
        return source -> rule.get().parse(source);
    }

    private static <T> List<T> append(List<T> values, T value) {
        return Stream.concat(values.stream(), Stream.of(value)).toList();
    }
}