package cr.ac.una.eif400.cyphail.compiler.parser.result;

/**
 * Representa un fallo del parser, conteniene la razón por el cual no se pudo procesar
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @version P1
 * @param reason el motivo o mensaje del fallo
 * @param <I> el tipo de la entrada restante
 * @param <T> el tipo del valor analizado
 * @param <R> el tipo de la razón o error devuelto
 */
public record Fail<I, T, R>(R reason) implements Result<I, T, R> {
}