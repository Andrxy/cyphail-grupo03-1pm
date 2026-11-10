package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Asignación de etiquetas a un nodo dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param variable la hoja que contiene la variable del nodo
 * @param labels la lista de etiquetas que se desean asignar
 */
public record SetLabels(Leaf variable, List<Leaf> labels) implements AstNode {
}
