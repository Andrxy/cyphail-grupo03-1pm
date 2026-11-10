:- encoding(utf8).

/** <module> Grafo actual

Guarda cuál grafo está seleccionado. Todos los predicados cy_* del engine
consultan el grafo actual en lugar de recibirlo como argumento; por eso CodeGen
no necesita conocer el nombre del grafo: el broker antepone
set_current_graph('nombre') a cada meta que envía.

current_graph/1 es dinámico porque cambia en ejecución con cada .use.

Notación de modos y determinismo: ver engine.pl.

@author Alejandro Miranda
@author Sebastián González (documentación)
@version P2
*/

:- dynamic(current_graph/1).

:- ensure_loaded('../data/amigos.pl').
:- ensure_loaded('../data/ingenieria.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Current graph %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  set_current_graph(+G) is det.
%
%   Establece el grafo actual, reemplazando el anterior si existe.
%
%   retractall/1 borra el grafo anterior y assert/1 guarda el nuevo, de modo
%   que siempre hay a lo sumo un hecho current_graph/1.
%
%   @arg G  Nombre del grafo, por ejemplo ingenieria.
%   @throws cy_error('Graph does not exist') si G no está declarado con graph/1.
set_current_graph(G) :- (
    graph(G)
    ->  retractall(current_graph(_)),
        assert(current_graph(G))
    ;   throw(cy_error('Graph does not exist'))
).

%!  get_current_graph(-G) is det.
%
%   Devuelve el grafo actual.
%
%   Es el primer paso de todo predicado cy_*. Si se consulta antes de .use,
%   el error llega hasta el REPL como "Error: No graph selected".
%
%   @arg G  Nombre del grafo seleccionado.
%   @throws cy_error('No graph selected') si todavía no se eligió un grafo.
get_current_graph(G) :- (
    current_graph(G)
    ->  true
    ;   throw(cy_error('No graph selected'))
).
