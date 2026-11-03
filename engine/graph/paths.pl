:- encoding(utf8).
:- use_module(library(lists)).
:- ensure_loaded('graph.pl').
:- ensure_loaded('nodes.pl').
:- ensure_loaded('edges.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Match %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Busca un camino de arcos entre dos nodos de un grafo, según las opciones.
cy_path(From, To, Path, Options) :-
    find_options(Options, Direction, Types, Props, Min, Max),
    cy_node(From),
    path_dfs(rules(Direction, Types, Props, Min, Max), From, To, [], 0, Path)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Search %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Búsqueda en profundidad; acumula los arcos usados para no repetirlos.
path_dfs(rules(_, _, _, Min, _), To, To, Path, Length, Path) :-
    Length >= Min
.

path_dfs(Rules, Node, To, Edges, Length, Path) :-
    Rules = rules(Direction, Types, Props, _, Max),
    Length < Max,
    path_step(Direction, Types, Props, Node, Edge, Next),
    \+ memberchk(Edge, Edges),
    Length1 is Length + 1,
    append(Edges, [Edge], NewEdges),
    path_dfs(Rules, Next, To, NewEdges, Length1, Path)
.

% Un paso: un arco desde Node que cumple dirección, tipos y propiedades.
path_step(Direction, Types, Props, Node, Edge, Next) :-
    path_edge(Direction, Node, Edge, Next),
    edge_matches(Edge, Types, Props)
.

% Arcos hacia la derecha (salen de Node), hacia la izquierda (entran a Node) o ambos (un arco de un nodo a sí mismo cuenta una vez).
path_edge(right, Node, Edge, Next) :-
    cy_edge(Edge, Node, Next)
.

path_edge(left, Node, Edge, Next) :-
    cy_edge(Edge, Next, Node)
.

path_edge(none, Node, Edge, Next) :- (
    cy_edge(Edge, Node, Next)
    ;
    cy_edge(Edge, Next, Node),
    Node \== Next
).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Utils %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Lee de las opciones la dirección, los tipos, las propiedades y los límites del camino.
find_options(Options, Direction, Types, Props, Min, Max) :-
    find_option(Options, edge_direction(Direction), edge_direction(right)),
    find_option(Options, edge_types(Types), edge_types([])),
    find_option(Options, edge_props(Props), edge_props([])),
    find_option(Options, bounds(Min, Provisional_Max), bounds(1, infinite)),
    path_max(Provisional_Max, Max)
.

% Toma la opción de la lista o, si no está, su valor por defecto.
find_option(Options, Option, Default) :- (
    memberchk(Option, Options)
    ->  true
    ;   Option = Default
).

% Convierte el máximo infinite en la cantidad de arcos del grafo; un máximo numérico queda igual.
path_max(infinite, Max) :- !,
    edge_count(Max)
.

path_max(Max, Max).
