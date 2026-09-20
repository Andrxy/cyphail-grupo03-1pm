package cr.ac.una.eif400.cyphail.compiler.lexer;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.parser.Parser;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;

public interface Lexer extends Parser<InputString, TokenString, String> {}
