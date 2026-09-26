package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.IParser;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;

/**
 * @author Andrey Solano
 */
public interface Lexer extends IParser<InputString, TokenString, String> {}
