package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Llamada a una función dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param name la hoja que contiene el nombre de la función
 * @param arguments la lista de expresiones utilizadas como argumentos
 */
public record FunctionCall(Leaf name, List<AstNode> arguments) implements AstNode {
}