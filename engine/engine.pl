:- encoding(utf8).

/** <module> Engine de Cyphail

Punto de entrada del motor en SWI-Prolog. El broker arranca swipl con este
archivo (swipl -l engine/engine.pl mqi ...) y, por MQI, le envía las metas que
genera CodeGen. Este archivo carga los datos y las utilidades, y define los tres
predicados que el broker invoca directamente: cy_exec_query/4, cy_exec_update/4
y cy_graphs/1.

Organización del engine:

  data/     hechos de cada grafo (node/2, edge/4, ...)
  graph/    grafo actual y vistas sobre él (cy_node, cy_edge, cy_path)
  utils/    comparaciones, WHERE, aritmética, mutaciones y formato Rows

Notación de modos (PlDoc, la convención vista en clase):

  +Arg   entrada: debe venir instanciado.
  -Arg   salida: llega libre y el predicado le da valor.
  ?Arg   entrada o salida.

Determinismo:

  det       tiene exactamente una solución.
  semidet   tiene una solución o falla.
  nondet    puede tener cero, una o varias soluciones (backtracking).

Los errores propios del engine se lanzan como cy_error(Mensaje). El broker los
envuelve con catch/3 para que MQI los entregue con su mensaje completo.

@author Alejandro Miranda
@author Sebastián González
@version P2
@see Documentación con asistencia de Claude (Anthropic), declarada en el README, Sprint P2.
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

%!  cy_exec_query(+Goal, +Headers, +Columns, -Rows) is det.
%
%   Ejecuta el patrón y convierte todas sus soluciones en texto.
%
%   Es el predicado que envuelve toda consulta transpilada. findall/3
%   recorre por backtracking todas las soluciones de Goal y, por cada una,
%   guarda la lista Columns. Así, aunque la tabla tenga varias filas, MQI
%   devuelve una sola solución cuyo binding Rows ya contiene la tabla.
%
%   @arg Goal     Conjunción de metas cy_* generada por CodeGen.
%   @arg Headers  Lista de títulos de las columnas, por ejemplo ['p.nombre'].
%   @arg Columns  Lista de variables de Goal, una por columna, en el mismo orden.
%   @arg Rows     Texto con los títulos y las filas (formato Rows, ver format_rows/3).
%   @throws cy_error(Mensaje) si Goal lanza un error propio del engine.
cy_exec_query(Goal, Headers, Columns, Rows) :-
    findall(Columns, Goal, Solutions),
    format_rows(Headers, Solutions, Rows)
.

%!  cy_exec_update(+Goal, +Headers, +Columns, -Rows) is det.
%
%   Ejecuta una actualizacion y revierte sus cambios si ocurre un error.
%
%   CodeGen lo usa cuando la consulta tiene CREATE, SET, REMOVE o DELETE.
%   transaction/1 hace la ejecución atómica: si una meta lanza una excepción
%   a mitad del camino, ningún assert ni retract anterior queda aplicado.
%
%   @arg Goal     Conjunción de metas de lectura y de mutación.
%   @arg Headers  Títulos de las columnas del RETURN, o [] si no hay RETURN.
%   @arg Columns  Variables de las columnas, o [] si no hay RETURN.
%   @arg Rows     Texto en formato Rows; '' si no hay columnas.
%   @throws cy_error(Mensaje) o un error de Prolog; en ambos casos se revierte todo.
cy_exec_update(Goal, Headers, Columns, Rows) :-
    transaction(cy_exec_query(Goal, Headers, Columns, Rows))
.

%!  cy_graphs(-Rows) is det.
%
%   Devuelve los grafos disponibles y sus descripciones en formato Rows.
%
%   Lo invoca el comando .use sin argumentos. Los grafos salen de los hechos
%   graph/1 y graph_desc/2 de todos los archivos de data/.
%
%   @arg Rows  Texto en formato Rows con las columnas id y description.
cy_graphs(Rows) :-
    findall(
        [G, Description],
        (graph(G), graph_desc(G, Description)),
        GraphRows
    ),
    format_rows([id, description], GraphRows, Rows)
.
