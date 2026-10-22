package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

public sealed interface UpdatingClause extends AstNode
        permits Create, Delete, SetClause, RemoveClause {
}