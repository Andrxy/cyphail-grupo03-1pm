package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;

/**
 * Nodo hoja (terminal) dentro del AST, envuelve directamente
 * un token
 *
 * @author Andrey Solano
 * @version P1
 * @param token el token léxico asociado a esta hoja
 */
public record Leaf(TokenString token) implements AstNode {
}
