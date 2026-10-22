package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.AstBuilders;
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
                AstBuilders::toQuery);
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
     * updatingClause
     *     : createClause
     *     | setClause
     *     | deleteClause
     *     | removeClause
     *     ;
     * }</pre>
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
                AstBuilders::toMatch);
    }

    /**
     * <pre>{@code
     * whereClause : "WHERE" expression
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> whereClause() {
        return TFlat(
                Sequence(parsify(WHERE), expression()),
                AstBuilders::toWhere);
    }

    /**
     * <pre>{@code
     * createClause : "CREATE" pattern
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> createClause() {
        return TFlat(
                Sequence(parsify(CREATE), pattern()),
                AstBuilders::toCreate
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
                Sequence(parsify(SET), setItem(), asNode(Star(JustGetRight(parsify(COMMA), setItem())))),
                AstBuilders::toSet
        );
    }

    /**
     * <pre>{@code
     * setItem
     *     : propertyExpression "=" expression   # SetProperty
     *     | variable "=" expression             # SetAllProperties
     *     | variable nodeLabels                 # SetLabels
     *     ;
     * }</pre>
     */
    private static IParser <InputTokens, AstNode, String> setItem(){

        return OrFlat(concatStringList,

                TFlat(
                        Sequence(propertyExpression(), parsify(EQUALS), expression()),
                        AstBuilders::toSetProperty
                ),

                TFlat(
                        Sequence(variable(), parsify(EQUALS), expression()),
                        AstBuilders::toSetAllProperties
                ),

                TFlat(
                        Sequence(variable(), nodeLabels()),
                        AstBuilders::toSetLabels
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
                ), AstBuilders::toDelete
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
                AstBuilders::toRemove
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
                        AstBuilders::toRemoveLabels
                ),
                TFlat(
                        Sequence(propertyExpression()),
                        AstBuilders::toRemoveProperty
                )
        );
    }

    /**
     * <pre>{@code
     * returnClause : "RETURN" "DISTINCT"? projectionBody
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> returnClause() {
        return TFlat(
                Sequence(parsify(RETURN), asNode(Optional(parsify(DISTINCT))), projectionBody()),
                AstBuilders::toReturn);
    }

    /**
     * <pre>{@code
     * projectionBody
     *     : projectionItems projectionModifiers
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> projectionBody() {
        return asNode(Sequence(projectionItems(), projectionModifiers()));
    }

    /**
     * <pre>{@code
     * projectionItems
     *     : "*" ("," projectionItem)*
     *     | projectionItem ("," projectionItem)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> projectionItems() {
        return OrFlat(concatStringList,
                TFlat(
                        Sequence(parsify(MULTIPLY), asNode(Star(JustGetRight(parsify(COMMA), projectionItem())))),
                        AstBuilders::toItems),
                TFlat(
                        Sequence(projectionItem(), asNode(Star(JustGetRight(parsify(COMMA), projectionItem())))),
                        AstBuilders::toItems));
    }

    /**
     * <pre>{@code
     * projection : expression "AS" variable
     *            | expression
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> projectionItem() {
        return OrFlat(concatStringList,
                TFlat(
                        Sequence(expression(), parsify(AS), variable()),
                        AstBuilders::toProjection
                ),
                TFlat(
                        Sequence(expression()),
                        AstBuilders::toProjection
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
    private static IParser<InputTokens, AstNode, String> projectionModifiers() {
        return TFlat(
                Sequence(asNode(Optional(order())), asNode(Optional(skip_())), asNode(Optional(limit_()))),
                AstBuilders::toModifiers);
    }

    /**
     * <pre>{@code
     * order
     *     : "ORDER" "BY" sortItem ("," sortItem)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> order() {
        return TFlat(
                Sequence(
                        parsify(ORDER), parsify(BY), sortItem(),
                        asNode(Star(JustGetRight(parsify(COMMA), sortItem())))),
                AstBuilders::toOrder);
    }

    /**
     * <pre>{@code
     * sortItem
     *     : expression ("ASCENDING" | "DESCENDING")?
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> sortItem() {
        return TFlat(
                Sequence(
                        expression(),
                        asNode(Optional(OrFlat(concatStringList, parsify(ASCENDING), parsify(DESCENDING))))),
                AstBuilders::toSortItem);
    }

    /**
     * <pre>{@code
     * skip_
     *     : "SKIP" expression
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> skip_() {
        return TFlat(Sequence(parsify(SKIP), expression()), AstBuilders::toSkip);
    }

    /**
     * <pre>{@code
     * limit_
     *     : "LIMIT" expression
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> limit_() {
        return TFlat(Sequence(parsify(LIMIT), expression()), AstBuilders::toLimit);
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
                AstBuilders::toPatterns);
    }

    /**   CAMBIAR
     * <pre>{@code
     * patternElement : nodePattern (relationshipPattern nodePattern)?
     *                | LPAREN patternElement RPAREN
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> patternElement() {
        return OrFlat(concatStringList,
                TFlat(
                        Sequence(nodePattern(), asNode(Optional(patternElementChain()))),
                        AstBuilders::toPatternElement),
                JustGetMiddle(parsify(LPAREN), Lazy(Grammar::patternElement), parsify(RPAREN)));
    }

    /**
     * <pre>{@code
     * patternElementChain
     *     : relationshipPattern nodePattern
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> patternElementChain() {
        return asNode(Sequence(relationshipPattern(), nodePattern()));
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
                AstBuilders::toPattern);
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
     */
    private static IParser<InputTokens, AstNode, String> relationshipDetail() {
        return TFlat(
                Sequence(
                        parsify(LBRACKET),
                        asNode(Optional(variable())),
                        orEmpty(relTypes()),
                        asNode(Optional(relRange())),
                        orEmpty(relProperties()),
                        parsify(RBRACKET)),
                AstBuilders::toRelationshipDetail);
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
                AstBuilders::toRelTypes);
    }

    /**
     * <pre>{@code
     * relTypeName : symbolicName
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> relTypeName() {
        return symbolicName();
    }

    /**                               PONER OJO PARA CAMBIAR
     * <pre>{@code
     * relRange
     *     : "*" (integerLiteral (".." integerLiteral?)? | ".." integerLiteral)?
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> relRange() {
        return TFlat(
                Sequence(
                        parsify(MULTIPLY),
                        asNode(Optional(OrFlat(concatStringList,
                                asNode(Sequence(
                                        integerLiteral(),
                                        asNode(Optional(asNode(Sequence(
                                                parsify(RANGE_DOTS), asNode(Optional(integerLiteral())))))))),
                                asNode(Sequence(parsify(RANGE_DOTS), integerLiteral())))))),
                parts -> toRelRange(parts));
    }

    /**
     * <pre>{@code
     * relProperties
     *     : "{" (symbolicName ":" expression ("," symbolicName ":" expression)*)? "}"
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> relProperties() {
        return mapLiteral();
    }


    /**                      CAMBIAR
     * <pre>{@code
     * expression : orExpression
     * }</pre>
     * */
    private static IParser<InputTokens, AstNode, String> expression() {
        return orExpression();
    }

    /**
     * <pre>{@code
     * orExpression
     *     : andExpression ("OR" andExpression)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> orExpression() {
        return TFlat(
                Sequence(andExpression(), asNode(Star(JustGetRight(parsify(OR), andExpression())))),
                AstBuilders::toOr
        );
    }

    /**
     * <pre>{@code
     * andExpression
     *     : notExpression ("AND" notExpression)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> andExpression() {
        return TFlat(
                Sequence(notExpression(), asNode(Star(JustGetRight(parsify(AND), notExpression())))),
                AstBuilders::toAnd
        );
    }


    /**
     * <pre>{@code
     * notExpression
     *     : "NOT"* comparisonExpression
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> notExpression() {
        return TFlat(
                Sequence(asNode(Star(parsify(NOT))), comparisonExpression()),
                AstBuilders::toNot
        );
    }

    /**
     * <pre>{@code
     * comparisonExpression
     *     : addOrSubtractExpression (comparisonOperator addOrSubtractExpression)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> comparisonExpression() {
        return TFlat(
                Sequence(
                    addOrSubtractExpression(),
                        asNode(Star(asNode(Sequence(comparisonOperator(), addOrSubtractExpression()))))
                ),
                AstBuilders::toComparison
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

    /**
     * <pre>{@code
     * addOrSubtractExpression
     *     : multiplyDivideModuloExpression (("+" | "-") multiplyDivideModuloExpression)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> addOrSubtractExpression() {
        return TFlat(
                Sequence(
                        multiplyDivideModuloExpression(),
                        asNode(Star(asNode(Sequence(
                                OrFlat(
                                        concatStringList,
                                        parsify(PLUS),
                                        parsify(MINUS)
                                ),
                                multiplyDivideModuloExpression()
                        ))))
                ),
                AstBuilders::toArithmetic
        );
    }

    /**
     * <pre>{@code
     * multiplyDivideModuloExpression
     *     : unaryAddOrSubtractExpression (("*" | "/" | "%") unaryAddOrSubtractExpression)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> multiplyDivideModuloExpression() {
        return TFlat(
                Sequence(
                        unaryAddOrSubtractExpression(),
                        asNode(Star(asNode(Sequence(
                                OrFlat(concatStringList, parsify(MULTIPLY), parsify(DIVIDE), parsify(MODULO)),
                                unaryAddOrSubtractExpression()))))
                ),
                AstBuilders::toArithmetic
        );
    }

    /**
     * <pre>{@code
     * unaryAddOrSubtractExpression
     *     : ("+" | "-")* nullTestExpression
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> unaryAddOrSubtractExpression() {
        return TFlat(
                Sequence(
                        asNode(Star(OrFlat(concatStringList, parsify(PLUS), parsify(MINUS)))),
                        nullTestExpression()),
                AstBuilders::toUnary);
    }

    /**
     * <pre>{@code
     * nullTestExpression
     *     : propertyOrLabelsExpression ("IS" "NOT"? "NULL")?
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> nullTestExpression() {
        return TFlat(
                Sequence(
                        propertyOrLabelsExpression(),
                        asNode(Optional(asNode(Sequence(
                                parsify(IS), asNode(Optional(parsify(NOT))), parsify(NULL)))))),
                AstBuilders::toNullTest);
    }

    /**
     * <pre>{@code
     * propertyOrLabelsExpression
     *     : atom (propertyLookup | nodeLabels)*
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> propertyOrLabelsExpression() {
        return TFlat(
                Sequence(
                        atom(),
                        asNode(Star(OrFlat(concatStringList, propertyLookup(), nodeLabels())))),
                AstBuilders::toPropertyOrLabels);
    }

    /**
     * <pre>{@code
     * propertyLookup
     *     : "." propertyKeyName
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> propertyLookup() {
        return JustGetRight(parsify(DOT), propertyKeyName());
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
    private static IParser<InputTokens, AstNode, String> atom() {
        return OrFlat(concatStringList,
                literal(),
                JustGetMiddle(parsify(LPAREN), Lazy(Grammar::expression), parsify(RPAREN)),
                functionInvocation(),
                variable());
    }

    /**
     * <pre>{@code
     * functionInvocation
     *     : functionName "(" (expression ("," expression)*)? ")"
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> functionInvocation() {
        return TFlat(
                Sequence(
                        functionName(),
                        parsify(LPAREN),
                        orEmpty(TFlat(
                                Sequence(
                                        Lazy(Grammar::expression),
                                        asNode(Star(JustGetRight(parsify(COMMA), Lazy(Grammar::expression))))),
                                AstBuilders::toItems)),
                        parsify(RPAREN)),
                AstBuilders::toFunctionCall);
    }

    /**
     * <pre>{@code
     * functionName
     *     : symbolicName
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> functionName() {
        return symbolicName();
    }

    /**                       CAMBIAR
     * <pre>{@code
     * mapLiteral : "{" (propertyKeyName ":" expression ("," propertyKeyName ":" expression)*)? "}"
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> mapLiteral() {
        return JustGetMiddle(
                parsify(LBRACE),
                orEmpty(TFlat(
                        Sequence(
                                propertyKeyName(), parsify(COLON), expression(),
                                asNode(Star(JustGetRight(
                                        parsify(COMMA),
                                        asNode(Sequence(propertyKeyName(), parsify(COLON), expression())))))),
                        AstBuilders::toMapLiteral)),
                parsify(RBRACE));
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
     */
    private static IParser<InputTokens, AstNode, String> literal() {
        return OrFlat(concatStringList,
                parsify(NUMBER),
                parsify(STRING_LITERAL),
                booleanLiteral(),
                parsify(NULL)
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
    private static IParser<InputTokens, AstNode, String> numberLiteral() {
        return parsify(NUMBER);
    }

    /**
     * <pre>{@code
     * booleanLiteral
     *     : "TRUE"
     *     | "FALSE"
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> booleanLiteral() {
        return OrFlat(
                concatStringList,
                parsify(TRUE),
                parsify(FALSE)
        );
    }

    /**
     * <pre>{@code
     * integerLiteral
     *     : DecimalInteger
     *     ;
     * }</pre>
     */
    private static IParser<InputTokens, AstNode, String> integerLiteral() {
        return parsify(NUMBER);
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
     * properties : mapLiteral
     * }</pre>
     * Usada en {@code nodePattern} pero no definida en el .g4.
     */
    private static IParser<InputTokens, AstNode, String> properties() {
        return mapLiteral();
    }





    private static IParser<InputTokens, AstNode, String> propertyExpression() {
        return TFlat(
                Sequence(atom(), asNode(Plus(propertyLookup()))),
                AstBuilders::toPropertyOrLabels);
    }



}

