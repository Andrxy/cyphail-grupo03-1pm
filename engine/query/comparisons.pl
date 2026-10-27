:- encoding(utf8).
:- ensure_loaded('rows.pl').

% Compara números por valor y otros términos por identidad.
cy_equal(Left, Right) :-
    ( number(Left), number(Right)
    ->  Left =:= Right
    ;   Left == Right
    )
.

% Comprueba que dos valores sean diferentes.
cy_not_equal(Left, Right) :-
    \+ cy_equal(Left, Right)
.

% Comprueba si un número o texto es menor que otro del mismo tipo.
cy_less_than(Left, Right) :-
    ( number(Left), number(Right)
    ->  Left < Right
    ;   text_atom(Left, LeftText),
        text_atom(Right, RightText),
        LeftText @< RightText
    )
.

% Comprueba si el primer valor es mayor que el segundo.
cy_greater_than(Left, Right) :-
    cy_less_than(Right, Left)
.

% Comprueba si el primer valor es menor o igual que el segundo.
cy_less_or_equal(Left, Right) :-
    ( number(Left), number(Right)
    ->  Left =< Right
    ;   text_atom(Left, LeftText),
        text_atom(Right, RightText),
        LeftText @=< RightText
    )
.

% Comprueba si el primer valor es mayor o igual que el segundo.
cy_greater_or_equal(Left, Right) :-
    cy_less_or_equal(Right, Left)
.
