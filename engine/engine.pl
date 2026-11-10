:- encoding(utf8).

/* Engine de Cyphail

Punto de entrada del motor en SWI-Prolog. El broker arranca swipl con este
archivo (swipl -l engine/engine.pl mqi ...) y, por MQI, le envía las metas que
genera CodeGen. Este archivo carga los datos y las utilidades, y define los tres
predicados que el broker invoca directamente: cy_exec_query/4, cy_exec_update/4
y cy_graphs/1.

Determinismo:

  det       tiene exactamente una solución.
  semidet   tiene una solución o falla.
  nondet    puede tener cero, una o varias soluciones (backtracking).

Los errores propios del engine se lanzan como cy_error(Mensaje). El broker los
envuelve con catch/3 para que MQI los entregue con su mensaje completo.

Alejandro Miranda
Sebastián González
P2
*/

:- ensure_loaded('data/amigos.pl').
:- ensure_loaded('data/ingenieria.pl').
:- ensure_loaded('utils/rows.pl').
:- ensure_loaded('utils/comparisons.pl').
:- ensure_loaded('utils/expressions.pl').
:- ensure_loaded('graph/graph.pl').
:- ensure_loaded('graph/nodes.pl').
:- ensure_loaded('graph/edges.pl').
:- ensure_loaded('graph/paths.pl').

%   cy_exec_query(+Goal, +Headers, +Columns, -Rows) is det.
%
%   Ejecuta el patrón y convierte todas sus soluciones en texto.
%
%   Aunque la consulta tenga varias filas, el resultado es una sola solución:
%   Rows ya contiene el texto con todas las filas armado.
%
%   Goal     Conjunción de metas cy_* que representa el patrón de la consulta.
%   Headers  Lista de títulos de las columnas, por ejemplo ['p.nombre'].
%   Columns  Lista de variables de Goal, una por columna, en el mismo orden.
%   Rows     Texto con los títulos y las filas (formato Rows, ver format_rows/3).
cy_exec_query(Goal, Headers, Columns, Rows) :-
    findall(Columns, Goal, Solutions),
    format_rows(Headers, Solutions, Rows)
.

%   cy_exec_update(+Goal, +Headers, +Columns, -Rows) is det.
%
%   Ejecuta una actualizacion y revierte sus cambios si ocurre un error.
%
%   Se usa en consultas con CREATE, SET, REMOVE o DELETE, que modifican el grafo.
%   Si una meta falla a mitad del camino, ningún cambio anterior queda aplicado.
%
%   Goal     Conjunción de metas de lectura y de mutación.
%   Headers  Títulos de las columnas del RETURN, o [] si no hay RETURN.
%   Columns  Variables de las columnas, o [] si no hay RETURN.
%   Rows     Texto en formato Rows; '' si no hay columnas.
cy_exec_update(Goal, Headers, Columns, Rows) :-
    transaction(cy_exec_query(Goal, Headers, Columns, Rows))
.

%   cy_graphs(-Rows) is det.
%
%   Devuelve los grafos disponibles y sus descripciones en formato Rows.
%
%   Lo invoca el comando .use sin argumentos. Los grafos salen de los hechos
%   graph/1 y graph_desc/2 de todos los archivos de data/.
%
%   Rows  Texto en formato Rows con las columnas id y description.
cy_graphs(Rows) :-
    findall(
        [G, Description],
        (graph(G), graph_desc(G, Description)),
        GraphRows
    ),
    format_rows([id, description], GraphRows, Rows)
.
