:- encoding(utf8).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Comparisons %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Compara números por valor y otros términos por identidad.
cy_equal(Left, Right) :- (
    number(Left), number(Right)
    ->  Left =:= Right
    ;   Left == Right
).

% Comprueba que dos valores sean diferentes.
cy_not_equal(Left, Right) :-
    \+ cy_equal(Left, Right)
.

% Comprueba si un número o texto es menor que otro del mismo tipo.
cy_less_than(Left, Right) :- (
    number(Left), number(Right)
    ->  Left < Right
    ;   text_atom(Left, LeftText),
        text_atom(Right, RightText),
        LeftText @< RightText
).

% Comprueba si el primer valor es mayor que el segundo.
cy_greater_than(Left, Right) :-
    cy_less_than(Right, Left)
.

% Comprueba si el primer valor es menor o igual que el segundo.
cy_less_or_equal(Left, Right) :- (
    cy_equal(Left, Right)
    ;
    cy_less_than(Left, Right)
).

% Comprueba si el primer valor es mayor o igual que el segundo.
cy_greater_or_equal(Left, Right) :-
    cy_less_or_equal(Right, Left)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Three-valued %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Evalúa una comparación con tres valores: si un operando es null, el resultado es null.
cy_compare(Comparison, Result) :- (
    Comparison =.. [_, Left, Right],
    (Left == null ; Right == null)
    ->  Result = null
    ;   call(Comparison)
    ->  Result = true
    ;   Result = false
).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Utils %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Convierte un átomo o string en un átomo para comparar textos.
text_atom(Value, Text) :- (
    atom(Value)
    ->  Text = Value
    ;   string(Value),
        atom_string(Text, Value)
).
