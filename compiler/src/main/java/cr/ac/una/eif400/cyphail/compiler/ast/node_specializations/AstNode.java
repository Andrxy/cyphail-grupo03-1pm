package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Interfaz sellada raíz que representa cualquier nodo dentro del AST,
 * restringiendo qué estructuras pueden extenderlo directamente
 *
 * @author Andrey Solano
 * @version P1
 */
public sealed interface AstNode permits Leaf, Node, Query, Match, Where,
                                        Create, Delete, Return, Path, Pattern,
                                        Property, PropertyAccess, Comparison,
                                        SimpleProjection, AliasedProjection { }
