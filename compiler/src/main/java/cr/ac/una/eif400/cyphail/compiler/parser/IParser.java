package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;

/**
 * Interfaz funcional que define parser con su método abstracto para procesar la entrada
 *
 * @author Andrey Solano
 * @version P1
 * @param <I> el tipo de la entrada a analizar
 * @param <T> el valor que devuelve si el parseo es exitoso
 * @param <R> el motivo o razón del fallo si no parsea
 */
@FunctionalInterface
public interface IParser<I, T, R> {

    /**
     * Parsea la fuente de entrada y devuelve el resultado
     *
     * @param source la fuente de entrada a procesar
     * @return un {@link Result} que indica éxito o fallo con sus respectivos valores
     */
    Result<I, T, R> parse(I source);
}