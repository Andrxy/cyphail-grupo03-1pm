:- encoding(utf8).

/** <module> Formato Rows

Convierte las soluciones de una consulta en el texto que viaja por MQI hasta
Java, donde RowsParser lo vuelve a convertir en tabla. Reglas del formato:

  - una línea por fila; la primera línea son los títulos;
  - cada celda entre comillas dobles, separadas por un espacio;
  - dentro de una celda, " se escribe \", \ se escribe \\ y el salto de línea \n;
  - null va sin comillas;
  - sin títulos, el resultado es el texto vacío ''.

Las comillas permiten valores con espacios, como 'Desarrollador en Prolog'.

Notación de modos y determinismo: ver engine.pl.

@author Alejandro Miranda
@author Sebastián González (documentación)
@version P2
*/

:- use_module(library(apply)).
:- use_module(library(lists)).

%!  format_rows(+Headers, +Solutions, -Rows) is det.
%
%   Convierte los encabezados y las filas en líneas de texto.
%
%   maplist/3 aplica format_row/2 a cada fila (es el map de la programación
%   funcional) y atomic_list_concat/3 las une con saltos de línea. La primera
%   cláusula cubre las actualizaciones sin RETURN.
%
%   @arg Headers    Lista de títulos.
%   @arg Solutions  Lista de filas; cada fila es una lista de valores.
%   @arg Rows       Texto en formato Rows.
format_rows([], _, '') :- ! .

format_rows(Headers, Solutions, Rows) :-
    maplist(format_row, [Headers | Solutions], Lines),
    atomic_list_concat(Lines, '\n', Rows)
.

%!  format_row(+Values, -Line) is det.
%
%   Convierte una lista de valores en una línea.
%
%   @arg Values  Valores de una fila.
%   @arg Line    Celdas separadas por un espacio.
format_row(Values, Line) :-
    maplist(format_cell, Values, Cells),
    atomic_list_concat(Cells, ' ', Line)
.

%!  format_cell(+Value, -Cell) is det.
%
%   Representa null sin comillas y los demás valores entre comillas.
%
%   El texto se pasa a códigos de carácter, se escapa cada código con
%   escape_code/2 y se vuelve a unir.
%
%   @arg Value  Valor de la celda.
%   @arg Cell   Celda lista para la línea.
%   @throws cy_error('Unsupported result value') si Value no es átomo, string ni número.
format_cell(null, null) :- ! .

format_cell(Value, Cell) :-
    cell_text(Value, Text),
    atom_codes(Text, Codes),
    maplist(escape_code, Codes, EscapedParts),
    append(EscapedParts, EscapedCodes),
    atom_codes(EscapedText, EscapedCodes),
    format(atom(Cell), '"~a"', [EscapedText])
.

%!  cell_text(+Value, -Text) is det.
%
%   Convierte un átomo, string o número en texto para una celda.
%
%   @arg Value  Átomo, string o número.
%   @arg Text   Su representación como átomo.
%   @throws cy_error('Unsupported result value') para cualquier otro término.
cell_text(Value, Text) :- (
    atom(Value)
    ->  Text = Value
    ;   string(Value)
    ->  atom_string(Text, Value)
    ;   number(Value)
    ->  format(atom(Text), '~w', [Value])
    ;   throw(cy_error('Unsupported result value'))
).

%!  escape_code(+Code, -Codes) is det.
%
%   Escapa comillas, barras invertidas y saltos de línea de una celda.
%
%   34 es ", 92 es \ y 10 es el salto de línea; cualquier otro código queda
%   igual.
%
%   @arg Code   Código de un carácter.
%   @arg Codes  Lista de códigos que lo reemplaza.
escape_code(34, [92, 34]) :- ! .

escape_code(92, [92, 92]) :- ! .

escape_code(10, [92, 110]) :- ! .

escape_code(Code, [Code]).
