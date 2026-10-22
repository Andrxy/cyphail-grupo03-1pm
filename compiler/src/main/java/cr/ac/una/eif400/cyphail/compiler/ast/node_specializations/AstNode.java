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
public sealed interface AstNode permits AliasedProjection, And, Arithmetic, Comparison, FunctionCall, LabelTest, Leaf, Limit, Match, Node, Not, NullTest, Or, Order, Path, Pattern, Property, PropertyAccess, Query, RelRange, Relationship, RemoveLabels, RemoveProperty, Return, SetAllProperties, SetLabels, SetProperty, SimpleProjection, Skip, SortItem, Unary, UpdatingClause, Where { }
