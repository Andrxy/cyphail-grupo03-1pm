:- encoding(utf8).

/* WHERE y aritmética con null

Evalúa las condiciones del WHERE con lógica de tres valores (true, false y
null) y calcula la aritmética propagando null, como Cypher.

CodeGen genera la condición del WHERE como un término y la envuelve en
cy_where/1. Por ejemplo, WHERE r.desde > 2010 AND b.edad >= 30 se traduce a:
cy_where(and(cy_greater_than(__Rdesde, 2010), cy_greater_or_equal(__Bedad, 30)))

Así una fila con una propiedad ausente (null) no pasa el filtro, y un OR que
se cumple por los dos lados no produce filas repetidas.

Alejandro Miranda
Sebastián González (documentación)
P2
*/

:- use_module(library(lists)).
:- ensure_loaded('comparisons.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Where %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_where(+Condition) is semidet.
%
%   Una fila pasa el WHERE solo si la condición es verdadera; false y null la descartan.
%
%   Condition  Término con and/2, or/2, not/1, is_null/1 y comparaciones cy_*.
cy_where(Condition) :-
    evaluate(Condition, Result),
    Result == true
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Arithmetic %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_calculate(+Expression, -Result) is det.
%
%   Calcula una expresión aritmética; si algún operando es null el resultado es null.
%
%   Solo revisa los operandos directos de la expresión: no entra a evaluar
%   si alguno de ellos es a su vez una subexpresión con un null escondido.
%
%   Expression  Expresión aritmética de Prolog.
%   Result      Su valor, o null.
cy_calculate(Expression, Result) :- (
    Expression =.. [_ | Operands],
    memberchk(null, Operands)
    ->  Result = null
    ;   Result is Expression
).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Utils %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   evaluate(+Condition, -Result) is det.
%
%   Evalúa una condición con tres valores: true, false o null.
%
%   Recorre el término de forma recursiva: and/2, or/2, not/1 e is_null/1 se
%   evalúan con sus propias reglas; cualquier otro término se trata como una
%   comparación.
%
%   Condition  Condición del WHERE.
%   Result     true, false o null.
evaluate(and(Left, Right), Result) :- !,
    evaluate(Left, LeftValue),
    evaluate(Right, RightValue),
    and_value(LeftValue, RightValue, Result)
.

evaluate(or(Left, Right), Result) :- !,
    evaluate(Left, LeftValue),
    evaluate(Right, RightValue),
    or_value(LeftValue, RightValue, Result)
.

evaluate(not(Inner), Result) :- !,
    evaluate(Inner, Value),
    not_value(Value, Result)
.

evaluate(is_null(Value), Result) :- !,
    null_value(Value, Result)
.

evaluate(Comparison, Result) :-
    cy_compare(Comparison, Result)
.

%   and_value(+Left, +Right, -Result) is det.
%
%   AND de tres valores: false decide; si no, null contagia.
%
%   Left    true, false o null.
%   Right   true, false o null.
%   Result  true, false o null.
and_value(false, _, false) :- ! .

and_value(_, false, false) :- ! .

and_value(true, true, true) :- ! .

and_value(_, _, null).

%   or_value(+Left, +Right, -Result) is det.
%
%   OR de tres valores: true decide; si no, null contagia.
%
%   Left    true, false o null.
%   Right   true, false o null.
%   Result  true, false o null.
or_value(true, _, true) :- ! .

or_value(_, true, true) :- ! .

or_value(false, false, false) :- ! .

or_value(_, _, null).

%   not_value(+Value, -Result) is det.
%
%   NOT de tres valores: null sigue siendo null.
%
%   Value   true, false o null.
%   Result  true, false o null.
not_value(true, false).
not_value(false, true).
not_value(null, null).

%   null_value(+Value, -Result) is det.
%
%   IS NULL nunca da null: siempre responde true o false.
%
%   Value   Valor que se examina.
%   Result  true si Value es null; false en otro caso.
null_value(null, true) :- ! .

null_value(_, false).
