package cr.ac.una.eif400.cyphail.compiler.parser.input;

/**
 * Representa una fuente de entrada basada en una string
 * junto con su posición actual
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @version P1
 * @param input el texto de entrada
 * @param index la posición actual
 */
public record InputString(String input, int index) implements Input<String> {

    /**
     * Crea uno nuevo comenzando desde el inicio (índice 0)
     *
     * @param input el texto de entrada
     */
    public InputString(String input){
        this(input, 0);
    }
}
