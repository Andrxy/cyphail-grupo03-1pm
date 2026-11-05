:- encoding(utf8).
:- use_module(library(lists)).
:- ensure_loaded('graph.pl').
:- ensure_loaded('edges.pl').
:- ensure_loaded('../utils/mutations.pl').
:- ensure_loaded('../utils/edge_queries.pl').

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

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Create %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Crea un nodo con sus etiquetas y propiedades Clave-Valor en el grafo actual y devuelve su id.
cy_create_node(Labels, Props, Id_node) :-
    new_id(n, Id_node),
    assert_node(Id_node),
    forall(member(Label, Labels), assert_node_label(Id_node, Label)),
    forall(member(Key-Value, Props), assert_node_prop(Id_node, Key, Value))
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Set %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Agrega una etiqueta a un nodo; si ya la tiene no hace nada.
cy_set_node_label(Id_node, Label) :- (
    cy_node_label(Id_node, Label)
    ->  true
    ;   assert_node_label(Id_node, Label)
).

% Cambia el valor de una propiedad de un nodo; asignar null la elimina.
cy_set_node_prop(Id_node, Key, Value) :-
    cy_remove_node_prop(Id_node, Key),
    assert_node_prop(Id_node, Key, Value)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Delete %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Elimina un nodo sin relaciones; si todavía tiene alguna lanza un error, como Cypher.
cy_delete_node(Id_node) :- (
    node_has_edges(Id_node)
    ->  throw(cy_error('Cannot delete a node that still has relationships, use DETACH DELETE'))
    ;   remove_node(Id_node)
).

% Elimina un nodo junto con todas las relaciones que entran o salen de él.
cy_detach_delete_node(Id_node) :-
    forall(node_edge(Id_node, Id_edge), cy_delete_edge(Id_edge)),
    remove_node(Id_node)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Remove %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Quita una etiqueta de un nodo; si no la tiene no hace nada.
cy_remove_node_label(Id_node, Label) :-
    get_current_graph(G),
    retractall(node_label(G, Id_node, Label))
.

% Elimina una propiedad de un nodo; si no la tiene no hace nada.
cy_remove_node_prop(Id_node, Key) :-
    get_current_graph(G),
    retractall(node_prop(G, Id_node, Key, _))
.
