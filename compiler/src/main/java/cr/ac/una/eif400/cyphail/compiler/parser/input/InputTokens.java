package cr.ac.una.eif400.cyphail.compiler.parser.input;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;

import java.util.List;

/**
 * Representa una entrada compuesta por una lista de tokens
 * y la posición actual
 *
 * @author Andrey Solano
 * @version P1
 * @param input la lista de tokens de entrada
 * @param index la posición o índice actual en la lista
 */
public record InputTokens(List<TokenString> input, int index) implements Input<List<TokenString>>{

    /**
     * Nueva entrada de tokens comenzando desde el inicio (índice 0)
     *
     * @param input la lista de tokens
     */
    public InputTokens(List<TokenString> input){
        this(input, 0);
    }

    /**
     * Avanza a la siguiente posición de la lista
     *
     * @return un nuevo {@link InputTokens} apuntando al siguiente token
     */
    public InputTokens next(){
        return new InputTokens(input, index + 1);
    }

    /**
     * Verifica si se consumieron todos los tokens de la entrada
     *
     * @return true si el índice alcanzó o superó el tamaño de la lista
     */
    public boolean isEmpty(){
        return index() >= input().size();
    }

    /**
     * Obtiene el token actual sin avanzar la posición
     *
     * @return el token actual de tipo {@link TokenString}
     */
    public TokenString first(){
        return input().get(index);
    }
}
