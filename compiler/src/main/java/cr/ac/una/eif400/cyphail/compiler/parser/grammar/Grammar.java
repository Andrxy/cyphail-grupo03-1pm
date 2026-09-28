package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.Combinators;
import cr.ac.una.eif400.cyphail.compiler.parser.IParser;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ast.AstBuilders.*;
import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.*;
import static cr.ac.una.eif400.cyphail.compiler.parser.Combinators.*;
import static cr.ac.una.eif400.cyphail.compiler.parser.grammar.GrammarHelpers.*;

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
public class Grammar {
    public static Result<InputTokens, AstNode, String> parse(List<TokenString> source) {
        return cyphail().parse(new InputTokens(source));
    }

    /**
     * <pre>{@code
     * cyphail : regularQuery EOF
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> cyphail() {
        return JustGetLeft(regularQuery(), EOF());
    }

    /**
     * <pre>{@code
     * regularQuery : singleQuery
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> regularQuery() {
        return singleQuery();
    }

    /**
     * <pre>{@code
     * singleQuery : singlePartQuery
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> singleQuery() {
        return singlePartQuery();
    }

    /**
     * <pre>{@code
     * singlePartQuery : readingClause* updatingClause* returnClause?
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> singlePartQuery() {
        return TFlat(
                Sequence(
                        asNode(Star(readingClause())),
                        asNode(Star(updatingClause())),
                        asNode(Optional(returnClause()))),
                parts -> toQuery(parts));
    }

    /**
     * <pre>{@code
     * readingClause : matchClause
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> readingClause() {
        return matchClause();
    }

    /**
     * <pre>{@code
     * matchClause : "MATCH" pattern whereClause?
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> matchClause() {
        return TFlat(
                Sequence(parsify(MATCH), pattern(), asNode(Optional(whereClause()))),
                parts -> toMatch(parts));
    }

    /**
     * <pre>{@code
     * whereClause : "WHERE" expression
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> whereClause() {
        return TFlat(
                Sequence(parsify(WHERE), expression()),
                parts -> toWhere(parts));
    }

    /**
     * <pre>{@code
     * createClause : "CREATE" pattern
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> createClause() {
        return TFlat(
                Sequence(parsify(CREATE), pattern()),
                parts -> toCreate(parts)
        );
    }

    /**
     * <pre>{@code
     * pattern : patternElement ("," patternElement)*
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> pattern() {
        return TFlat(
                Sequence(
                        patternElement(),
                        asNode(Star(JustGetRight(parsify(COMMA), patternElement())))),
                parts -> toPatterns(parts));
    }

    /**
     * <pre>{@code
     * patternElement : nodePattern (relationshipPattern nodePattern)?
     *                | LPAREN patternElement RPAREN
     * }</pre>
     * Implementado: {@code path | nodePattern | "(" patternElement ")"}
     */
    private static IParser<InputTokens, AstNode, String> patternElement() {
        return OrFlat(concatStringList,
                path(),
                nodePattern(),
                JustGetMiddle(parsify(TokenType.LPAREN), Lazy(() -> patternElement()), parsify(TokenType.RPAREN)));
    }

    /**
     * <pre>{@code
     * path : nodePattern relationshipPattern nodePattern
     * }</pre>
     * Primera alternativa de {@code patternElement}, separada como regla propia.
     */
    private static IParser<InputTokens, AstNode, String> path() {
        return TFlat(
                Sequence(nodePattern(), relationshipPattern(), nodePattern()),
                parts -> toPath(parts));
    }

    /**
     * <pre>{@code
     * nodePattern : "(" variable? nodeLabels? properties? ")"
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> nodePattern() {
        return TFlat(
           Sequence(
              parsify(TokenType.LPAREN),
                   asNode(Optional(variable())),
                        orEmpty(nodeLabels()),
                        orEmpty(properties()),
                        parsify(TokenType.RPAREN)),
                parts -> toPattern(parts));
    }

    /**
     * <pre>{@code
     * nodeLabels : (":" labelName)+
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> nodeLabels() {
        return asNode(Plus(JustGetRight(parsify(TokenType.COLON), labelName())));
    }

    /**
     * <pre>{@code
     * properties : mapLiteral
     * }</pre>
     * Usada en {@code nodePattern} pero no definida en el .g4.
     */
    private static IParser<InputTokens, AstNode, String> properties() {
        return mapLiteral();
    }

    /**
     * <pre>{@code
     * mapLiteral : "{" (propertyKeyName ":" expression ("," propertyKeyName ":" expression)*)? "}"
     * }</pre>
     * Implementado: {@code "{" (propertyEntry ("," propertyEntry)*)? "}"}
     */
    private static IParser<InputTokens, AstNode, String> mapLiteral() {
        return JustGetMiddle(
                parsify(TokenType.LBRACE),
                orEmpty(TFlat(
                        Sequence(
                                propertyEntry(),
                                asNode(Star(JustGetRight(parsify(TokenType.COMMA), propertyEntry())))),
                        parts -> toProperties(parts))),
                parsify(TokenType.RBRACE));
    }

