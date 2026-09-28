package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;

import java.util.Map;
import java.util.regex.Pattern;

import static cr.ac.una.eif400.cyphail.compiler.lexer.TokenMaps.*;

/**
 * Clase auxiliar que provee fábricas de lexers basadas en regex
 * y define los lexers individuales para cada categoría o familia
 *
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @version P1
 */
public class Lexers {

    /**
     * Crea un lexer que reconoce un regex específico y lo asocia directamente a un tipo de token
     *
     * @param regex la expresión regular que define el token
     * @param type  el tipo de token que se asignará en caso de coincidencia
     * @return un {@link Lexer} listo para evaluar la entrada
     */
    private static Lexer specificLexer(String regex, TokenType type) {
        return (InputString source) -> {
            var sourceString = source.input();
            var index = source.index();

            var regularExpr = Pattern.compile("\\s*(?<token>" + regex + ")");
            var matcher = regularExpr.matcher(sourceString);
            matcher.region(index, sourceString.length());

            if (!matcher.lookingAt())
                return new Fail<>("Error at index " + index);

            var token = matcher.group("token");

            return new Ok<>(
                    new InputString(sourceString, matcher.end()),
                    new TokenString(type, token)
            );
        };
    }

    /**
     * Crea un lexer que busca coincidir con un regex y valida el token resultante
     * consultando un mapa para determinar su tipo exacto
     *
     * @param regex la expresión regular para capturar el texto candidato
     * @param map   el mapa que relaciona los textos con sus respectivos {@link TokenType}
     * @return un {@link Lexer} capaz de clasificar dinámicamente el token encontrado
     */
    private static Lexer sneakyLexer(String regex, Map<String, TokenType> map) {
        return (InputString source) -> {
            var sourceString = source.input();
            var index = source.index();

            var regularExpr = Pattern.compile("\\s*(?<token>" + regex + ")");
            var matcher = regularExpr.matcher(sourceString);
            matcher.region(index, sourceString.length());

            if (!matcher.lookingAt())
                return new Fail<>("Error at index " + index);

            var token = matcher.group("token");

            TokenType type = map.get(token);
            if (type == null) {
                return new Fail<>("Unexpected token '" + token + "' at index " + index);
            }

            return new Ok<>(
                    new InputString(sourceString, matcher.end()),
                    new TokenString(type, token));
        };
    }

    /** Para palabras reservadas del lenguaje */
    public static Lexer Keywords = sneakyLexer("(?i)[a-zA-Z]\\w*", KEYWORDS);

    /** Para operadores aritméticos */
    public static Lexer Arithmetic = sneakyLexer("[+\\-*/%]", ARITHMETIC);

    /** Para operadores de comparación */
    public static Lexer Comparison = sneakyLexer("<=|>=|<>|[=<>]", COMPARISON);

    /** Para delimitadores y signos de puntuación */
    public static Lexer Delimiter = sneakyLexer("[()\\[\\]{}.,:|;]", DELIMITER);

    /** Para elementos y flechas de relaciones */
    public static Lexer Relationship = sneakyLexer("->|<-|\\.\\.", RELATIONSHIP);

    /** Para literales de cadena de texto (en comillas simples o dobles) */
    public static Lexer StringLiteral = specificLexer("\"[^\"]*\"|'[^']*'", TokenType.STRING_LITERAL);

    /** Para identificadores (nombres de variables, propiedades, etc) */
    public static Lexer Identifier = specificLexer("[a-zA-Z_]\\w*", TokenType.IDENTIFIER);

    /** Para números enteros y decimales */
    public static Lexer Numbers = specificLexer("\\d+(\\.\\d+)?", TokenType.NUMBER);
}

