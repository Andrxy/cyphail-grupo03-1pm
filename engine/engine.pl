:- ensure_loaded('data/amigos.pl').
:- ensure_loaded('query/rows.pl').
:- ensure_loaded('query/comparisons.pl').
:- ensure_loaded('graph/graph.pl').
:- ensure_loaded('graph/nodes.pl').
:- ensure_loaded('graph/relationships.pl').

% Ejecuta el patrón y convierte todas sus soluciones en texto.
cy_exec_query(Headers, Columns, Goal, Rows) :-
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
