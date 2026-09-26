package cr.ac.una.eif400.cyphail.compiler.parser.input;

/**
 * @author Alejandro Miranda
 * @param <T>
 */
public interface Input<T>{
    T input();
    int index();
}