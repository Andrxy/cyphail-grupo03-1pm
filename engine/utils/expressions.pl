:- encoding(utf8).
:- use_module(library(lists)).
:- ensure_loaded('comparisons.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Where %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Una fila pasa el WHERE solo si la condición es verdadera; false y null la descartan.
cy_where(Condition) :-
    evaluate(Condition, Result),
    Result == true
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Arithmetic %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Calcula una expresión aritmética; si algún operando es null el resultado es null.
cy_calculate(Expression, Result) :- (
    Expression =.. [_ | Operands],
    memberchk(null, Operands)
    ->  Result = null
    ;   Result is Expression
).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Utils %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Evalúa una condición con tres valores: true, false o null.
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

% AND de tres valores: false decide; si no, null contagia.
and_value(false, _, false) :- ! .

and_value(_, false, false) :- ! .

and_value(true, true, true) :- ! .

and_value(_, _, null).

% OR de tres valores: true decide; si no, null contagia.
or_value(true, _, true) :- ! .

or_value(_, true, true) :- ! .

or_value(false, false, false) :- ! .

or_value(_, _, null).

% NOT de tres valores: null sigue siendo null.
not_value(true, false).
not_value(false, true).
not_value(null, null).

% IS NULL nunca da null: siempre responde true o false.
null_value(null, true) :- ! .

null_value(_, false).