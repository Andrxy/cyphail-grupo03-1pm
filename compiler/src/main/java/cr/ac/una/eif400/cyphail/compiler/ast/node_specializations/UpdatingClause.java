package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Interfaz para las cláusulas de actualización dentro del AST.
 *
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @version P2
 */
public sealed interface UpdatingClause extends AstNode
        permits Create, Delete, SetClause, RemoveClause {
}