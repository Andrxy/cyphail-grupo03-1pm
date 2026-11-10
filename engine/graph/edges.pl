:- encoding(utf8).

/* Relaciones del grafo actual

Consulta y modificación de relaciones (edges) sobre el grafo actual.

El CodeGen resuelve la dirección al generar el código.
(a)-[r]->(b) se traduce a cy_edge(__R, __A, __B),
(a)<-[r]-(b) a cy_edge(__R, __B, __A), y
(a)-[r]-(b) a cy_edge(__R, __A, __B) ; cy_edge(__R, __B, __A)

Alejandro Miranda
Sebastián González (documentación)
P2
*/

:- use_module(library(lists)).
:- ensure_loaded('graph.pl').
:- ensure_loaded('../utils/mutations.pl').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Match %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_edge(?Id_edge, ?From, ?To) is nondet.
%
%   Busca una relación y sus extremos en el grafo actual.
%
%   Con argumentos libres enumera todas las relaciones por backtracking y le
%   da valor a los dos nodos del patrón.
%
%   Id_edge  Identificador de la relación, por ejemplo e4.
%   From     Nodo de origen.
%   To       Nodo de destino.
cy_edge(Id_edge, From, To) :-
    get_current_graph(G),
    edge(G, Id_edge, From, To)
.

%   cy_edge_type(?Id_edge, ?Type) is nondet.
%
%   Busca el tipo de una relación en el grafo actual.
%
%   Se usa como filtro, [r:CONOCE] genera cy_edge_type(__R, 'CONOCE')
%
%   Id_edge  Identificador de la relación.
%   Type     Tipo, por ejemplo 'CONOCE'.
cy_edge_type(Id_edge, Type) :-
    get_current_graph(G),
    edge_type(G, Id_edge, Type)
.

%   cy_edge_prop(+Id_edge, +Key, ?Value) is semidet.
%
%   Busca una propiedad de una relación en el grafo actual; si no existe, leerla da null.
%
%   Tiene dos usos:
%     - Lectura (Value libre): unifica Value con el valor guardado o, si la
%       propiedad no existe, con null. Así RETURN r.desde muestra null y
%       WHERE r.desde IS NULL funciona como en Cypher.
%     - Filtro (Value instanciado): tiene éxito solo si el valor guardado es
%       Value. Una relación sin la propiedad no pasa el filtro.
%
%   Id_edge  Identificador de la relación.
%   Key      Nombre de la propiedad, por ejemplo desde.
%   Value    Valor de la propiedad, o null si no existe.
cy_edge_prop(Id_edge, Key, Value) :- (
    get_current_graph(G),
    edge_prop(G, Id_edge, Key, Value_exist)
    ->  Value = Value_exist
    ;   var(Value), Value = null
).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Create %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_create_edge(+From, +To, +Type, +Props, -Id_edge) is det.
%
%   Crea una relación de un tipo entre dos nodos del grafo actual y devuelve su id.
%
%   Corresponde a CREATE (a)-[r:TIPO {clave: valor}]->(b).
%
%   From     Nodo de origen.
%   To       Nodo de destino.
%   Type     Tipo de la relación.
%   Props    Lista de pares Clave-Valor; los valores null no se guardan.
%   Id_edge  Id nuevo de la relación.
cy_create_edge(From, To, Type, Props, Id_edge) :-
    new_id(e, Id_edge),
    assert_edge(Id_edge, From, To),
    assert_edge_type(Id_edge, Type),
    forall(member(Key-Value, Props), assert_edge_prop(Id_edge, Key, Value))
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Set %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_set_edge_prop(+Id_edge, +Key, +Value) is det.
%
%   Cambia el valor de una propiedad de una relación; asignar null la elimina.
%
%   Corresponde a SET r.clave = valor.
%
%   Id_edge  Identificador de la relación.
%   Key      Nombre de la propiedad.
%   Value    Nuevo valor, o null para eliminarla.
cy_set_edge_prop(Id_edge, Key, Value) :-
    cy_remove_edge_prop(Id_edge, Key),
    assert_edge_prop(Id_edge, Key, Value)
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Delete %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_delete_edge(+Id_edge) is det.
%
%   Elimina una relación con su tipo y sus propiedades; los nodos de sus extremos no cambian.
%
%   Corresponde a DELETE r. También lo usa cy_detach_delete_node/1.
%
%   Id_edge  Identificador de la relación.
cy_delete_edge(Id_edge) :-
    get_current_graph(G),
    retractall(edge(G, Id_edge, _, _)),
    retractall(edge_type(G, Id_edge, _)),
    retractall(edge_prop(G, Id_edge, _, _))
.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Remove %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   cy_remove_edge_prop(+Id_edge, +Key) is det.
%
%   Elimina una propiedad de una relación; si no la tiene no hace nada.
%
%   Corresponde a REMOVE r.clave.
%
%   Id_edge  Identificador de la relación.
%   Key      Nombre de la propiedad.
cy_remove_edge_prop(Id_edge, Key) :-
    get_current_graph(G),
    retractall(edge_prop(G, Id_edge, Key, _))
.
