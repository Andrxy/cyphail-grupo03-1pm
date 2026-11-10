package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Eliminación de etiquetas de un nodo dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param variable la hoja que contiene la variable del nodo
 * @param labels la lista de etiquetas que se desean eliminar
 */
public record RemoveLabels (Leaf variable, List<Leaf> labels) implements AstNode{
}
