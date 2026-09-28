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

    private static IParser<InputTokens, AstNode, String> cyphail() {
        return JustGetLeft(regularQuery(), EOF());
    }

    private static IParser<InputTokens, AstNode, String> regularQuery() {
        return singleQuery();
    }

    private static IParser<InputTokens, AstNode, String> singleQuery() {
        return singlePartQuery();
    }

    private static IParser<InputTokens, AstNode, String> singlePartQuery() {
        return TFlat(
                Sequence(
                        asNode(Star(readingClause())),
                        asNode(Star(updatingClause())),
                        asNode(Optional(returnClause()))),
                parts -> toQuery(parts));
    }

    private static IParser<InputTokens, AstNode, String> readingClause() {
        return matchClause();
    }

    private static IParser<InputTokens, AstNode, String> matchClause() {
        return TFlat(
                Sequence(parsify(MATCH), pattern(), asNode(Optional(whereClause()))),
                parts -> toMatch(parts));
    }

    private static IParser<InputTokens, AstNode, String> whereClause() {
        return TFlat(
                Sequence(parsify(WHERE), expression()),
                parts -> toWhere(parts));
    }

    private static IParser<InputTokens, AstNode, String> createClause() {
        return TFlat(
                Sequence(parsify(CREATE), pattern()),
                parts -> toCreate(parts)
        );
    }

    private static IParser<InputTokens, AstNode, String> pattern() {
        return TFlat(
                Sequence(
                        patternElement(),
                        asNode(Star(JustGetRight(parsify(COMMA), patternElement())))),
                parts -> toPatterns(parts));
    }

    private static IParser<InputTokens, AstNode, String> patternElement() {
        return OrFlat(concatStringList,
                path(),
                nodePattern(),
                JustGetMiddle(parsify(TokenType.LPAREN), Lazy(() -> patternElement()), parsify(TokenType.RPAREN)));
    }

    private static IParser<InputTokens, AstNode, String> path() {
        return TFlat(
                Sequence(nodePattern(), relationshipPattern(), nodePattern()),
                parts -> toPath(parts));
    }

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

    private static IParser<InputTokens, AstNode, String> nodeLabels() {
        return asNode(Plus(JustGetRight(parsify(TokenType.COLON), labelName())));
    }

    private static IParser<InputTokens, AstNode, String> properties() {
        return mapLiteral();
    }

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

    private static IParser<InputTokens, AstNode, String> propertyEntry() {
        return TFlat(
                Sequence(propertyKeyName(), parsify(TokenType.COLON), expression()),
                parts -> toProperty(parts));
    }

    private static IParser<InputTokens, AstNode, String> variable() {
        return symbolicName();
    }

    private static IParser<InputTokens, AstNode, String> labelName() {
        return symbolicName();
    }

    private static IParser<InputTokens, AstNode, String> propertyKeyName() {
        return symbolicName();
    }

    private static IParser<InputTokens, AstNode, String> symbolicName() {
        return parsify(TokenType.IDENTIFIER);
    }

    private static IParser<InputTokens, AstNode, String> relationshipPattern() {
        return source -> new Fail<>("not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> updatingClause() {
        return OrFlat(concatStringList,
                createClause(),
                deleteClause()
        );
    }

    private static IParser<InputTokens, AstNode, String> returnClause() {
        return TFlat(
                Sequence(parsify(RETURN), projection(),
                        asNode(Star(JustGetRight(parsify(COMMA), projection())))),
                parts -> toReturn(parts));
    }

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

    private static IParser<InputTokens, AstNode, String> deleteClause() {
        return TFlat(
                Sequence(
                        asNode(Optional(parsify(DETACH))), parsify(DELETE), expression(), asNode(Star(JustGetRight(parsify(COMMA), expression())))
                ), parts -> toDelete(parts)
        );
    }

    private static IParser<InputTokens, AstNode, String> expression() {
        return TFlat(
                Sequence(
                        simple(),
                        asNode(Optional(asNode(Sequence(comparisonOperator(), simple()))))
                ),
                parts -> toExpression(parts)
        );
    }

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

    private static IParser<InputTokens, AstNode, String> name() {
        return symbolicName();
    }

    private static IParser<InputTokens, AstNode, String> literal() {
        return OrFlat(concatStringList,
                parsify(NUMBER),
                parsify(STRING_LITERAL)
        );
    }

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

    private static IParser<InputTokens, AstNode, String> setClause() {
        return source -> new Fail<>("'Set Clause' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> setItem() {
        return source -> new Fail<>("'Set Item' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> removeClause() {
        return source -> new Fail<>("'Remove Clause' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> removeItem() {
        return source -> new Fail<>("'Remove Item' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> projectionBody() {
        return source -> new Fail<>("'Projection Body' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> projectionItems() {
        return source -> new Fail<>("'Projection Items' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> projectionItem() {
        return source -> new Fail<>("'Projection Item' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> projectionModifiers() {
        return source -> new Fail<>("'Projection Modifiers' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> order() {
        return source -> new Fail<>("'Order' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> sortItem() {
        return source -> new Fail<>("'Sort Item' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> skip() {
        return source -> new Fail<>("'Skip' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> limit() {
        return source -> new Fail<>("'Limit' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> relationshipDetail() {
        return source -> new Fail<>("'Relationship Detail' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> relTypes() {
        return source -> new Fail<>("'Rel Types' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> relTypeName() {
        return source -> new Fail<>("'Rel Type Name' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> relRange() {
        return source -> new Fail<>("'Rel Range' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> relProperties() {
        return source -> new Fail<>("'Rel Properties' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> orExpression() {
        return source -> new Fail<>("'Or Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> andExpression() {
        return source -> new Fail<>("'And Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> notExpression() {
        return source -> new Fail<>("'Not Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> comparisonExpression() {
        return source -> new Fail<>("'Comparison Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> addOrSubtractExpression() {
        return source -> new Fail<>("'Add Or Subtract Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> multiplyDivideModuloExpression() {
        return source -> new Fail<>("'Multiply Divide Modulo Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> unaryAddOrSubtractExpression() {
        return source -> new Fail<>("'Unary Add Or Subtract Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> nullTestExpression() {
        return source -> new Fail<>("'Null Test Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> propertyOrLabelsExpression() {
        return source -> new Fail<>("'Property Or Labels Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> propertyLookup() {
        return source -> new Fail<>("'Property Lookup' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> propertyExpression() {
        return source -> new Fail<>("'Property Expression' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> atom() {
        return source -> new Fail<>("'Atom' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> functionInvocation() {
        return source -> new Fail<>("'Function Invocation' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> functionName() {
        return source -> new Fail<>("'Function Name' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> numberLiteral() {
        return source -> new Fail<>("'Number Literal' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> doubleLiteral() {
        return source -> new Fail<>("'Double Literal' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> booleanLiteral() {
        return source -> new Fail<>("'Boolean Literal' is not implemented yet");
    }

    private static IParser<InputTokens, AstNode, String> integerLiteral() {
        return source -> new Fail<>("'Integer Literal' is not implemented yet");
    }
}

