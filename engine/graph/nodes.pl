:- encoding(utf8).

/** <module> Nodos del grafo actual

Consulta y modificación de nodos sobre el grafo actual. Los predicados de la
sección Match son los que CodeGen genera para los patrones de nodo; los de
Create, Set, Delete y Remove son los que genera para las cláusulas de
actualización. Todos esconden el argumento del grafo usando
get_current_graph/1.

Notación de modos y determinismo: ver engine.pl.

@author Alejandro Miranda
@author Sebastián González (documentación)
@version P2
*/

:- use_module(library(lists)).
:- ensure_loaded('graph.pl').
:- ensure_loaded('edges.pl').
:- ensure_loaded('../utils/mutations.pl').
:- ensure_loaded('../utils/edge_queries.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Match %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  cy_node(?Id_node) is nondet.
%
%   Busca un nodo en el grafo actual.
%
%   Con Id_node libre enumera todos los nodos por backtracking. CodeGen lo
%   genera para un patrón de nodo suelto, como (p), porque necesita recorrer
%   todos los nodos; en un camino, cy_edge/3 ya le da valor a los extremos.
%
%   @arg Id_node  Identificador del nodo, por ejemplo andrey.
%   @throws cy_error('No graph selected') si no hay grafo actual.
cy_node(Id_node) :-
    get_current_graph(G),
    node(G, Id_node)
.

%!  cy_node_label(?Id_node, ?Label) is nondet.
%
%   Busca una etiqueta de un nodo en el grafo actual.
%
%   Para el patrón (p:Persona), CodeGen genera cy_node_label(__P, 'Persona'),
%   que funciona como filtro.
%
%   @arg Id_node  Identificador del nodo.
%   @arg Label    Etiqueta, por ejemplo 'Persona'.
cy_node_label(Id_node, Label) :-
    get_current_graph(G),
    node_label(G, Id_node, Label)
.

%!  cy_node_prop(+Id_node, +Key, ?Value) is semidet.
%
%   Busca una propiedad de un nodo en el grafo actual; si no existe, leerla da null.
%
%   Tiene dos usos:
%     - Lectura (Value libre): unifica Value con el valor guardado o, si la
%       propiedad no existe, con null. Así RETURN p.edad muestra null y
%       WHERE p.edad IS NULL funciona como en Cypher.
%     - Filtro (Value instanciado): tiene éxito solo si el valor guardado es
%       Value. Un nodo sin la propiedad no pasa el filtro.
%
%   Id_node debe venir instanciado: el if-then-else (->) se queda con la
%   primera propiedad que encuentra, por eso no sirve para enumerar nodos.
%   CodeGen siempre lo genera después de cy_node/1 o cy_edge/3.
%
%   @arg Id_node  Identificador del nodo.
%   @arg Key      Nombre de la propiedad, por ejemplo nombre.
%   @arg Value    Valor de la propiedad, o null si no existe.
cy_node_prop(Id_node, Key, Value) :- (
    get_current_graph(G),
    node_prop(G, Id_node, Key, Value_exist)
    ->  Value = Value_exist
    ;   var(Value), Value = null
).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Create %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  cy_create_node(+Labels, +Props, -Id_node) is det.
%
%   Crea un nodo con sus etiquetas y propiedades Clave-Valor en el grafo actual y devuelve su id.
%
%   Corresponde a CREATE (a:Persona {nombre: 'Marta'}), que CodeGen traduce a
%   cy_create_node(['Persona'], ['nombre'-'Marta'], __A). El id se genera con
%   new_id/2 (n1, n2, ...). forall/2 recorre las listas solo por sus efectos.
%
%   @arg Labels   Lista de etiquetas.
%   @arg Props    Lista de pares Clave-Valor; los valores null no se guardan.
%   @arg Id_node  Id nuevo del nodo.
cy_create_node(Labels, Props, Id_node) :-
    new_id(n, Id_node),
    assert_node(Id_node),
    forall(member(Label, Labels), assert_node_label(Id_node, Label)),
    forall(member(Key-Value, Props), assert_node_prop(Id_node, Key, Value))
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Set %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  cy_set_node_label(+Id_node, +Label) is det.
%
%   Agrega una etiqueta a un nodo; si ya la tiene no hace nada.
%
%   Corresponde a SET a:Etiqueta. La comprobación previa evita etiquetas
%   duplicadas.
%
%   @arg Id_node  Identificador del nodo.
%   @arg Label    Etiqueta que se agrega.
cy_set_node_label(Id_node, Label) :- (
    cy_node_label(Id_node, Label)
    ->  true
    ;   assert_node_label(Id_node, Label)
).

%!  cy_set_node_prop(+Id_node, +Key, +Value) is det.
%
%   Cambia el valor de una propiedad de un nodo; asignar null la elimina.
%
%   Corresponde a SET a.clave = valor. Primero borra el valor anterior y luego
%   guarda el nuevo; si Value es null, assert_node_prop/3 no guarda nada y la
%   propiedad queda eliminada, como en Cypher.
%
%   @arg Id_node  Identificador del nodo.
%   @arg Key      Nombre de la propiedad.
%   @arg Value    Nuevo valor, o null para eliminarla.
cy_set_node_prop(Id_node, Key, Value) :-
    cy_remove_node_prop(Id_node, Key),
    assert_node_prop(Id_node, Key, Value)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Delete %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  cy_delete_node(+Id_node) is det.
%
%   Elimina un nodo sin relaciones; si todavía tiene alguna lanza un error, como Cypher.
%
%   Corresponde a DELETE n. Impide dejar relaciones colgando de un nodo que
%   ya no existe.
%
%   @arg Id_node  Identificador del nodo.
%   @throws cy_error('Cannot delete a node that still has relationships, use DETACH DELETE')
%           si el nodo aún tiene relaciones.
cy_delete_node(Id_node) :- (
    node_has_edges(Id_node)
    ->  throw(cy_error('Cannot delete a node that still has relationships, use DETACH DELETE'))
    ;   remove_node(Id_node)
).

%!  cy_detach_delete_node(+Id_node) is det.
%
%   Elimina un nodo junto con todas las relaciones que entran o salen de él.
%
%   Corresponde a DETACH DELETE n.
%
%   @arg Id_node  Identificador del nodo.
cy_detach_delete_node(Id_node) :-
    forall(node_edge(Id_node, Id_edge), cy_delete_edge(Id_edge)),
    remove_node(Id_node)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Remove %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%!  cy_remove_node_label(+Id_node, +Label) is det.
%
%   Quita una etiqueta de un nodo; si no la tiene no hace nada.
%
%   Corresponde a REMOVE a:Etiqueta. retractall/1 nunca falla, aunque no
%   haya nada que borrar.
%
%   @arg Id_node  Identificador del nodo.
%   @arg Label    Etiqueta que se quita.
cy_remove_node_label(Id_node, Label) :-
    get_current_graph(G),
    retractall(node_label(G, Id_node, Label))
.

%!  cy_remove_node_prop(+Id_node, +Key) is det.
%
%   Elimina una propiedad de un nodo; si no la tiene no hace nada.
%
%   Corresponde a REMOVE a.clave. También lo usa cy_set_node_prop/3 para
%   borrar el valor anterior.
%
%   @arg Id_node  Identificador del nodo.
%   @arg Key      Nombre de la propiedad.
cy_remove_node_prop(Id_node, Key) :-
    get_current_graph(G),
    retractall(node_prop(G, Id_node, Key, _))
.
