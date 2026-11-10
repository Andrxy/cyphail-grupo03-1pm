:- encoding(utf8).

/* Grafo actual

Guarda cuál grafo está seleccionado.

current_graph/1 es dinámico porque cambia en ejecución con cada .use.

Alejandro Miranda
Sebastián González (documentación)
P2
*/

:- dynamic(current_graph/1).

:- ensure_loaded('../data/amigos.pl').
:- ensure_loaded('../data/ingenieria.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Current graph %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   set_current_graph(+G) is det.
%
%   Establece el grafo actual, reemplazando el anterior si existe.
%
%   G  Nombre del grafo, por ejemplo ingenieria.
set_current_graph(G) :- (
    graph(G)
    ->  retractall(current_graph(_)),
        assert(current_graph(G))
    ;   throw(cy_error('Graph does not exist'))
).

%   get_current_graph(-G) is det.
%
%   Devuelve el grafo actual.
%
%   Es el primer paso de todo predicado cy_*. Si se consulta antes de .use,
%   el error llega hasta el REPL como "Error: No graph selected".
%
%   G  Nombre del grafo seleccionado.
get_current_graph(G) :- (
    current_graph(G)
    ->  true
    ;   throw(cy_error('No graph selected'))
).
