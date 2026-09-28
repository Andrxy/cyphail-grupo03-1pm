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
 * Gramatica de Cyphail: convierte la lista de tokens en un AST.
 *
 * <p>Sigue la gramatica del curso (EIF400-II-2026-GrammarCypherSprint1.g4) recortada al
 * alcance que fija el archivo de casos de prueba: solo patrones de nodo, varios patrones
 * separados por coma, comparaciones entre expresiones simples, y sin MATCH anidados.</p>
 *
 * <p>Reglas implementadas:</p>
 *
 * <pre>
 * cyphail        : singlePartQuery EOF
 * singlePartQuery: matchClause* updatingClause* returnClause?
 * matchClause    : MATCH pattern whereClause?
 * whereClause    : WHERE expression
 * updatingClause : createClause | deleteClause
 * createClause   : CREATE pattern
 * deleteClause   : DETACH? DELETE expression ("," expression)*
 * returnClause   : RETURN projection ("," projection)*
 * projection     : expression AS variable | expression
 * pattern        : patternElement ("," patternElement)*
 * patternElement : path | nodePattern | "(" patternElement ")"
 * nodePattern    : "(" variable? nodeLabels? properties? ")"
 * nodeLabels     : (":" labelName)+
 * properties     : mapLiteral
 * mapLiteral     : "{" (propertyEntry ("," propertyEntry)*)? "}"
 * propertyEntry  : propertyKeyName ":" expression
 * expression     : simple (comparisonOperator simple)?
 * simple         : variable "." name | literal | variable
 * literal        : NUMBER | STRING_LITERAL
 * symbolicName   : IDENTIFIER
 * </pre>
 *
 * <p>Cada regla de arriba es un metodo de esta clase, con el mismo nombre. Se construyen con
 * los combinadores de {@link Combinators}: {@code Sequence} para el orden, {@code OrFlat} para
 * las alternativas, {@code Plus} y {@code Star} para las repeticiones, {@code Optional} para
 * lo que puede faltar, y {@code TFlat} para convertir lo reconocido en un nodo del AST
 * mediante los constructores de {@code AstBuilders}.</p>
 *
 * <p>Las reglas son metodos y no campos para permitir la recursion: {@code patternElement}
 * se referencia a si misma a traves de {@link Combinators#Lazy}, que retrasa la construccion
 * del parser hasta el momento de usarlo. Con campos estaticos eso no seria posible, porque
 * un campo no puede referirse a otro que todavia no se ha inicializado.</p>
 *
 * <p>El resto de reglas de la gramatica del curso estan declaradas al final de la clase como
 * marcadores que devuelven un fallo. Estan fuera del alcance de este sprint y sirven de mapa
 * de lo que faltaria implementar.</p>
 */
public class Grammar {

    /**
     * Punto de entrada: parsea una consulta completa.
     *
     * @param source tokens producidos por el Tokenizer.
     * @return el AST de la consulta, o el motivo del fallo.
     */
    public static Result<InputTokens, AstNode, String> parse(List<TokenString> source) {
        return cyphail().parse(new InputTokens(source));
    }

    /**
     * cyphail : singlePartQuery EOF
     *
     * <p>Regla raiz. El {@code EOF} exige que no sobren tokens: sin el, una consulta como
     * {@code MATCH (a) (b)} reconoceria solo su primera mitad y el resto se perderia
     * en silencio, sin dar error.</p>
     */
    private static IParser<InputTokens, AstNode, String> cyphail() {
        return JustGetLeft(regularQuery(), EOF());
    }

    /**
     * regularQuery : singleQuery
     *
     * <p>En la gramatica del curso esta regla tambien admite {@code UNION}, que queda
     * fuera del alcance de este sprint. Se conserva el nivel para que la estructura
     * siga calcando la gramatica original.</p>
     */
    private static IParser<InputTokens, AstNode, String> regularQuery() {
        return singleQuery();
    }

    /**
     * singleQuery : singlePartQuery
     *
     * <p>La gramatica del curso admite aqui consultas de varias partes encadenadas con
     * {@code WITH}, que queda fuera del alcance de este sprint.</p>
     */
    private static IParser<InputTokens, AstNode, String> singleQuery() {
        return singlePartQuery();
    }

    /**
     * singlePartQuery : matchClause* updatingClause* returnClause?
     *
     * <p>Las tres partes se reconocen en ese orden y se juntan en un nodo {@code Query}.</p>
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
     * readingClause : matchClause
     *
     * <p>La gramatica del curso admite aqui otras clausulas de lectura como
     * {@code UNWIND}, que quedan fuera del alcance de este sprint.</p>
     */
    private static IParser<InputTokens, AstNode, String> readingClause() {
        return matchClause();
    }

    /**
     * matchClause : MATCH pattern whereClause?
     *
     * <p>El {@code WHERE} es opcional: {@code Optional} devuelve una lista vacia cuando
     * no aparece, que es como el nodo {@code Match} representa su ausencia.</p>
     */
    private static IParser<InputTokens, AstNode, String> matchClause() {
        return TFlat(
                Sequence(parsify(MATCH), pattern(), asNode(Optional(whereClause()))),
                parts -> toMatch(parts));
    }

    /**
     * whereClause : WHERE expression
     */
    private static IParser<InputTokens, AstNode, String> whereClause() {
        return TFlat(
                Sequence(parsify(WHERE), expression()),
                parts -> toWhere(parts));
    }

    /**
     * createClause : CREATE pattern
     */
    private static IParser<InputTokens, AstNode, String> createClause() {
        return TFlat(
                Sequence(parsify(CREATE), pattern()),
                parts -> toCreate(parts)
        );
    }

    /**
     * pattern : patternElement ("," patternElement)*
     *
     * <p>Uno o varios patrones separados por coma. Es lo que permite
     * {@code MATCH (m:Movie), (p:Person)}.</p>
     */
    private static IParser<InputTokens, AstNode, String> pattern() {
        return TFlat(
                Sequence(
                        patternElement(),
                        asNode(Star(JustGetRight(parsify(COMMA), patternElement())))),
                parts -> toPatterns(parts));
    }

    /**
     * patternElement : path | nodePattern | "(" patternElement ")"
     *
     * <p>El orden de las alternativas importa: {@code path} va primero porque empieza igual
     * que {@code nodePattern} y solo se distingue al llegar a la relacion. Si estuviera
     * despues, {@code nodePattern} reconoceria el primer nodo y la relacion quedaria
     * sin consumir.</p>
     *
     * <p>La tercera alternativa es recursiva, por eso usa {@link Combinators#Lazy}: sin el,
     * la llamada se evaluaria al construir el parser y entraria en un ciclo infinito.</p>
     */
    private static IParser<InputTokens, AstNode, String> patternElement() {
        return OrFlat(concatStringList,
                path(),
                nodePattern(),
                JustGetMiddle(parsify(TokenType.LPAREN), Lazy(() -> patternElement()), parsify(TokenType.RPAREN)));
    }

    /**
     * path : nodePattern relationshipPattern nodePattern
     *
     * <p>Dos nodos unidos por una relacion. Depende de {@link #relationshipPattern()},
     * que aun no esta implementado, asi que hoy esta regla siempre falla y
     * {@link #patternElement()} cae en la alternativa de nodo suelto.</p>
     */
    private static IParser<InputTokens, AstNode, String> path() {
        return TFlat(
                Sequence(nodePattern(), relationshipPattern(), nodePattern()),
                parts -> toPath(parts));
    }

    /**
     * nodePattern : "(" variable? nodeLabels? properties? ")"
     *
     * <p>Las tres partes internas son opcionales, asi que {@code ()} es un patron valido.
     * {@code orEmpty} devuelve un nodo vacio cuando la parte no aparece, para que la
     * {@code Sequence} siga teniendo el mismo numero de elementos en todos los casos.</p>
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
     * nodeLabels : (":" labelName)+
     *
     * <p>Una o mas etiquetas, cada una precedida de dos puntos. {@code JustGetRight}
     * descarta el token de los dos puntos y conserva solo el nombre. Es lo que permite
     * {@code (a:Person:Employee)}.</p>
     */
    private static IParser<InputTokens, AstNode, String> nodeLabels() {
        return asNode(Plus(JustGetRight(parsify(TokenType.COLON), labelName())));
    }

    /**
     * properties : mapLiteral
     */
    private static IParser<InputTokens, AstNode, String> properties() {
        return mapLiteral();
    }

    /**
     * mapLiteral : "{" (propertyEntry ("," propertyEntry)*)? "}"
     *
     * <p>{@code JustGetMiddle} descarta las llaves y conserva solo el contenido.
     * El {@code orEmpty} interior permite el mapa vacio {@code {}}.</p>
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
     * propertyEntry : propertyKeyName ":" expression
     *
     * <p>El valor es una expresion, no solo un literal. Por eso funciona
     * {@code {personId: p.id}}, donde el valor es el acceso a una propiedad de otro nodo.</p>
     */
    private static IParser<InputTokens, AstNode, String> propertyEntry() {
        return TFlat(
                Sequence(propertyKeyName(), parsify(TokenType.COLON), expression()),
                parts -> toProperty(parts));
    }

    /**
     * variable : symbolicName
     */
    private static IParser<InputTokens, AstNode, String> variable() {
        return symbolicName();
    }

    /**
     * labelName : symbolicName
     */
    private static IParser<InputTokens, AstNode, String> labelName() {
        return symbolicName();
    }

    /**
     * propertyKeyName : symbolicName
     */
    private static IParser<InputTokens, AstNode, String> propertyKeyName() {
        return symbolicName();
    }

    /**
     * symbolicName : IDENTIFIER
     *
     * <p>Base comun de variables, etiquetas y nombres de propiedad. Tenerlas separadas
     * arriba, aunque las tres deleguen aqui, mantiene la correspondencia con la gramatica
     * del curso y deja el lugar preparado por si alguna necesita reglas propias.</p>
     */
    private static IParser<InputTokens, AstNode, String> symbolicName() {
        return parsify(TokenType.IDENTIFIER);
    }

    /**
     * relationshipPattern : "-" relationshipDetail? "->" | "&lt;-" relationshipDetail? "-" | ...
     *
     * <p>Fuera del alcance de este sprint: el archivo de casos cubre solo patrones de nodo.
     * Al fallar siempre, {@link #path()} nunca tiene exito y {@link #patternElement()}
     * resuelve por la alternativa de nodo suelto.</p>
     */
    private static IParser<InputTokens, AstNode, String> relationshipPattern() {
        return source -> new Fail<>("not implemented yet");
    }

    /**
     * updatingClause : createClause | deleteClause
     *
     * <p>La gramatica del curso incluye aqui {@code SET} y {@code REMOVE}, fuera del
     * alcance de este sprint.</p>
     */
    private static IParser<InputTokens, AstNode, String> updatingClause() {
        return OrFlat(concatStringList,
                createClause(),
                deleteClause()
        );
    }

    /**
     * returnClause : RETURN projection ("," projection)*
     */
    private static IParser<InputTokens, AstNode, String> returnClause() {
        return TFlat(
                Sequence(parsify(RETURN), projection(),
                        asNode(Star(JustGetRight(parsify(COMMA), projection())))),
                parts -> toReturn(parts));
    }

    /**
     * projection : expression AS variable | expression
     *
     * <p>La alternativa con alias va primero. Si fuera al revés, la expresion sola
     * siempre tendria exito y el {@code AS} nunca llegaria a intentarse, dejando el
     * alias sin consumir.</p>
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
     * deleteClause : DETACH? DELETE expression ("," expression)*
     *
     * <p>El {@code DETACH} opcional distingue el borrado en cascada del borrado simple.</p>
     */
    private static IParser<InputTokens, AstNode, String> deleteClause() {
        return TFlat(
                Sequence(
                        asNode(Optional(parsify(DETACH))), parsify(DELETE), expression(), asNode(Star(JustGetRight(parsify(COMMA), expression())))
                ), parts -> toDelete(parts)
        );
    }

    /**
     * expression : simple (comparisonOperator simple)?
     *
     * <p>Una expresion es un valor simple y, opcionalmente, su comparacion contra otro.
     * Cuando la parte opcional no aparece, {@code toExpression} devuelve el valor simple
     * tal cual, sin envolverlo en un nodo de comparacion.</p>
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
     * simple : variable "." name | literal | variable
     *
     * <p>El orden importa: el acceso a propiedad va primero porque empieza con una
     * variable. Si {@code variable} estuviera antes, {@code p.name} reconoceria solo
     * la {@code p} y el punto quedaria sin consumir.</p>
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
     * name : symbolicName
     */
    private static IParser<InputTokens, AstNode, String> name() {
        return symbolicName();
    }

    /**
     * literal : NUMBER | STRING_LITERAL
     *
     * <p>La gramatica del curso incluye ademas {@code TRUE}, {@code FALSE} y {@code NULL},
     * que ningun caso de prueba usa.</p>
     */
    private static IParser<InputTokens, AstNode, String> literal() {
        return OrFlat(concatStringList,
                parsify(NUMBER),
                parsify(STRING_LITERAL)
        );
    }

    /**
     * comparisonOperator : "=" | "&lt;&gt;" | "&lt;" | "&gt;" | "&lt;=" | "&gt;="
     *
     * <p>El archivo de casos acota el alcance a {@code &lt;}, {@code &gt;} y {@code &lt;&gt;}.
     * Los otros tres se aceptan igual, por estar en la gramatica del curso.</p>
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

    //////////////////////////////////////////////////////////////////////////
    // REGLAS FUERA DEL ALCANCE DE ESTE SPRINT
    //
    // Estan declaradas para que la clase refleje la gramatica completa del curso
    // y sirva de mapa de lo que faltaria. Todas fallan con un mensaje que las nombra.
    //////////////////////////////////////////////////////////////////////////

    /** setClause : SET setItem ("," setItem)* */
    private static IParser<InputTokens, AstNode, String> setClause() {
        return source -> new Fail<>("'Set Clause' is not implemented yet");
    }

    /** setItem : propertyExpression "=" expression | variable "=" expression | variable nodeLabels */
    private static IParser<InputTokens, AstNode, String> setItem() {
        return source -> new Fail<>("'Set Item' is not implemented yet");
    }

    /** removeClause : REMOVE removeItem ("," removeItem)* */
    private static IParser<InputTokens, AstNode, String> removeClause() {
        return source -> new Fail<>("'Remove Clause' is not implemented yet");
    }

    /** removeItem : variable nodeLabels | propertyExpression */
    private static IParser<InputTokens, AstNode, String> removeItem() {
        return source -> new Fail<>("'Remove Item' is not implemented yet");
    }

    /** projectionBody : DISTINCT? projectionItems projectionModifiers */
    private static IParser<InputTokens, AstNode, String> projectionBody() {
        return source -> new Fail<>("'Projection Body' is not implemented yet");
    }

    /** projectionItems : "*" ("," projectionItem)* | projectionItem ("," projectionItem)* */
    private static IParser<InputTokens, AstNode, String> projectionItems() {
        return source -> new Fail<>("'Projection Items' is not implemented yet");
    }

    /** projectionItem : expression AS variable | expression */
    private static IParser<InputTokens, AstNode, String> projectionItem() {
        return source -> new Fail<>("'Projection Item' is not implemented yet");
    }

    /** projectionModifiers : order? skip? limit? */
    private static IParser<InputTokens, AstNode, String> projectionModifiers() {
        return source -> new Fail<>("'Projection Modifiers' is not implemented yet");
    }

    /** order : ORDER BY sortItem ("," sortItem)* */
    private static IParser<InputTokens, AstNode, String> order() {
        return source -> new Fail<>("'Order' is not implemented yet");
    }

    /** sortItem : expression (ASCENDING | DESCENDING)? */
    private static IParser<InputTokens, AstNode, String> sortItem() {
        return source -> new Fail<>("'Sort Item' is not implemented yet");
    }

    /** skip : SKIP expression */
    private static IParser<InputTokens, AstNode, String> skip() {
        return source -> new Fail<>("'Skip' is not implemented yet");
    }

    /** limit : LIMIT expression */
    private static IParser<InputTokens, AstNode, String> limit() {
        return source -> new Fail<>("'Limit' is not implemented yet");
    }

    /** relationshipDetail : "[" variable? relTypes? relRange? properties? "]" */
    private static IParser<InputTokens, AstNode, String> relationshipDetail() {
        return source -> new Fail<>("'Relationship Detail' is not implemented yet");
    }

    /** relTypes : ":" relTypeName ("|" relTypeName)* */
    private static IParser<InputTokens, AstNode, String> relTypes() {
        return source -> new Fail<>("'Rel Types' is not implemented yet");
    }

    /** relTypeName : symbolicName */
    private static IParser<InputTokens, AstNode, String> relTypeName() {
        return source -> new Fail<>("'Rel Type Name' is not implemented yet");
    }

    /** relRange : "*" (integerLiteral? ".." integerLiteral? | integerLiteral)? */
    private static IParser<InputTokens, AstNode, String> relRange() {
        return source -> new Fail<>("'Rel Range' is not implemented yet");
    }

    /** relProperties : mapLiteral */
    private static IParser<InputTokens, AstNode, String> relProperties() {
        return source -> new Fail<>("'Rel Properties' is not implemented yet");
    }

    /** orExpression : andExpression (OR andExpression)* */
    private static IParser<InputTokens, AstNode, String> orExpression() {
        return source -> new Fail<>("'Or Expression' is not implemented yet");
    }

    /** andExpression : notExpression (AND notExpression)* */
    private static IParser<InputTokens, AstNode, String> andExpression() {
        return source -> new Fail<>("'And Expression' is not implemented yet");
    }

    /** notExpression : NOT* comparisonExpression */
    private static IParser<InputTokens, AstNode, String> notExpression() {
        return source -> new Fail<>("'Not Expression' is not implemented yet");
    }

    /** comparisonExpression : addOrSubtractExpression (comparisonOperator addOrSubtractExpression)* */
    private static IParser<InputTokens, AstNode, String> comparisonExpression() {
        return source -> new Fail<>("'Comparison Expression' is not implemented yet");
    }

    /** addOrSubtractExpression : multiplyDivideModuloExpression (("+" | "-") multiplyDivideModuloExpression)* */
    private static IParser<InputTokens, AstNode, String> addOrSubtractExpression() {
        return source -> new Fail<>("'Add Or Subtract Expression' is not implemented yet");
    }

    /** multiplyDivideModuloExpression : unaryAddOrSubtractExpression (("*" | "/" | "%") unaryAddOrSubtractExpression)* */
    private static IParser<InputTokens, AstNode, String> multiplyDivideModuloExpression() {
        return source -> new Fail<>("'Multiply Divide Modulo Expression' is not implemented yet");
    }

    /** unaryAddOrSubtractExpression : ("+" | "-")* nullTestExpression */
    private static IParser<InputTokens, AstNode, String> unaryAddOrSubtractExpression() {
        return source -> new Fail<>("'Unary Add Or Subtract Expression' is not implemented yet");
    }

    /** nullTestExpression : propertyOrLabelsExpression (IS NOT? NULL)? */
    private static IParser<InputTokens, AstNode, String> nullTestExpression() {
        return source -> new Fail<>("'Null Test Expression' is not implemented yet");
    }

    /** propertyOrLabelsExpression : atom propertyLookup* nodeLabels? */
    private static IParser<InputTokens, AstNode, String> propertyOrLabelsExpression() {
        return source -> new Fail<>("'Property Or Labels Expression' is not implemented yet");
    }

    /** propertyLookup : "." propertyKeyName */
    private static IParser<InputTokens, AstNode, String> propertyLookup() {
        return source -> new Fail<>("'Property Lookup' is not implemented yet");
    }

    /** propertyExpression : atom propertyLookup+ */
    private static IParser<InputTokens, AstNode, String> propertyExpression() {
        return source -> new Fail<>("'Property Expression' is not implemented yet");
    }

    /** atom : literal | "(" expression ")" | functionInvocation | variable */
    private static IParser<InputTokens, AstNode, String> atom() {
        return source -> new Fail<>("'Atom' is not implemented yet");
    }

    /** functionInvocation : functionName "(" (expression ("," expression)*)? ")" */
    private static IParser<InputTokens, AstNode, String> functionInvocation() {
        return source -> new Fail<>("'Function Invocation' is not implemented yet");
    }

    /** functionName : symbolicName */
    private static IParser<InputTokens, AstNode, String> functionName() {
        return source -> new Fail<>("'Function Name' is not implemented yet");
    }

    /** numberLiteral : doubleLiteral | integerLiteral */
    private static IParser<InputTokens, AstNode, String> numberLiteral() {
        return source -> new Fail<>("'Number Literal' is not implemented yet");
    }

    /** doubleLiteral : DECIMAL_DOUBLE */
    private static IParser<InputTokens, AstNode, String> doubleLiteral() {
        return source -> new Fail<>("'Double Literal' is not implemented yet");
    }

    /** booleanLiteral : TRUE | FALSE */
    private static IParser<InputTokens, AstNode, String> booleanLiteral() {
        return source -> new Fail<>("'Boolean Literal' is not implemented yet");
    }

    /** integerLiteral : DECIMAL_INTEGER */
    private static IParser<InputTokens, AstNode, String> integerLiteral() {
        return source -> new Fail<>("'Integer Literal' is not implemented yet");
    }
}