:- encoding(utf8).
:- ensure_loaded('graph.pl').

% Busca un nodo en el grafo actual.
cy_node(Id) :-
    get_current_graph(G),
    node(G, Id)
.

% Busca una etiqueta de un nodo en el grafo actual.
cy_node_label(Id, Label) :-
    get_current_graph(G),
    node_label(G, Id, Label)
.

% Busca una propiedad de un nodo en el grafo actual.
cy_node_prop(Id, Key, Value) :-
    get_current_graph(G),
    node_prop(G, Id, Key, Value)
.
