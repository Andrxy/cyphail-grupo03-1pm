:- encoding(utf8).
:- dynamic current_graph/1.

:- ensure_loaded('../data/amigos.pl').
:- ensure_loaded('../data/ingenieria.pl').

% Establece el grafo actual, reemplazando el anterior si existe.
set_current_graph(G) :- (
    graph(G)
    ->  retractall(current_graph(_)),
        assert(current_graph(G))
    ;   throw(cy_error('Graph does not exist'))
).

% Devuelve el grafo actual.
get_current_graph(G) :- (
    current_graph(G)
    ->  true
    ;   throw(cy_error('No graph selected'))
).
