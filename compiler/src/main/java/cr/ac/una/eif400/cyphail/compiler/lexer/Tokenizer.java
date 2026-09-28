package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;

import cr.ac.una.eif400.cyphail.compiler.parser.IParser;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.lexer.Lexers.*;
import static cr.ac.una.eif400.cyphail.compiler.parser.Combinators.*;

/**
 * Convierte el código fuente a una lista de tokens
 *
 * @author Andrey Solano
 * @author Sebastián González
 * @version P1
 */
public class Tokenizer {

    /**
     * Reconoce un token de cualquier tipo. El orden importa (greedy), los operadores de varios
     * caracteres van antes que los de uno solo, para que {@code ->} no se lea como
     * {@code -} seguido de {@code >}.
     */
    private static IParser<InputString, TokenString, String> ANY_TOKEN = OrFlat(
            concatStringList,
            Relationship,
            Comparison,
            Keywords,
            Identifier,
            StringLiteral,
            Numbers,
            Arithmetic,
            Delimiter
    );

    private static IParser<InputString, List<TokenString>, String> TOKENIZER = Star(ANY_TOKEN);

    public static Result<InputString, List<TokenString>, String> tokenize(String sourceCode) {
        return switch (TOKENIZER.parse(new InputString(sourceCode))) {
            case Fail(var reason) -> new Fail<>(reason);
            case Ok(var rest, var tokens) -> unconsumed(sourceCode, rest.index())
                    ? new Fail<>("Lexical Error: Unexpected character at index " + rest.index())
                    : new Ok<>(rest, tokens);
        };
    }

    /**
     * Indica si quedó algo sin reconocer al final del código fuente.
     *
     * <p>Cada lexer consume los espacios que vienen antes de su token, pero los que quedan
     * después del último no los consume nadie. Por eso no basta con comparar el índice
     * contra el largo: hay que ignorar el espacio en blanco sobrante, o una consulta
     * terminada en espacio o en salto de línea daría un error léxico falso.</p>
     *
     * @param sourceCode código fuente completo.
     * @param index      posición donde se detuvo el reconocimiento.
     * @return true si lo que sobra no es solo espacio en blanco.
     */
    private static boolean unconsumed(String sourceCode, int index) {
        return !sourceCode.substring(index).isBlank();
    }
}