    /**
     * <pre>{@code
     * propertyEntry : propertyKeyName ":" expression
     * }</pre>
     * Fragmento de {@code mapLiteral}, separado como regla propia.
     */
    private static IParser<InputTokens, AstNode, String> propertyEntry() {
        return TFlat(
                Sequence(propertyKeyName(), parsify(TokenType.COLON), expression()),
                parts -> toProperty(parts));
    }

    /**
     * <pre>{@code
     * variable : symbolicName
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> variable() {
        return symbolicName();
    }

    /**
     * <pre>{@code
     * labelName : symbolicName
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> labelName() {
        return symbolicName();
    }

    /**
     * <pre>{@code
     * propertyKeyName : symbolicName
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> propertyKeyName() {
        return symbolicName();
    }

    /**
     * <pre>{@code
     * symbolicName : IDENTIFIER
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> symbolicName() {
        return parsify(TokenType.IDENTIFIER);
    }

    /**
     * <pre>{@code
     * relationshipPattern : "-" relationshipDetail? ("->" | "-")
     *                     | "<-" relationshipDetail? "-"
     * }</pre>
     * Implementado: pendiente.
     */
    private static IParser<InputTokens, AstNode, String> relationshipPattern() {
        return source -> new Fail<>("not implemented yet");
    }

    /**
     * <pre>{@code
     * updatingClause : createClause
     *                | setClause
     *                | deleteClause
     *                | removeClause
     * }</pre>
     * Implementado: {@code createClause | deleteClause}
     */
    private static IParser<InputTokens, AstNode, String> updatingClause() {
        return OrFlat(concatStringList,
                createClause(),
                deleteClause()
        );
    }

    /**
     * <pre>{@code
     * returnClause : "RETURN" "DISTINCT"? projectionBody
     * }</pre>
     * Implementado: {@code "RETURN" projection ("," projection)*}
     */
    private static IParser<InputTokens, AstNode, String> returnClause() {
        return TFlat(
                Sequence(parsify(RETURN), projection(),
                        asNode(Star(JustGetRight(parsify(COMMA), projection())))),
                parts -> toReturn(parts));
    }

    /**
     * <pre>{@code
     * projection : expression "AS" variable
     *            | expression
     * }</pre>
     * Equivale a {@code projectionItem} del .g4.
     */
    private static IParser<InputTokens, AstNode, String> projection() {
        return OrFlat(concatStringList,
                TFlat(
                        Sequence(expression(), parsify(AS), variable()),
                        parts -> toProjection(parts)
                ),
                TFlat(
                        Sequence(expression()),
                        parts -> toProjection(parts)
                )
        );
    }

    /**
     * <pre>{@code
     * deleteClause : "DETACH"? "DELETE" expression ("," expression)*
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> deleteClause() {
        return TFlat(
                Sequence(
                        asNode(Optional(parsify(DETACH))), parsify(DELETE), expression(), asNode(Star(JustGetRight(parsify(COMMA), expression())))
                ), parts -> toDelete(parts)
        );
    }

    /**
     * <pre>{@code
     * expression : orExpression
     * }</pre>
     * Implementado: {@code simple (comparisonOperator simple)?}
     */
    private static IParser<InputTokens, AstNode, String> expression() {
        return TFlat(
                Sequence(
                        simple(),
                        asNode(Optional(asNode(Sequence(comparisonOperator(), simple()))))
                ),
                parts -> toExpression(parts)
        );
    }

    /**
     * <pre>{@code
     * simple : variable "." name
     *        | literal
     *        | variable
     * }</pre>
     * Regla propia que reemplaza a {@code atom} y {@code propertyExpression} con un solo acceso a propiedad.
     */
    private static IParser<InputTokens, AstNode, String> simple() {
        return OrFlat(concatStringList,
                TFlat(
                        Sequence(variable(), parsify(DOT), name()),
                        parts -> toPropertyAccess(parts)
                ),
                literal(),
                variable()
        );
    }

    /**
     * <pre>{@code
     * name : symbolicName
     * }</pre>
     * Regla propia, equivale a {@code propertyKeyName}.
     */
    private static IParser<InputTokens, AstNode, String> name() {
        return symbolicName();
    }

    /**
     * <pre>{@code
     * literal : numberLiteral
     *         | StringLiteral
     *         | booleanLiteral
     *         | "NULL"
     * }</pre>
     * Implementado: {@code NUMBER | STRING_LITERAL}
     */
    private static IParser<InputTokens, AstNode, String> literal() {
        return OrFlat(concatStringList,
                parsify(NUMBER),
                parsify(STRING_LITERAL)
        );
    }

    /**
     * <pre>{@code
     * comparisonOperator : "=" | "<>" | "<" | ">" | "<=" | ">="
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> comparisonOperator() {
        return OrFlat(concatStringList,
                parsify(EQUALS),
                parsify(NOTEQUALS),
                parsify(LESS_THAN),
                parsify(GREATER_THAN),
                parsify(LESS_THAN_OR_EQUAL),
                parsify(GREATER_THAN_OR_EQUAL)
        );
    }
}

