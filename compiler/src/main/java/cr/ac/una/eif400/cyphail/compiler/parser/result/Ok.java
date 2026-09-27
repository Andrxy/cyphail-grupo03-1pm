package cr.ac.una.eif400.cyphail.compiler.parser.result;

/**
 * Representa un resultado exitoso del parser, contiene la entrada que sobró y el valor obtenido
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @param rest la entrada restante por procesar
 * @param value el valor resultante del análisis
 * @param <I> el tipo de la entrada restante
 * @param <T> el tipo del valor analizado
 * @param <R> el tipo de la razón o error devuelto
 */
public record Ok<I, T, R>(I rest, T value) implements Result<I, T, R> {
}