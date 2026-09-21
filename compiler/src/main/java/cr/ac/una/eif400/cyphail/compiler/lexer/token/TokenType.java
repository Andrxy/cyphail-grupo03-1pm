package cr.ac.una.eif400.cyphail.compiler.lexer.token;

public enum TokenType {
    ID,
    INTEGER_LITERAL,
    STRING_LITERAL,
    KEYWORD,
    OPERATOR,

    LPAR,
    RPAR,
    LBRACKET,
    RBRACKET,
    LBRACE,
    RBRACE,
    COMMA,
    COLON,
    DOT,
    PIPE,

    PLUS,
    MINUS,
    STAR,
    SLASH,
    PERCENT,

    EQUALS,
    NOT_EQUALS,
    LESS_THAN,
    GREATER_THAN,
    LESS_EQUALS,
    GREATER_EQUALS,

    ARROW_RIGHT,
    ARROW_LEFT,

    RANGE,

    Num, EOF
}
