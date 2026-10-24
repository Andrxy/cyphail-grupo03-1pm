package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Separa una lista de tokens en sentencias, usando el token {@code ;} como terminador
 *
 * @author Alejandro Miranda
 * @version P2
 */
public class Statements {

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
