package cr.ac.una.eif400.cyphail.compiler.parser.input;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;

import java.util.List;

/**
 * @author Andrey Solano
 * @param input
 * @param index
 */
public record InputTokens(List<TokenString> input, int index) implements Input<List<TokenString>>{
    public InputTokens(List<TokenString> input){
        this(input, 0);
    }

    public InputTokens next(){
        return new InputTokens(input, index + 1);
    }
    public boolean isEmpty(){
        return index() >= input().size();
    }
    public TokenString first(){
        return input().get(index);
    }
}
