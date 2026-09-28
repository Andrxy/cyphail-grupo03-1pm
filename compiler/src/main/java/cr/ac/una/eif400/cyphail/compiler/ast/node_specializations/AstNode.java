package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

/**
 * Interfaz sellada raíz que representa cualquier nodo dentro del AST,
 * restringiendo qué estructuras pueden extenderlo directamente <br><br>
 *
 * Prompt: A nivel de diseño, cuáles son los nodos más representativos para la consturcción del AST
 *
 * @author Andrey Solano
 * @author Wilson Umaña
 * @version P1
 */
public sealed interface AstNode permits Leaf, Node, Query, Match, Where,
                                        Create, Delete, Return, Path, Pattern,
                                        Property, PropertyAccess, Comparison,
                                        SimpleProjection, AliasedProjection { }
