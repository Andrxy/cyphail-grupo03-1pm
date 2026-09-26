package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.ast.ASTNode;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;

public interface Parser extends IParser<InputTokens, ASTNode, String> {}
