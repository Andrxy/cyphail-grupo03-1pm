package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Acceso a la propiedad de una variable
 * (ej, {@code variable.clave}) dentro del AST
 *
 * @author Andrey Solano
 * @version P1
 * @param variable la hoja que representa la variable de la cual se accede
 * @param key la hoja que representa la clave de la propiedad consultada
 */
public record PropertyAccess(Leaf target, Leaf key) implements AstNode {
}
