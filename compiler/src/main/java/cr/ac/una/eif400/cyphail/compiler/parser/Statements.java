package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Separa una lista de tokens en sentencias, usando el token {@code ;} como terminador
 *
 * @author Alejandro Miranda
 * @version P2
 */
public class Statements {

    public record Statement(String source, List<TokenString> tokens) {}

    /** Ubica cada token una sola vez, incluidos los comentarios, sin volver a tokenizar. */
    public static List<Statement> withSource(String source, List<TokenString> tokens) {
        List<Statement> statements = new ArrayList<>();
        List<TokenString> current = new ArrayList<>();
        int position = 0;
        int start = -1;
        int end = 0;
        for (TokenString token : tokens) {
            int tokenStart = source.indexOf(token.value(), position);
            position = tokenStart + token.value().length();
            if (token.type() == TokenType.LINE_COMMENT || token.type() == TokenType.BLOCK_COMMENT) {
                continue;
            }
            if (token.type() == TokenType.SEMICOLON) {
                if (!current.isEmpty()) {
                    statements.add(new Statement(source.substring(start, position), List.copyOf(current)));
                    current.clear();
                    start = -1;
                }
            } else {
                if (current.isEmpty()) start = tokenStart;
                current.add(token);
                end = position;
            }
        }
        if (!current.isEmpty()) {
            statements.add(new Statement(source.substring(start, end), List.copyOf(current)));
        }
        return statements;
    }

    /**
     * Parte los tokens en sentencias separadas por {@code ;}, descartando las sentencias vacías
     *
     * @param tokens los tokens de un script completo
     * @return una lista con los tokens de cada sentencia, sin el {@code ;} que la termina
     */
    public static List<List<TokenString>> split(List<TokenString> tokens) {
        List<Integer> bounds = Stream.of(
                        Stream.of(-1),
                        IntStream.range(0, tokens.size())
                                .filter(index -> tokens.get(index).type() == TokenType.SEMICOLON)
                                .boxed(),
                        Stream.of(tokens.size()))
                .flatMap(stream -> stream)
                .toList();

        return IntStream.range(0, bounds.size() - 1)
                .mapToObj(index -> tokens.subList(bounds.get(index) + 1, bounds.get(index + 1)))
                .filter(statement -> !statement.isEmpty())
                .toList();
    }
}
