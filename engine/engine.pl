:- encoding(utf8).
:- ensure_loaded('data/amigos.pl').
:- ensure_loaded('data/ingenieria.pl').
:- ensure_loaded('utils/rows.pl').
:- ensure_loaded('utils/comparisons.pl').
:- ensure_loaded('utils/expressions.pl').
:- ensure_loaded('graph/graph.pl').
:- ensure_loaded('graph/nodes.pl').
:- ensure_loaded('graph/edges.pl').
:- ensure_loaded('graph/paths.pl').
:- ensure_loaded('graph/mutations.pl').

% Ejecuta el patrón y convierte todas sus soluciones en texto.
cy_exec_query(Goal, Headers, Columns, Rows) :-
    findall(Columns, Goal, Solutions),
    format_rows(Headers, Solutions, Rows)
.

% Devuelve los grafos disponibles y sus descripciones en formato Rows.
cy_graphs(Rows) :-
    findall(
        [G, Description],
        (graph(G), graph_desc(G, Description)),
        GraphRows
    ),
    format_rows([id, description], GraphRows, Rows)
.
