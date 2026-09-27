package cr.ac.una.eif400.cyphail.compiler.parser.result;

/**
 * Interfaz sellada que representa el resultado de una operación del Parser,
 * pudiendo ser un éxito ({@link Ok}) o un fallo ({@link Fail})
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @version P1
 * @param <I> el tipo de la entrada restante
 * @param <T> el tipo del valor analizado
 * @param <R> el tipo de la razón o error devuelto
 */
public sealed interface Result<I, T, R> permits Ok, Fail {
}
