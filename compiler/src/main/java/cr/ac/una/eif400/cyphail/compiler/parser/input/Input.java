package cr.ac.una.eif400.cyphail.compiler.parser.input;

/**
 * Interfaz que define la estructura básica de un input
 * manteniendo un su contenido y el índice actual
 *
 * @author Alejandro Miranda
 * @version P1
 * @param <T> el tipo de datos que contiene la entrada
 */
public interface Input<T> {

    /**
     * @return el contenido de la entrada
     */
    T input();

    /**
     * @return el índice actual
     */
    int index();
}