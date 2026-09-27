package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.parser.IParser;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;

/**
 * Representa un Lexer que extiende la funcionalidad de un {@link IParser}, este
 * transformar una entrada en tokens de tipo {@link TokenString}
 *
 * @author Andrey Solano
 * @version P1
 */
public interface Lexer extends IParser<InputString, TokenString, String> {}
