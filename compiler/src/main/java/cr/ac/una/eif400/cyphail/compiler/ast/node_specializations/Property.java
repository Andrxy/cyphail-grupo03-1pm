package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Representa una propiedad (key:value) dentro del AST
 *
 * @author Andrey Solano
 * @version P1
 * @param key la hoja que representa la clave o nombre de la propiedad
 * @param value el nodo o expresión que representa el valor asignado
 */
public record Property(Leaf key, AstNode value) implements AstNode {
}
