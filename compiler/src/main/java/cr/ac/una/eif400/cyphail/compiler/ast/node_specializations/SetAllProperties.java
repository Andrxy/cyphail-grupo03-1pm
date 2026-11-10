package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Asignación de todas las propiedades de un elemento dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param variable la hoja que contiene la variable del elemento
 * @param value la expresión que contiene las nuevas propiedades
 */
public record SetAllProperties(Leaf variable, AstNode value) implements AstNode{
}
