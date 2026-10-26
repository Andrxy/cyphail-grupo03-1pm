:- ensure_loaded('data/graphs.pl').
:- ensure_loaded('prolog/util.pl').
:- ensure_loaded('prolog/comparisons.pl').
:- ensure_loaded('prolog/graph/current_graph.pl').
:- ensure_loaded('prolog/graph/nodes.pl').
:- ensure_loaded('prolog/graph/relationships.pl').

% Ejecuta el patrón y convierte todas sus soluciones en texto.
cy_exec_query(Headers, Columns, Goal, Rows) :-
    length(Headers, HeaderCount),
    length(Columns, ColumnCount),
    ( HeaderCount =:= ColumnCount -> true
    ; throw(cy_error('Header count does not match column count'))
    ),
    findall(Columns, Goal, Solutions),
    format_rows(Headers, Solutions, Rows).

% Devuelve los grafos disponibles y sus descripciones en formato Rows.
cy_graphs(Rows) :-
    findall(
        [G, Description],
        (graph(G), graph_desc(G, Description)),
        GraphRows
    ),
    format_rows([id, description], GraphRows, Rows).
