package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.Combinators;
import cr.ac.una.eif400.cyphail.compiler.parser.IParser;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.Relationship.Direction;

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
                setClause(),
                deleteClause(),
                removeClause()
        );
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
     * setClause
     *     : "SET" setItem ("," setItem)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> setClause() {
        return TFlat(
                Sequence(
                        parsify(SET),
                        setItem(),
                        asNode(Star(JustGetRight(parsify(COMMA), setItem())))
                ),
                parts -> toSet(parts)
        );
    }

    private static IParser <InputTokens, AstNode, String> setItem(){

        return OrFlat(concatStringList,

                TFlat(
                        Sequence(propertyExpression(), parsify(EQUALS), expression()),
                        parts -> toSetProperty(parts)
                ),

                TFlat(
                        Sequence(variable(), parsify(EQUALS), expression()),
                        parts -> toSetAllProperties(parts)
                ),

                TFlat(
                        Sequence(variable(), nodeLabels()),
                        parts -> toSetLabels(parts)
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
     * removeClause
     *     : "REMOVE" removeItem ("," removeItem)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> removeClause() {
        return TFlat(
                Sequence(
                        parsify(REMOVE),
                        removeItem(),
                        asNode(Star(
                                JustGetRight(parsify(COMMA), removeItem())
                        ))
                ),
                parts -> toRemove(parts)
        );
    }

    /**
     * <pre>{@code
     * removeItem
     *     : variable nodeLabels     # RemoveLabels
     *     | propertyExpression      # RemoveProperty
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> removeItem() {
        return OrFlat(concatStringList,

                TFlat(
                        Sequence(variable(), nodeLabels()),
                        parts->toRemoveLabels(parts)
                ),
                TFlat(
                        Sequence(propertyExpression()),
                        parts->toRemoveProperty(parts)
                )
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
     * projectionBody
     *     : projectionItems projectionModifiers
     *     ;
     * }</pre>
     */
    private static String projectionBody() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * projectionItems
     *     : "*" ("," projectionItem)*
     *     | projectionItem ("," projectionItem)*
     *     ;
     * }</pre>
     */
    private static String projectionItems() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * projection : expression "AS" variable
     *            | expression
     * }</pre>
     * Equivale a {@code projectionItem} del .g4.
     */
    private static IParser<InputTokens, AstNode, String> projectionItem() {
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
     * projectionModifiers
     *     : order? skip_? limit_?
     *     ;
     * }</pre>
     */
    private static String projectionModifiers() {
        return "TO DO";
    }

    /**
     * <pre>{@code
     * order
     *     : "ORDER" "BY" sortItem ("," sortItem)*
     *     ;
     * }</pre>
     */
    private static String order() {
        return "TO DO";
    }

    /**
     * <pre>{@code
     * sortItem
     *     : expression ("ASCENDING" | "DESCENDING")?
     *     ;
     * }</pre>
     */
    private static String sortItem() {
        return "TO DO";
    }

    /**
     * <pre>{@code
     * skip_
     *     : "SKIP" expression
     *     ;
     * }</pre>
     */
    private static String skip_() {
        return "TO DO";
    }

    /**
     * <pre>{@code
     * limit_
     *     : "LIMIT" expression
     *     ;
     * }</pre>
     */
    private static String limit_() {
        return "TO DO";
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
     * patternPart
     *     :  patternElement     // No (variable "=")? patternElement
     *     ;
     * }</pre>
     */
    private static String patternPart() {
        return "TODO";
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
     * patternElementChain
     *     : relationshipPattern nodePattern
     *     ;
     * }</pre>
     */
    private static String patternElementChain() {
        return "TODO";
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
     * labelName : symbolicName
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> labelName() {
        return symbolicName();
    }

    /**
     * <pre>{@code
     * relationshipPattern : "-" relationshipDetail? ("->" | "-")
     *                     | "<-" relationshipDetail? "-"
     * }</pre>
     * Cada forma produce un {@code Relationship} con su {@link Direction}: {@code -...->} es {@code OUT},
     * {@code -...-} es {@code NONE} y {@code <-...-} es {@code IN}.
     */
    private static IParser<InputTokens, AstNode, String> relationshipPattern() {
        return OrFlat(concatStringList,
                TFlat(
                        Sequence(parsify(MINUS), orEmpty(relationshipDetail()), parsify(RIGHT_ARROW)),
                        parts -> toRelationship(parts, Direction.OUT)),
                TFlat(
                        Sequence(parsify(MINUS), orEmpty(relationshipDetail()), parsify(MINUS)),
                        parts -> toRelationship(parts, Direction.NONE)),
                TFlat(
                        Sequence(parsify(LEFT_ARROW), orEmpty(relationshipDetail()), parsify(MINUS)),
                        parts -> toRelationship(parts, Direction.IN)));
    }

    /**
     * <pre>{@code
     * relationshipDetail : "[" variable? relTypes? relRange? relProperties? "]"
     * }</pre>
     * Implementado: {@code "[" variable? relTypes? properties? "]"}. Todavía no admite {@code relRange}, y las
     * propiedades usan la misma regla {@code properties} de {@code nodePattern} en lugar de {@code relProperties}.
     */
    private static IParser<InputTokens, AstNode, String> relationshipDetail() {
        return TFlat(
                Sequence(
                        parsify(LBRACKET),
                        asNode(Optional(variable())),
                        orEmpty(relTypes()),
                        orEmpty(properties()),
                        parsify(RBRACKET)),
                parts -> toRelationshipDetail(parts));
    }


    /**
     * <pre>{@code
     * relTypes : ":" relTypeName ("|" relTypeName)*
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> relTypes() {
        return TFlat(
                Sequence(
                        parsify(COLON),
                        relTypeName(),
                        asNode(Star(JustGetRight(parsify(PIPE), relTypeName())))),
                parts -> toRelTypes(parts));
    }

    /**
     * <pre>{@code
     * relTypeName : symbolicName
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> relTypeName() {
        return symbolicName();
    }

    /**
     * <pre>{@code
     * relRange
     *     : "*" (integerLiteral (".." integerLiteral?)? | ".." integerLiteral)?
     *     ;
     * }</pre>
     */
    private static String relRange() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * relProperties
     *     : "{" (symbolicName ":" expression ("," symbolicName ":" expression)*)? "}"
     *     ;
     * }</pre>
     */
    private static String relProperties() {
        return "TODO";
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
     * orExpression
     *     : andExpression ("OR" andExpression)*
     *     ;
     * }</pre>
     */
    private static String orExpression() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * andExpression
     *     : notExpression ("AND" notExpression)*
     *     ;
     * }</pre>
     */
    private static String andExpression() {
        return "TODO";
    }


    /**
     * <pre>{@code
     * notExpression
     *     : "NOT"* comparisonExpression
     *     ;
     * }</pre>
     */
    private static String notExpression() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * comparisonExpression
     *     : addOrSubtractExpression (comparisonOperator addOrSubtractExpression)*
     *     ;
     * }</pre>
     */
    private static String comparisonExpression() {
        return "TODO";
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

    /**
     * <pre>{@code
     * addOrSubtractExpression
     *     : multiplyDivideModuloExpression (("+" | "-") multiplyDivideModuloExpression)*
     *     ;
     * }</pre>
     */
    private static String addOrSubtractExpression() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * multiplyDivideModuloExpression
     *     : unaryAddOrSubtractExpression (("*" | "/" | "%") unaryAddOrSubtractExpression)*
     *     ;
     * }</pre>
     */
    private static String multiplyDivideModuloExpression() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * unaryAddOrSubtractExpression
     *     : ("+" | "-")* nullTestExpression
     *     ;
     * }</pre>
     */
    private static String unaryAddOrSubtractExpression() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * nullTestExpression
     *     : propertyOrLabelsExpression ("IS" "NOT"? "NULL")?
     *     ;
     * }</pre>
     */
    private static String nullTestExpression() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * propertyOrLabelsExpression
     *     : atom (propertyLookup | nodeLabels)*
     *     ;
     * }</pre>
     */
    private static String propertyOrLabelsExpression() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * propertyLookup
     *     : "." propertyKeyName
     *     ;
     * }</pre>
     */
    private static String propertyLookup() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * propertyExpression
     *     : atom propertyLookup+
     *     ;
     * }</pre>
     */
    private static String propertyExpression() {
        return "TODO";
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
     * atom
     *     : literal
     *     | "(" expression ")"
     *     | functionInvocation
     *     | variable
     *     ;
     * }</pre>
     */
    private static String atom() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * functionInvocation
     *     : functionName "(" (expression ("," expression)*)? ")"
     *     ;
     * }</pre>
     */
    private static String functionInvocation() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * functionName
     *     : symbolicName
     *     ;
     * }</pre>
     */
    private static String functionName() {
        return "TODO";
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
     * variable : symbolicName
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> variable() {
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
     * numberLiteral
     *     : doubleLiteral
     *     | integerLiteral
     *     ;
     * }</pre>
     */
    private static String numberLiteral() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * booleanLiteral
     *     : "TRUE"
     *     | "FALSE"
     *     ;
     * }</pre>
     */
    private static String booleanLiteral() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * integerLiteral
     *     : DecimalInteger
     *     ;
     * }</pre>
     */
    private static String integerLiteral() {
        return "TODO";
    }

    /**
     * <pre>{@code
     * symbolicName : IDENTIFIER
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> symbolicName() {
        return parsify(TokenType.IDENTIFIER);
    }

    // A PARTIR DE AQUI LOS DE ABAJO NO SALEN EN GRAMATICA OFICIAL


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
     * properties : mapLiteral
     * }</pre>
     * Usada en {@code nodePattern} pero no definida en el .g4.
     */
    private static IParser<InputTokens, AstNode, String> properties() {
        return mapLiteral();
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



    private static IParser<InputTokens, AstNode, String> propertyExpression() {
        return TFlat(
                Sequence(variable(), parsify(DOT), propertyKeyName()),
                parts -> toPropertyAccess(parts)
        );
    }



}

