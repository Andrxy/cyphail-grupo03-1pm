:- encoding(utf8).

/** <module> Mutaciones de bajo nivel

Agrega y quita hechos del grafo actual con assert/1 y retractall/1. Lo usan
nodes.pl y edges.pl para implementar CREATE, SET, REMOVE y DELETE. Funciona
porque los archivos de data/ declaran sus predicados como dinámicos.

Los cambios viven en la memoria del proceso swipl: duran mientras Cyphail esté
abierto y no se escriben en los archivos de data/.

Notación de modos y determinismo: ver engine.pl.

@author Alejandro Miranda
@author Sebastián González (documentación)
@version P2
*/

:- ensure_loaded('../graph/graph.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Assert %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  assert_node(+Id_node) is det.
%
%   Agrega un nodo al grafo actual.
%
%   @arg Id_node  Identificador del nodo nuevo.
assert_node(Id_node) :-
    get_current_graph(G),
    assert(node(G, Id_node))
.

%!  assert_node_label(+Id_node, +Label) is det.
%
%   Agrega una etiqueta a un nodo del grafo actual.
%
%   @arg Id_node  Identificador del nodo.
%   @arg Label    Etiqueta.
assert_node_label(Id_node, Label) :-
    get_current_graph(G),
    assert(node_label(G, Id_node, Label))
.

%!  assert_node_prop(+Id_node, +Key, +Value) is det.
%
%   Agrega una propiedad a un nodo del grafo actual, salvo que el valor sea null,
%   que en Cypher equivale a no tener la propiedad.
%
%   @arg Id_node  Identificador del nodo.
%   @arg Key      Nombre de la propiedad.
%   @arg Value    Valor; si es null no se guarda nada.
assert_node_prop(_, _, null) :- ! .

assert_node_prop(Id_node, Key, Value) :-
    get_current_graph(G),
    assert(node_prop(G, Id_node, Key, Value))
.

%!  assert_edge(+Id_edge, +From, +To) is det.
%
%   Agrega una relación entre dos nodos del grafo actual.
%
%   @arg Id_edge  Identificador de la relación nueva.
%   @arg From     Nodo de origen.
%   @arg To       Nodo de destino.
assert_edge(Id_edge, From, To) :-
    get_current_graph(G),
    assert(edge(G, Id_edge, From, To))
.

%!  assert_edge_type(+Id_edge, +Type) is det.
%
%   Asocia una relación del grafo actual con su tipo.
%
%   @arg Id_edge  Identificador de la relación.
%   @arg Type     Tipo de la relación.
assert_edge_type(Id_edge, Type) :-
    get_current_graph(G),
    assert(edge_type(G, Id_edge, Type))
.

%!  assert_edge_prop(+Id_edge, +Key, +Value) is det.
%
%   Agrega una propiedad a una relación del grafo actual, salvo que el valor sea null,
%   que en Cypher equivale a no tener la propiedad.
%
%   @arg Id_edge  Identificador de la relación.
%   @arg Key      Nombre de la propiedad.
%   @arg Value    Valor; si es null no se guarda nada.
assert_edge_prop(_, _, null) :- ! .

assert_edge_prop(Id_edge, Key, Value) :-
    get_current_graph(G),
    assert(edge_prop(G, Id_edge, Key, Value))
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Retract %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  remove_node(+Id_node) is det.
%
%   Quita un nodo con sus etiquetas y propiedades.
%
%   No revisa relaciones: esa decisión la toman cy_delete_node/1 y
%   cy_detach_delete_node/1.
%
%   @arg Id_node  Identificador del nodo.
remove_node(Id_node) :-
    get_current_graph(G),
    retractall(node(G, Id_node)),
    retractall(node_label(G, Id_node, _)),
    retractall(node_prop(G, Id_node, _, _))
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Ids %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  new_id(+Prefix, -Id_element) is det.
%
%   Genera el primer id con el prefijo y un número que no use ningún nodo ni relación del grafo actual.
%
%   between(1, infinite, Number) propone 1, 2, 3, ... por backtracking hasta
%   que un candidato esté libre; el corte final se queda con ese primero.
%   Con el prefijo n produce n1, n2, ...; con e produce e1, e2, ...
%
%   @arg Prefix      Prefijo del id: n para nodos, e para relaciones.
%   @arg Id_element  Id libre en el grafo actual.
new_id(Prefix, Id_element) :-
    get_current_graph(G),
    between(1, infinite, Number),
    atom_concat(Prefix, Number, Id_element),
    \+ node(G, Id_element),
    \+ edge(G, Id_element, _, _),
    !
.
