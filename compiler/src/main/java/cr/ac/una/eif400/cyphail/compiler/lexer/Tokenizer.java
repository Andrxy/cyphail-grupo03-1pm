package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;

import cr.ac.una.eif400.cyphail.compiler.parser.IParser;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.lexer.Lexers.*;
import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.BLOCK_COMMENT;
import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.LINE_COMMENT;
import static cr.ac.una.eif400.cyphail.compiler.parser.Combinators.*;

/**
 * Convierte el código fuente a una lista de tokens
 *
 * <p>La version inicial de esta clase, incluida la correccion del reconocimiento
 * de espacios al final del codigo fuente, se escribio con la asistencia de
 * Claude (Anthropic) y fue modificada e integrada por el equipo. Corresponde al
 * prompt 6 del Sprint P1 declarado en el README.</p>
 * 
 * @author Andrey Solano
 * @author Sebastián González
 * @version P1
 *
 *
 */
public class Tokenizer {

    /**
     * Reconoce un token de cualquier tipo. El orden importa (greedy), los operadores de varios
     * caracteres van antes que los de uno solo, para que {@code ->} no se lea como
     * {@code -} seguido de {@code >}.
     */
    private static IParser<InputString, TokenString, String> ANY_TOKEN = OrFlat(
            concatStringList,
            LineComments,
            BlockComments,
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
        return tokenize(sourceCode, false);
    }

    /** Conserva los comentarios para ubicar las sentencias en su texto original. */
    public static Result<InputString, List<TokenString>, String> tokenizeWithComments(String sourceCode) {
        return tokenize(sourceCode, true);
    }

    private static Result<InputString, List<TokenString>, String> tokenize(String sourceCode, boolean includeComments) {
        return switch (TOKENIZER.parse(new InputString(sourceCode))) {
            case Fail(var reason) -> new Fail<>(reason);
            case Ok(var rest, var tokens) -> {
                if (unconsumed(sourceCode, rest.index())) {
                    yield new Fail<>("Lexical Error: Unexpected character at index " + rest.index());
                }

                List<TokenString> filtered = tokens.stream()
                                                   .filter(token -> includeComments || (token.type() != LINE_COMMENT && token.type() != BLOCK_COMMENT))
                                                   .toList();
                yield new Ok<>(rest, filtered);

            }
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
