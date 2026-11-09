package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;

import java.util.List;

/**
 *  Una sentencia con su texto original y los tokens que se van a compilar.
 *
 * @author Wilson Umaña
 * @version P2
 */
public record Statement(String source, List<TokenString> tokens) {
}
