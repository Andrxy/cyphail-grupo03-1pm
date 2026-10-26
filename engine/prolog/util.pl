:- use_module(library(apply)).
:- use_module(library(lists)).

% Convierte un átomo o string en un átomo para comparar textos.
text_atom(Value, Text) :-
    ( atom(Value) ->
        Text = Value
    ;
        string(Value),
        atom_string(Text, Value)
    ).

% Convierte los encabezados y las filas en líneas de texto.
format_rows([], _, '') :-
    !.

format_rows(Headers, Solutions, Rows) :-
    maplist(format_row, [Headers | Solutions], Lines),
    atomic_list_concat(Lines, '\n', Rows).

% Convierte una lista de valores en una línea.
format_row(Values, Line) :-
    maplist(format_cell, Values, Cells),
    atomic_list_concat(Cells, ' ', Line).

% Representa null sin comillas y los demás valores entre comillas.
format_cell(null, null) :-
    !.

format_cell(Value, Cell) :-
    cell_text(Value, Text),
    atom_codes(Text, Codes),
    maplist(escape_code, Codes, EscapedParts),
    append(EscapedParts, EscapedCodes),
    atom_codes(EscapedText, EscapedCodes),
    format(atom(Cell), '"~a"', [EscapedText]).

% Convierte un átomo, string o número en texto para una celda.
cell_text(Value, Text) :-
    ( atom(Value) ->
        Text = Value
    ; string(Value) ->
        atom_string(Text, Value)
    ; number(Value) ->
        format(atom(Text), '~w', [Value])
    ;
        throw(cy_error('Unsupported result value'))
    ).

% Escapa comillas, barras invertidas y saltos de línea de una celda.
escape_code(34, [92, 34]) :-
    !.

escape_code(92, [92, 92]) :-
    !.

escape_code(10, [92, 110]) :-
    !.

escape_code(Code, [Code]).
