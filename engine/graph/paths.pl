:- encoding(utf8).

/* Caminos de largo variable

Resuelve las relaciones de largo variable, como (a)-[:CONOCE*1..3]->(b).

Alejandro Miranda
Sebastián González (documentación)
P2
*/

:- use_module(library(lists)).
:- ensure_loaded('graph.pl').
:- ensure_loaded('nodes.pl').
:- ensure_loaded('edges.pl').
:- ensure_loaded('../utils/edge_queries.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Match %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_path(?From, ?To, -Path, +Options) is nondet.
%
%   Busca un camino de arcos entre dos nodos de un grafo, según las opciones.
%
%   Opciones reconocidas (cada una con su valor por defecto):
%     - edge_direction(D): right, left o none. Por defecto right.
%     - bounds(Min, Max): largo mínimo y máximo; Max puede ser infinite.
%       Por defecto bounds(1, infinite).
%     - edge_types(Tipos): solo relaciones de esos tipos. Por defecto [], cualquiera.
%     - edge_props(Props): solo relaciones con esos pares Clave-Valor. Por defecto [].
%
%   Ejemplo: -[:CONOCE*1..2]-> se traduce a
%   cy_path(__A, __B, _, [edge_direction(right), bounds(1, 2), edge_types(['CONOCE'])]).
%
%   From     Nodo de salida; si llega libre, cy_node/1 recorre todos.
%   To       Nodo de llegada.
%   Path     Lista de ids de las relaciones recorridas, en orden.
%   Options  Lista de opciones.
cy_path(From, To, Path, Options) :-
    find_options(Options, Direction, Types, Props, Min, Max),
    cy_node(From),
    path_dfs(rules(Direction, Types, Props, Min, Max), From, To, [], 0, Path)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Search %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   path_dfs(+Rules, +Node, ?To, +Edges, +Length, -Path) is nondet.
%
%   Búsqueda en profundidad; acumula los arcos usados para no repetirlos.
%
%   Primera cláusula: se llegó a To con un largo suficiente; el camino es
%   Edges. Segunda cláusula: si aún no se alcanzó el máximo, da un paso por
%   una relación no usada y sigue. Una relación no se repite en el mismo
%   camino; un nodo sí puede repetirse, como en Cypher.
%
%   Rules   rules(Direction, Types, Props, Min, Max).
%   Node    Nodo en el que va la búsqueda.
%   To      Nodo de llegada.
%   Edges   Relaciones recorridas hasta ahora (acumulador).
%   Length  Largo actual del camino.
%   Path    Camino completo.
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

%   path_step(+Direction, +Types, +Props, +Node, -Edge, -Next) is nondet.
%
%   Un paso: un arco desde Node que cumple dirección, tipos y propiedades.
%
%   Direction  right, left o none.
%   Types      Tipos permitidos; [] acepta cualquiera.
%   Props      Propiedades Clave-Valor exigidas.
%   Node       Nodo actual.
%   Edge       Relación usada en el paso.
%   Next       Nodo al que se llega.
path_step(Direction, Types, Props, Node, Edge, Next) :-
    path_edge(Direction, Node, Edge, Next),
    edge_matches(Edge, Types, Props)
.

%   path_edge(+Direction, +Node, -Edge, -Next) is nondet.
%
%   Arcos hacia la derecha (salen de Node), hacia la izquierda (entran a Node) o ambos (un arco de un nodo a sí mismo cuenta una vez).
%
%   Direction  right, left o none.
%   Node       Nodo actual.
%   Edge       Relación encontrada.
%   Next       Nodo en el otro extremo.
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

%   find_options(+Options, -Direction, -Types, -Props, -Min, -Max) is det.
%
%   Lee de las opciones la dirección, los tipos, las propiedades y los límites del camino.
%
%   Options    Lista de opciones de cy_path/4.
%   Direction  Dirección leída o right.
%   Types      Tipos leídos o [].
%   Props      Propiedades leídas o [].
%   Min        Largo mínimo.
%   Max        Largo máximo, ya numérico (ver path_max/2).
find_options(Options, Direction, Types, Props, Min, Max) :-
    find_option(Options, edge_direction(Direction), edge_direction(right)),
    find_option(Options, edge_types(Types), edge_types([])),
    find_option(Options, edge_props(Props), edge_props([])),
    find_option(Options, bounds(Min, Provisional_Max), bounds(1, infinite)),
    path_max(Provisional_Max, Max)
.

%   find_option(+Options, ?Option, +Default) is det.
%
%   Toma la opción de la lista o, si no está, su valor por defecto.
%
%   Options  Lista de opciones.
%   Option   Plantilla de la opción, por ejemplo edge_types(T).
%   Default  Opción completa que se usa si no viene en la lista.
find_option(Options, Option, Default) :- (
    memberchk(Option, Options)
    ->  true
    ;   Option = Default
).

%   path_max(+Provisional_Max, -Max) is det.
%
%   Convierte el máximo infinite en la cantidad de arcos del grafo; un máximo numérico queda igual.
%
%   Como un camino no repite relaciones, nunca puede ser más largo que el
%   total de relaciones del grafo; ese total sirve de máximo real.
%
%   Provisional_Max  Un número o infinite.
%   Max              Máximo numérico.
path_max(infinite, Max) :- !,
    edge_count(Max)
.

path_max(Max, Max).
