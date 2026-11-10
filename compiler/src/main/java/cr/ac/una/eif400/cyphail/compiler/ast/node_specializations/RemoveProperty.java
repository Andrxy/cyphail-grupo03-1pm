package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Eliminación de una propiedad dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param target el acceso a la propiedad que se desea eliminar
 */
public record RemoveProperty(PropertyAccess target) implements AstNode{
}
