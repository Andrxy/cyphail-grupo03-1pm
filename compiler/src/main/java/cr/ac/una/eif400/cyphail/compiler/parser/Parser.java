package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.ast.specializations.AstNode;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;

public interface Parser extends IParser<InputTokens, AstNode, String> {}
