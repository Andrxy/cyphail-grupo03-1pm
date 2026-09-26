package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;

import java.util.Map;

/**
 *
 * Prompt used: De acuerdo a los siguientes Lexers que representan elementos
 * del lenguaje cypher agrupados por "familias", por cada una cree un {@code Map<String, TokenType>}
 *
 * @author Andrey Solano
 * @author Gemini
 */
public class TokenMaps {

    public static Map<String, TokenType> COMPARISON = Map.of(
            "=", TokenType.EQUALS,
            "<>", TokenType.NOTEQUALS,
            "<", TokenType.LESS_THAN,
            ">", TokenType.GREATER_THAN,
            "<=", TokenType.LESS_THAN_OR_EQUAL,
            ">=", TokenType.GREATER_THAN_OR_EQUAL
    );

    public static Map<String, TokenType> ARITHMETIC = Map.of(
            "+", TokenType.PLUS,
            "-", TokenType.MINUS,
            "*", TokenType.MULTIPLY,
            "/", TokenType.DIVIDE,
            "%", TokenType.MODULO
    );

    public static Map<String, TokenType> DELIMITER = Map.ofEntries(
            Map.entry("(", TokenType.LPAREN),
            Map.entry(")", TokenType.RPAREN),
            Map.entry("[", TokenType.LBRACKET),
            Map.entry("]", TokenType.RBRACKET),
            Map.entry("{", TokenType.LBRACE),
            Map.entry("}", TokenType.RBRACE),
            Map.entry(".", TokenType.DOT),
            Map.entry(",", TokenType.COMMA),
            Map.entry(":", TokenType.COLON),
            Map.entry("|", TokenType.PIPE),
            Map.entry(";", TokenType.SEMICOLON)
    );

    public static Map<String, TokenType> RELATIONSHIP = Map.of(
            "->", TokenType.RIGHT_ARROW,
            "<-", TokenType.LEFT_ARROW,
            "..", TokenType.RANGE_DOTS
    );


    public static Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("MATCH", TokenType.MATCH),
            Map.entry("WHERE", TokenType.WHERE),
            Map.entry("CREATE", TokenType.CREATE),
            Map.entry("SET", TokenType.SET),
            Map.entry("DETACH", TokenType.DETACH),
            Map.entry("DELETE", TokenType.DELETE),
            Map.entry("REMOVE", TokenType.REMOVE),
            Map.entry("RETURN", TokenType.RETURN),
            Map.entry("DISTINCT", TokenType.DISTINCT),
            Map.entry("AS", TokenType.AS),
            Map.entry("ORDER", TokenType.ORDER),
            Map.entry("BY", TokenType.BY),
            Map.entry("ASCENDING", TokenType.ASCENDING),
            Map.entry("DESCENDING", TokenType.DESCENDING),
            Map.entry("SKIP", TokenType.SKIP),
            Map.entry("LIMIT", TokenType.LIMIT),
            Map.entry("OR", TokenType.OR),
            Map.entry("AND", TokenType.AND),
            Map.entry("NOT", TokenType.NOT),
            Map.entry("IS", TokenType.IS),
            Map.entry("TRUE", TokenType.TRUE),
            Map.entry("FALSE", TokenType.FALSE),
            Map.entry("NULL", TokenType.NULL)
    );
}
