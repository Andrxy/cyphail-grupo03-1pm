package cr.ac.una.eif400.cyphail.compiler.lexer.token;

/**
 * Define los diferentes tipos de tokens reconocidos <br>
 * <b>Prompt: </b> En base a la gramática, extraiga los tokens necesarios para armar un enumerador de TokenType
 *
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @version P1
 */
public enum TokenType {
    /**
     * Identifiers and literals
     */
    IDENTIFIER,
    NUMBER,
    STRING_LITERAL,

    /**
     * Comparison operators
     */
    EQUALS, NOTEQUALS, LESS_THAN, GREATER_THAN,
    LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL,

    /**
     * Arithmetic operators
     */
    PLUS, MINUS, MULTIPLY,
    DIVIDE, MODULO,

    /**
     * Delimiters and punctuation
     */
    LPAREN, RPAREN,
    LBRACKET, RBRACKET,
    LBRACE, RBRACE,
    DOT, COMMA, COLON,
    PIPE, SEMICOLON,

    /**
     * Relationship stuff
     */
    RIGHT_ARROW, LEFT_ARROW, RANGE_DOTS,

    /**
     * Keywords
     */
    MATCH, WHERE, CREATE, SET, DETACH, DELETE,
    REMOVE, RETURN, DISTINCT, AS, ORDER, BY,
    ASCENDING, DESCENDING, SKIP, LIMIT, OR,
    AND, NOT, IS, TRUE, FALSE, NULL,

    /**
     * Comentarios
     */
    LINE_COMMENT, BLOCK_COMMENT,

    EOF
}
