:- encoding(utf8).
:- ensure_loaded('../graph/graph.pl').

% Agrega un nodo al grafo actual.
assert_node(Id_node) :-
    get_current_graph(G),
    assert(node(G, Id_node))
.

% Agrega una etiqueta a un nodo del grafo actual.
assert_node_label(Id_node, Label) :-
    get_current_graph(G),
    assert(node_label(G, Id_node, Label))
.

% Agrega una propiedad a un nodo del grafo actual, salvo que el valor sea null,
% que en Cypher equivale a no tener la propiedad.
assert_node_prop(_, _, null) :- ! .

assert_node_prop(Id_node, Key, Value) :-
    get_current_graph(G),
    assert(node_prop(G, Id_node, Key, Value))
.

% Agrega una relación entre dos nodos del grafo actual.
assert_edge(Id_edge, From, To) :-
    get_current_graph(G),
    assert(edge(G, Id_edge, From, To))
.

% Asocia una relación del grafo actual con su tipo.
assert_edge_type(Id_edge, Type) :-
    get_current_graph(G),
    assert(edge_type(G, Id_edge, Type))
.

% Agrega una propiedad a una relación del grafo actual, salvo que el valor sea null,
% que en Cypher equivale a no tener la propiedad.
assert_edge_prop(_, _, null) :- ! .

assert_edge_prop(Id_edge, Key, Value) :-
    get_current_graph(G),
    assert(edge_prop(G, Id_edge, Key, Value))
.
