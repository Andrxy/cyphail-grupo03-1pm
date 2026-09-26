package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.ast.ASTNode;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Result;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static cr.ac.una.eif400.cyphail.compiler.parser.Combinators.*;

/**
 * Gramatica de Cyphail para el Sprint P1: convierte la lista de tokens en un AST.
 *
 * <p>Sigue la gramatica del curso (EIF400-II-2026-GrammarCypherSprint1.g4) recortada al
 * alcance del SPEC de casos de P1: solo patrones de nodo (sin relaciones), varios patrones
 * separados por coma, comparaciones entre expresiones simples, y toda consulta empieza con MATCH.</p>
 *
 * <pre>
 * query          : matchClause+ updatingClause* returnClause? EOF
 * matchClause    : MATCH pattern whereClause?
 * updatingClause : createClause | deleteClause
 * createClause   : CREATE pattern
 * deleteClause   : DETACH? DELETE expression ("," expression)*
 * returnClause   : RETURN projection ("," projection)*
 * projection     : expression (AS variable)?
 * whereClause    : WHERE expression
 * pattern        : nodePattern ("," nodePattern)*
 * nodePattern    : "(" variable? labels? properties? ")"
 * labels         : (":" name)+
 * properties     : "{" (property ("," property)*)? "}"
 * property       : name ":" expression
 * expression     : simple (comparisonOperator simple)?
 * simple         : variable "." name | literal | variable
 * </pre>
 *
 * <p>Cada regla es un parser construido con los combinadores de {@link Combinators}:
 * {@code Sequence} para el orden, {@code Or} para las alternativas, {@code Plus}/{@code Star}
 * para las repeticiones, y {@code Map} para convertir lo reconocido en un nodo del AST.
 * Las reglas estan declaradas en orden de dependencia: cada una usa solo las de arriba.</p>
 *
 * <p>Escrita por el Grupo 03-1pm con la asistencia de Claude (Anthropic),
 * segun lo declarado en el README.</p>
 */
public final class Grammar {

    private static Result<InputTokens, TokenString, String> eat(TokenType type, InputTokens source){
        if (source.isEmpty())
            return new Fail<>(String.format("Expecting token %s. Found empty source", type));
        var token = source.first();
        if (token.type() != type)
            return new Fail<>(String.format("Expecting token %s. Found %s", type, token.type()));
        return new Ok<>(source.next(), token);
    }

    private static IParser<InputTokens, ASTNode, String> parsify(TokenType type){
        return (InputTokens source) -> switch (eat(type, source)) {
            case Fail(String reason) -> new Fail<>(reason);
            case Ok(InputTokens rest, TokenString token) -> new Ok<>(rest, new ASTNode.Leaf(token));
        };
    }

    // NOMBRES

    private static IParser<InputTokens, ASTNode, String> symbolicName = parsify(TokenType.IDENTIFIER);

    private static IParser<InputTokens, ASTNode, String> variable = symbolicName;

    private static IParser<InputTokens, ASTNode, String> labelName = symbolicName;

    private static IParser<InputTokens, ASTNode, String> propertyKeyName = symbolicName;

    private static IParser<InputTokens, ASTNode, String> functionName = symbolicName;

    // LITERALES

    private static IParser<InputTokens, ASTNode, String> integerLiteral = parsify(TokenType.NUMBER);

    private static IParser<InputTokens, ASTNode, String> numberLiteral = integerLiteral;

    private static IParser<InputTokens, ASTNode, String> booleanLiteral = OrFlat(concatStringList, parsify(TokenType.TRUE), parsify(TokenType.FALSE));

    private static IParser<InputTokens, ASTNode, String> literal = OrFlat(concatStringList, numberLiteral, parsify(TokenType.STRING_LITERAL), booleanLiteral, parsify(TokenType.NULL));

    private static IParser<InputTokens, ASTNode, String> atom = OrFlat(concatStringList, literal,
                                                                           JustGetMiddle(parsify(TokenType.LBRACE), expression, parsify(TokenType.RBRACE)),
                                                                           functionInvocation, variable);

    private static IParser<InputTokens, ASTNode, String> propertyLookup = JustGetRight(parsify(TokenType.DOT), propertyKeyName);

    private static IParser<InputTokens, ASTNode, String> mapLiteral = JustGetMiddle(parsify(TokenType.LBRACE), Optional(Sequence(propertyKeyName, parsify(TokenType.COLON), expression, Star(Sequence(parsify(TokenType.COMMA), propertyKeyName, parsify(TokenType.COLON), expression)))), parsify(TokenType.LBRACE));




}
