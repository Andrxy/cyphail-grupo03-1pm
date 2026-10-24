package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.TokenMaps;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.IParser;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.parser.Combinators.*;

/**
 * Clase con métodos auxiliares y para la construcción de la clase {@link Grammar}
 *
 * @version P1
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @author Wilson Umaña
 */
public class GrammarHelpers {

    /**
     * Se come el primer token de la entrada si coincide con el tipo esperado. <br>
     * Tomado del archivo Work.java de la sesión del del 22 de setiembre
     *
     * @author Carlos Loría-Sáenz
     * @param type el tipo de token que espera encontrar
     * @param source record con la lista de tokens y el índice actual
     * @return un {@link Result}, ya sea {@link Ok} con los tokens restantes y el token que se comió
     * o {@link Fail} con un error si falla
     */
    static Result<InputTokens, TokenString, String> eat(TokenType type, InputTokens source) {
        if (source.isEmpty())
            return new Fail<>(String.format("Expecting token %s. Found empty source", type));
        var token = source.first();
        if (token.type() != type)
            return new Fail<>(String.format("Expecting token %s. Found %s", type, token.type()));
        return new Ok<>(source.next(), token);
    }

    /**
     * Crea un parser que come un token de un tipo específico y devuelve, ya sea {@link Fail} con un
     * error o {@link Result} con el el resto y el nodo hoja del AST.<br>
     * Tomado del archivo Work.java de la sesión
     * del del 22 de setiembre
     *
     * @author Carlos Loría-Sáenz
     * @param type el tipo de token que el parser debe comerse
     * @return un {@link IParser} que procesa la entrada y retorna un nodo {@link AstNode}
     */
    static IParser<InputTokens, AstNode, String> parsify(TokenType type) {
        return (InputTokens source) -> switch (eat(type, source)) {
            case Fail(String reason) -> new Fail<>(reason);
            case Ok(InputTokens rest, TokenString token) -> new Ok<>(rest, new Leaf(token));
        };
    }

    /**
     * Crea parser que verifica si se llegó al final de la entrada (EOF)
     *
     * @return un {@link IParser} que valida que no queden tokens por procesar.
     */
    static IParser<InputTokens, AstNode, String> EOF() {
        return (InputTokens source) -> {
            if (source.isEmpty())
                return new Ok<>(source, new Leaf(new TokenString(TokenType.EOF, "")));
            return new Fail<>(String.format("Expecting end of input. Found %s", source.first().type()));
        };
    }

    /**
     * Transforma un parser que produce una lista de nodos del AST en un parser
     * que encapsula dicha lista dentro de un nodo ({@link Node}).
     *
     * @param parser el parser que genera una lista de {@link AstNode}
     * @return un {@link IParser} que retorna un único {@link AstNode}
     */
    static IParser<InputTokens, AstNode, String> asNode(IParser<InputTokens, List<AstNode>, String> parser) {
        return TFlat(parser, items -> new Node(items));
    }

    /**
     * Intenta aplicar el parser de listas. Si falla usa {@code OrFlat} para devolver un nodo vacío
     *
     * @param listParser el parser de listas que se quiere evaluar
     * @return un parser que devuelve los elementos encontrados o un nodo vacío si todos fallan
     */
    static IParser<InputTokens, AstNode, String> orEmpty(IParser<InputTokens, AstNode, String> listParser) {
        return OrFlat(concatStringList, listParser, (InputTokens source) -> new Ok<>(source, new Node(List.of())));
    }

    /**
     * Crea un parser que come un token cualquiera que sea palabra clave y devuelve su nodo hoja,
     * para poder usar una palabra clave como nombre, como en el label {@code Order}
     *
     * @return un {@link IParser} que procesa la entrada y retorna un nodo {@link AstNode}
     */
    static IParser<InputTokens, AstNode, String> parsifyKeyword() {
        return (InputTokens source) -> {
            if (source.isEmpty())
                return new Fail<>("Expecting a keyword. Found empty source");
            var token = source.first();
            if (!TokenMaps.KEYWORDS.containsValue(token.type()))
                return new Fail<>(String.format("Expecting a keyword. Found %s", token.type()));
            return new Ok<>(source.next(), new Leaf(token));
        };
    }
}
