:- encoding(utf8).
:- ensure_loaded('graph.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Match %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Busca un nodo en el grafo actual.
cy_node(Id_node) :-
    get_current_graph(G),
    node(G, Id_node)
.

% Busca una etiqueta de un nodo en el grafo actual.
cy_node_label(Id_node, Label) :-
    get_current_graph(G),
    node_label(G, Id_node, Label)
.

% Busca una propiedad de un nodo en el grafo actual; si no existe, leerla da null.
cy_node_prop(Id_node, Key, Value) :- (
    get_current_graph(G),
    node_prop(G, Id_node, Key, Value_exist)
    ->  Value = Value_exist
    ;   var(Value), Value = null
).