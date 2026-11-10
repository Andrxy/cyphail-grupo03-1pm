package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Asignación de una propiedad dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param target el acceso a la propiedad que se desea actualizar
 * @param value la expresión que contiene el nuevo valor de la propiedad
 */
public record SetProperty(PropertyAccess target, AstNode value) implements AstNode {
}
