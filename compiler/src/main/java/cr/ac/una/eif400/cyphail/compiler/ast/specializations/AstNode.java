package cr.ac.una.eif400.cyphail.compiler.ast.specializations;

public sealed interface AstNode permits Leaf, Node, Query, Match, Where,
        Create, Delete, Return, Path, Pattern,
        Property, PropertyAccess, Comparison,
        SimpleProjection, AliasedProjection {
}
