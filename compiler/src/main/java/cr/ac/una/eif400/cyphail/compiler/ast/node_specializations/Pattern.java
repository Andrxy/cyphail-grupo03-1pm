package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Representa un patrón estructural dentro del AST
 *
 * @author Andrey Solano
 * @version P1
 * @param variable la lista de variables opcionales asignadas al patrón
 * @param labels la lista de etiquetas asociadas al elemento
 * @param properties la lista de restricciones de propiedades del patrón
 */
public record Pattern(List<Leaf> variable, List<Leaf> labels, List<Property> properties) implements AstNode {
}
