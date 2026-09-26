package cr.ac.una.eif400.cyphail.compiler.parser.input;

/**
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @param input
 * @param index
 */
public record InputString(String input, int index) implements Input<String>{
    public InputString(String input){
        this(input, 0);
    }
}
