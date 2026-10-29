:- encoding(utf8).
:- ensure_loaded('graph.pl').

% Busca una relación y sus extremos en el grafo actual.
cy_edge(Id, From, To) :-
    get_current_graph(G),
    edge(G, Id, From, To)
.

% Busca el tipo de una relación en el grafo actual.
cy_edge_type(Id, Type) :-
    get_current_graph(G),
    edge_type(G, Id, Type)
.

% Busca una propiedad de una relación en el grafo actual.
cy_edge_prop(Id, Key, Value) :-
    get_current_graph(G),
    edge_prop(G, Id, Key, Value)
.

% Cuenta todos los arcos del grafo actual.
edge_count(Count) :-
    aggregate_all(count, cy_edge(_, _, _), Count)
.

% Comprueba que la relación tenga alguno de los tipos; sin tipos acepta cualquiera.
edge_has_any_type(_, []) :-
    !
.

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
