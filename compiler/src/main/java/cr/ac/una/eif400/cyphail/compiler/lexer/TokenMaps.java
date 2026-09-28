package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;

import java.util.Map;

import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.*;

/**
 * Contiene los mapas que relacionan las string con sus respectivos
 * tokens ({@link TokenType}) agrupados por familias léxicas.
 * <br>
 * <b>Prompt:</b> De acuerdo a los siguientes Lexers que representan elementos
 * del lenguaje cypher agrupados por "familias", por cada una cree un {@code Map<String, TokenType>}
 *
 * @author Andrey Solano
 * @version P1
 */
public class TokenMaps {

    public static Map<String, TokenType> COMPARISON = Map.of(
            "=", EQUALS,
            "<>", NOTEQUALS,
            "<", LESS_THAN,
            ">", GREATER_THAN,
            "<=", LESS_THAN_OR_EQUAL,
            ">=", GREATER_THAN_OR_EQUAL
    );

    public static Map<String, TokenType> ARITHMETIC = Map.of(
            "+", PLUS,
            "-", MINUS,
            "*", MULTIPLY,
            "/", DIVIDE,
            "%", MODULO
    );

    public static Map<String, TokenType> DELIMITER = Map.ofEntries(
            Map.entry("(", LPAREN),
            Map.entry(")", RPAREN),
            Map.entry("[", LBRACKET),
            Map.entry("]", RBRACKET),
            Map.entry("{", LBRACE),
            Map.entry("}", RBRACE),
            Map.entry(".", DOT),
            Map.entry(",", COMMA),
            Map.entry(":", COLON),
            Map.entry("|", PIPE),
            Map.entry(";", SEMICOLON)
    );

    public static Map<String, TokenType> RELATIONSHIP = Map.of(
            "->", RIGHT_ARROW,
            "<-", LEFT_ARROW,
            "..", RANGE_DOTS
    );


    public static Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("MATCH", MATCH),
            Map.entry("WHERE", WHERE),
            Map.entry("CREATE", CREATE),
            Map.entry("SET", SET),
            Map.entry("DETACH", DETACH),
            Map.entry("DELETE", DELETE),
            Map.entry("REMOVE", REMOVE),
            Map.entry("RETURN", RETURN),
            Map.entry("DISTINCT", DISTINCT),
            Map.entry("AS", AS),
            Map.entry("ORDER", ORDER),
            Map.entry("BY", BY),
            Map.entry("ASCENDING", ASCENDING),
            Map.entry("DESCENDING", DESCENDING),
            Map.entry("SKIP", SKIP),
            Map.entry("LIMIT", LIMIT),
            Map.entry("OR", OR),
            Map.entry("AND", AND),
            Map.entry("NOT", NOT),
            Map.entry("IS", IS),
            Map.entry("TRUE", TRUE),
            Map.entry("FALSE", FALSE),
            Map.entry("NULL", NULL)
    );
}
