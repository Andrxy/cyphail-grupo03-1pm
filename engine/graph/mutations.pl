:- encoding(utf8).
:- use_module(library(lists)).
:- ensure_loaded('graph.pl').
:- ensure_loaded('nodes.pl').
:- ensure_loaded('edges.pl').
:- ensure_loaded('../utils/asserts.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Create %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Crea un nodo con sus etiquetas y propiedades Clave-Valor en el grafo actual y devuelve su id.
cy_create_node(Labels, Props, Id_node) :-
    new_id(n, Id_node),
    assert_node(Id_node),
    forall(member(Label, Labels), assert_node_label(Id_node, Label)),
    forall(member(Key-Value, Props), assert_node_prop(Id_node, Key, Value))
.

% Crea una relación de un tipo entre dos nodos del grafo actual y devuelve su id.
cy_create_edge(From, To, Type, Props, Id_edge) :-
    new_id(e, Id_edge),
    assert_edge(Id_edge, From, To),
    assert_edge_type(Id_edge, Type),
    forall(member(Key-Value, Props), assert_edge_prop(Id_edge, Key, Value))
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

% Cambia el valor de una propiedad de una relación; asignar null la elimina.
cy_set_edge_prop(Id_edge, Key, Value) :-
    cy_remove_edge_prop(Id_edge, Key),
    assert_edge_prop(Id_edge, Key, Value)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Delete %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Elimina una relación con su tipo y sus propiedades; los nodos de sus extremos no cambian.
cy_delete_edge(Id_edge) :-
    get_current_graph(G),
    retractall(edge(G, Id_edge, _, _)),
    retractall(edge_type(G, Id_edge, _)),
    retractall(edge_prop(G, Id_edge, _, _))
.

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

% Elimina una propiedad de una relación; si no la tiene no hace nada.
cy_remove_edge_prop(Id_edge, Key) :-
    get_current_graph(G),
    retractall(edge_prop(G, Id_edge, Key, _))
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Utils %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Quita un nodo con sus etiquetas y propiedades.
remove_node(Id_node) :-
    get_current_graph(G),
    retractall(node(G, Id_node)),
    retractall(node_label(G, Id_node, _)),
    retractall(node_prop(G, Id_node, _, _))
.

% Genera el primer id con el prefijo y un número que no use ningún nodo ni relación del grafo actual.
new_id(Prefix, Id_element) :-
    get_current_graph(G),
    between(1, infinite, Number),
    atom_concat(Prefix, Number, Id_element),
    \+ node(G, Id_element),
    \+ edge(G, Id_element, _, _),
    !
.
