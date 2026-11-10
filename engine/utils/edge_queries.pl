:- encoding(utf8).

/* Consultas auxiliares sobre relaciones

Predicados de apoyo para cy_path/4 (conteo y filtros de relaciones) y para el
borrado de nodos (relaciones de un nodo).

Alejandro Miranda
Sebastián González (documentación)
P2
*/

:- use_module(library(lists)).
:- ensure_loaded('../graph/edges.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Count %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   edge_count(-Count) is det.
%
%   Cuenta todos los arcos del grafo actual.
%
%   Count  Cantidad de relaciones.
edge_count(Count) :-
    aggregate_all(count, cy_edge(_, _, _), Count)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Filters %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   edge_has_any_type(+Edge, +Types) is semidet.
%
%   Comprueba que la relación tenga alguno de los tipos; sin tipos acepta cualquiera.
%
%   Edge   Identificador de la relación.
%   Types  Lista de tipos permitidos.
edge_has_any_type(_, []) :- ! .

edge_has_any_type(Edge, Types) :-
    once((cy_edge_type(Edge, Type), memberchk(Type, Types)))
.

%   edge_has_props(+Edge, +Props) is semidet.
%
%   Comprueba que la relación tenga todas las propiedades Clave-Valor; con la
%   lista vacía siempre tiene éxito.
%
%   Edge   Identificador de la relación.
%   Props  Lista de pares Clave-Valor.
edge_has_props(Edge, Props) :-
    forall(member(Key-Value, Props), cy_edge_prop(Edge, Key, Value))
.

%   edge_matches(+Edge, +Types, +Props) is semidet.
%
%   Comprueba que la relación cumpla los tipos y las propiedades.
%
%   Edge   Identificador de la relación.
%   Types  Tipos permitidos.
%   Props  Propiedades exigidas.
edge_matches(Edge, Types, Props) :-
    edge_has_any_type(Edge, Types),
    edge_has_props(Edge, Props)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Node edges %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   node_edge(+Id_node, ?Id_edge) is nondet.
%
%   Una relación que sale de un nodo o entra a él.
%
%   Id_node  Identificador del nodo.
%   Id_edge  Relación que lo toca.
node_edge(Id_node, Id_edge) :- (
    cy_edge(Id_edge, Id_node, _)
    ;
    cy_edge(Id_edge, _, Id_node)
).

%   node_has_edges(+Id_node) is semidet.
%
%   Comprueba si un nodo todavía tiene alguna relación.
%
%   Lo usa cy_delete_node/1 para decidir si el borrado se permite.
%
%   Id_node  Identificador del nodo.
node_has_edges(Id_node) :-
    once(node_edge(Id_node, _))
.
