:- encoding(utf8).

/** <module> Consultas auxiliares sobre relaciones

Predicados de apoyo para cy_path/4 (conteo y filtros de relaciones) y para el
borrado de nodos (relaciones de un nodo).

Notación de modos y determinismo: ver engine.pl.

@author Alejandro Miranda
@author Sebastián González (documentación)
@version P2
*/

:- use_module(library(lists)).
:- ensure_loaded('../graph/edges.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Count %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  edge_count(-Count) is det.
%
%   Cuenta todos los arcos del grafo actual.
%
%   aggregate_all(count, ...) cuenta las soluciones sin construir una lista.
%
%   @arg Count  Cantidad de relaciones.
edge_count(Count) :-
    aggregate_all(count, cy_edge(_, _, _), Count)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Filters %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  edge_has_any_type(+Edge, +Types) is semidet.
%
%   Comprueba que la relación tenga alguno de los tipos; sin tipos acepta cualquiera.
%
%   once/1 evita repetir el éxito si la relación calzara con más de un tipo.
%
%   @arg Edge   Identificador de la relación.
%   @arg Types  Lista de tipos permitidos.
edge_has_any_type(_, []) :- ! .

edge_has_any_type(Edge, Types) :-
    once((cy_edge_type(Edge, Type), memberchk(Type, Types)))
.

%!  edge_has_props(+Edge, +Props) is semidet.
%
%   Comprueba que la relación tenga todas las propiedades Clave-Valor.
%
%   forall/2 exige que cada par se cumpla; con la lista vacía siempre tiene éxito.
%
%   @arg Edge   Identificador de la relación.
%   @arg Props  Lista de pares Clave-Valor.
edge_has_props(Edge, Props) :-
    forall(member(Key-Value, Props), cy_edge_prop(Edge, Key, Value))
.

%!  edge_matches(+Edge, +Types, +Props) is semidet.
%
%   Comprueba que la relación cumpla los tipos y las propiedades.
%
%   @arg Edge   Identificador de la relación.
%   @arg Types  Tipos permitidos.
%   @arg Props  Propiedades exigidas.
edge_matches(Edge, Types, Props) :-
    edge_has_any_type(Edge, Types),
    edge_has_props(Edge, Props)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Node edges %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  node_edge(+Id_node, ?Id_edge) is nondet.
%
%   Una relación que sale de un nodo o entra a él.
%
%   @arg Id_node  Identificador del nodo.
%   @arg Id_edge  Relación que lo toca.
node_edge(Id_node, Id_edge) :- (
    cy_edge(Id_edge, Id_node, _)
    ;
    cy_edge(Id_edge, _, Id_node)
).

%!  node_has_edges(+Id_node) is semidet.
%
%   Comprueba si un nodo todavía tiene alguna relación.
%
%   Lo usa cy_delete_node/1 para decidir si el borrado se permite.
%
%   @arg Id_node  Identificador del nodo.
node_has_edges(Id_node) :-
    once(node_edge(Id_node, _))
.
