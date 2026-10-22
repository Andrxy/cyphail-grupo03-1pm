package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Acceso a la propiedad de una variable
 * (ej, {@code variable.clave}) dentro del AST
 *
 * @author Andrey Solano
 * @version P1
 * @param target la expresión de la cual se accede la propiedad
 * @param key la hoja que representa la clave de la propiedad consultada
 */
public record PropertyAccess(AstNode target, Leaf key) implements AstNode {
}
