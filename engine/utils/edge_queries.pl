:- encoding(utf8).
:- use_module(library(lists)).
:- ensure_loaded('../graph/edges.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Count %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Cuenta todos los arcos del grafo actual.
edge_count(Count) :-
    aggregate_all(count, cy_edge(_, _, _), Count)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Filters %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Comprueba que la relación tenga alguno de los tipos; sin tipos acepta cualquiera.
edge_has_any_type(_, []) :- ! .

edge_has_any_type(Edge, Types) :-
    once((cy_edge_type(Edge, Type), memberchk(Type, Types)))
.

% Comprueba que la relación tenga todas las propiedades Clave-Valor.
edge_has_props(Edge, Props) :-
    forall(member(Key-Value, Props), cy_edge_prop(Edge, Key, Value))
.

% Comprueba que la relación cumpla los tipos y las propiedades.
edge_matches(Edge, Types, Props) :-
    edge_has_any_type(Edge, Types),
    edge_has_props(Edge, Props)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Node edges %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Una relación que sale de un nodo o entra a él.
node_edge(Id_node, Id_edge) :- (
    cy_edge(Id_edge, Id_node, _)
    ;
    cy_edge(Id_edge, _, Id_node)
).

% Comprueba si un nodo todavía tiene alguna relación.
node_has_edges(Id_node) :-
    once(node_edge(Id_node, _))
.
