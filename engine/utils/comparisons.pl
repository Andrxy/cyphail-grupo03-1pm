:- encoding(utf8).

/* Comparaciones

Comparaciones que usa el código generado para los operadores del WHERE:
= es cy_equal/2, <> es cy_not_equal/2, < es cy_less_than/2, > es
cy_greater_than/2, <= es cy_less_or_equal/2 y >= es cy_greater_or_equal/2.

Existen porque los operadores de Prolog no bastan: 28 == 28.0 es falso en
Prolog, pero en Cypher 28 = 28.0 es verdadero. Los números se comparan por
valor y los textos en orden alfabético.

Alejandro Miranda
Sebastián González (documentación)
P2
*/

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Comparisons %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_equal(+Left, +Right) is semidet.
%
%   Compara números por valor y otros términos por identidad.
%
%   =:= compara números por valor; == compara cualquier otro término por
%   identidad, sin unificar.
%
%   Left   Primer valor.
%   Right  Segundo valor.
cy_equal(Left, Right) :- (
    number(Left), number(Right)
    ->  Left =:= Right
    ;   Left == Right
).

%   cy_not_equal(+Left, +Right) is semidet.
%
%   Comprueba que dos valores sean diferentes.
%
%   Left   Primer valor.
%   Right  Segundo valor.
cy_not_equal(Left, Right) :-
    \+ cy_equal(Left, Right)
.

%   cy_less_than(+Left, +Right) is semidet.
%
%   Comprueba si un número o texto es menor que otro del mismo tipo.
%
%   Dos números se comparan con <. Dos textos (átomos o strings) se comparan
%   en orden estándar con @<. Si los tipos no coinciden, falla.
%
%   Left   Primer valor.
%   Right  Segundo valor.
cy_less_than(Left, Right) :- (
    number(Left), number(Right)
    ->  Left < Right
    ;   text_atom(Left, LeftText),
        text_atom(Right, RightText),
        LeftText @< RightText
).

%   cy_greater_than(+Left, +Right) is semidet.
%
%   Comprueba si el primer valor es mayor que el segundo.
%
%   Left   Primer valor.
%   Right  Segundo valor.
cy_greater_than(Left, Right) :-
    cy_less_than(Right, Left)
.

%   cy_less_or_equal(+Left, +Right) is semidet.
%
%   Comprueba si el primer valor es menor o igual que el segundo.
%
%   Left   Primer valor.
%   Right  Segundo valor.
cy_less_or_equal(Left, Right) :- (
    cy_equal(Left, Right)
    ;
    cy_less_than(Left, Right)
).

%   cy_greater_or_equal(+Left, +Right) is semidet.
%
%   Comprueba si el primer valor es mayor o igual que el segundo.
%
%   Left   Primer valor.
%   Right  Segundo valor.
cy_greater_or_equal(Left, Right) :-
    cy_less_or_equal(Right, Left)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Three-valued %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_compare(+Comparison, -Result) is det.
%
%   Evalúa una comparación con tres valores: si un operando es null, el resultado es null.
%
%   Revisa los dos operandos de Comparison (por ejemplo, los de
%   cy_greater_than(31, 30)); si ninguno es null, evalúa la comparación
%   normalmente.
%
%   Comparison  Meta de comparación con dos operandos.
%   Result      true, false o null.
cy_compare(Comparison, Result) :- (
    Comparison =.. [_, Left, Right],
    (Left == null ; Right == null)
    ->  Result = null
    ;   call(Comparison)
    ->  Result = true
    ;   Result = false
).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Utils %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   text_atom(+Value, -Text) is semidet.
%
%   Convierte un átomo o string en un átomo para comparar textos.
%
%   Falla si Value no es átomo ni string; por eso cy_less_than/2 falla al
%   comparar un texto con un número.
%
%   Value  Átomo o string.
%   Text   El mismo texto como átomo.
text_atom(Value, Text) :- (
    atom(Value)
    ->  Text = Value
    ;   string(Value),
        atom_string(Text, Value)
).
