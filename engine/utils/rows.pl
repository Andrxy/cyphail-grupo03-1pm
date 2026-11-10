:- encoding(utf8).

/* Formato Rows

Convierte las soluciones de una consulta en el texto que viaja por MQI hasta
Java, donde RowsParser lo vuelve a convertir en tabla. Reglas del formato:

  - una línea por fila; la primera línea son los títulos;
  - cada celda entre comillas dobles, separadas por un espacio;
  - dentro de una celda, " se escribe \", \ se escribe \\ y el salto de línea \n;
  - null va sin comillas;
  - sin títulos, el resultado es el texto vacío ''.

Las comillas permiten valores con espacios, como 'Desarrollador en Prolog'.

Alejandro Miranda
Sebastián González (documentación)
P2
*/

:- use_module(library(apply)).
:- use_module(library(lists)).

%   format_rows(+Headers, +Solutions, -Rows) is det.
%
%   Convierte los encabezados y las filas en líneas de texto, separadas por
%   saltos de línea. La primera cláusula cubre las actualizaciones sin
%   RETURN, donde no hay encabezados.
%
%   Headers    Lista de títulos.
%   Solutions  Lista de filas; cada fila es una lista de valores.
%   Rows       Texto en formato Rows.
format_rows([], _, '') :- ! .

format_rows(Headers, Solutions, Rows) :-
    maplist(format_row, [Headers | Solutions], Lines),
    atomic_list_concat(Lines, '\n', Rows)
.

%   format_row(+Values, -Line) is det.
%
%   Convierte una lista de valores en una línea.
%
%   Values  Valores de una fila.
%   Line    Celdas separadas por un espacio.
format_row(Values, Line) :-
    maplist(format_cell, Values, Cells),
    atomic_list_concat(Cells, ' ', Line)
.

%   format_cell(+Value, -Cell) is det.
%
%   Representa null sin comillas y los demás valores entre comillas, con su
%   contenido escapado para que las comillas, barras invertidas y saltos de
%   línea no rompan el formato (ver escape_code/2).
%
%   Value  Valor de la celda.
%   Cell   Celda lista para la línea.
format_cell(null, null) :- ! .

format_cell(Value, Cell) :-
    cell_text(Value, Text),
    atom_codes(Text, Codes),
    maplist(escape_code, Codes, EscapedParts),
    append(EscapedParts, EscapedCodes),
    atom_codes(EscapedText, EscapedCodes),
    format(atom(Cell), '"~a"', [EscapedText])
.

%   cell_text(+Value, -Text) is det.
%
%   Convierte un átomo, string o número en texto para una celda.
%
%   Value  Átomo, string o número.
%   Text   Su representación como átomo.
cell_text(Value, Text) :- (
    atom(Value)
    ->  Text = Value
    ;   string(Value)
    ->  atom_string(Text, Value)
    ;   number(Value)
    ->  format(atom(Text), '~w', [Value])
    ;   throw(cy_error('Unsupported result value'))
).

%   escape_code(+Code, -Codes) is det.
%
%   Escapa comillas, barras invertidas y saltos de línea de una celda.
%
%   34 es ", 92 es \ y 10 es el salto de línea; cualquier otro código queda
%   igual.
%
%   Code   Código de un carácter.
%   Codes  Lista de códigos que lo reemplaza.
escape_code(34, [92, 34]) :- ! .

escape_code(92, [92, 92]) :- ! .

escape_code(10, [92, 110]) :- ! .

escape_code(Code, [Code]).
