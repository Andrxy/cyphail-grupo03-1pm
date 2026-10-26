:- ensure_loaded('current_graph.pl').

% Busca un nodo en el grafo actual.
cy_node(Id) :-
    get_current_graph(G),
    node(G, Id).

% Busca una etiqueta de un nodo en el grafo actual.
cy_node_lab(Id, Label) :-
    get_current_graph(G),
    node_lab(G, Id, Label).

% Busca una propiedad de un nodo en el grafo actual.
cy_node_prop(Id, Key, Value) :-
    get_current_graph(G),
    node_prop(G, Id, Key, Value).
