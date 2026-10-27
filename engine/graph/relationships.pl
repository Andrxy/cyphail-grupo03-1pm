:- ensure_loaded('graph.pl').

% Busca una relación y sus extremos en el grafo actual.
cy_edge(Id, From, To) :-
    get_current_graph(G),
    edge(G, Id, From, To)
.

% Busca el tipo de una relación en el grafo actual.
cy_edge_label(Id, Type) :-
    get_current_graph(G),
    edge_label(G, Id, Type)
.

% Busca una propiedad de una relación en el grafo actual.
cy_edge_prop(Id, Key, Value) :-
    get_current_graph(G),
    edge_prop(G, Id, Key, Value)
.
