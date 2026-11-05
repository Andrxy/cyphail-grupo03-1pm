:- encoding(utf8).
:- use_module(library(lists)).
:- ensure_loaded('graph.pl').
:- ensure_loaded('../utils/mutations.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Match %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Busca una relación y sus extremos en el grafo actual.
cy_edge(Id_edge, From, To) :-
    get_current_graph(G),
    edge(G, Id_edge, From, To)
.

% Busca el tipo de una relación en el grafo actual.
cy_edge_type(Id_edge, Type) :-
    get_current_graph(G),
    edge_type(G, Id_edge, Type)
.

% Busca una propiedad de una relación en el grafo actual; si no existe, leerla da null.
cy_edge_prop(Id_edge, Key, Value) :- (
    get_current_graph(G),
    edge_prop(G, Id_edge, Key, Value_exist)
    ->  Value = Value_exist
    ;   var(Value), Value = null
).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Create %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Crea una relación de un tipo entre dos nodos del grafo actual y devuelve su id.
cy_create_edge(From, To, Type, Props, Id_edge) :-
    new_id(e, Id_edge),
    assert_edge(Id_edge, From, To),
    assert_edge_type(Id_edge, Type),
    forall(member(Key-Value, Props), assert_edge_prop(Id_edge, Key, Value))
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Set %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Remove %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Elimina una propiedad de una relación; si no la tiene no hace nada.
cy_remove_edge_prop(Id_edge, Key) :-
    get_current_graph(G),
    retractall(edge_prop(G, Id_edge, Key, _))
.
