:- encoding(utf8).

/* Datos del grafo amigos

Grafo pequeño de ejemplo: dos personas, Ana y Luis, unidas por dos relaciones (AMIGO_DE y CONOCE).

Cada hecho lleva el nombre del grafo como primer argumento; así varios grafos
conviven en el mismo engine. El código generado no consulta estos hechos
directamente: usa las vistas cy_* de graph/, que fijan el grafo con
get_current_graph/1.

Sirve para pruebas rápidas y para mostrar que el engine maneja más de un grafo.

Alejandro Miranda
Sebastián González (documentación)
P2
*/

% multifile: cada archivo de data/ aporta cláusulas a los mismos predicados;
% sin esta declaración, cargar un segundo grafo borraría los hechos del primero.
% dynamic: CREATE, SET, REMOVE y DELETE agregan y quitan estos hechos en ejecución.
:- multifile((graph/1, graph_desc/2, node/2, node_label/3, node_prop/4, edge/4, edge_type/3, edge_prop/4)).
:- dynamic((node/2, node_label/3, node_prop/4, edge/4, edge_type/3, edge_prop/4)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Grafo %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   graph(?Graph) is nondet.
%
%   Identifica los grafos disponibles.
%
%   Graph  Nombre del grafo.
graph(amigos).

%   graph_desc(?Graph, ?Description) is nondet.
%
%   Guarda la descripción de cada grafo; la muestra el comando .use.
%
%   Graph        Nombre del grafo.
%   Description  Texto descriptivo.
graph_desc(amigos, 'Red de amigos').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   node(?Graph, ?Id_node) is nondet.
%
%   Identifica los nodos que pertenecen al grafo.
%
%   Graph    Nombre del grafo.
%   Id_node  Identificador único del nodo dentro del grafo.
node(amigos, ana).
node(amigos, luis).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Etiquetas de nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   node_label(?Graph, ?Id_node, ?Label) is nondet.
%
%   Asocia cada nodo con sus etiquetas (labels); un nodo puede tener varias.
%
%   Graph    Nombre del grafo.
%   Id_node  Identificador del nodo.
%   Label    Etiqueta, por ejemplo 'Persona'.
node_label(amigos, ana, 'Persona').
node_label(amigos, luis, 'Persona').
node_label(amigos, ana, 'Person').
node_label(amigos, luis, 'Person').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Propiedades de nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   node_prop(?Graph, ?Id_node, ?Key, ?Value) is nondet.
%
%   Almacena las propiedades de cada nodo; una propiedad ausente se lee como null.
%
%   Graph    Nombre del grafo.
%   Id_node  Identificador del nodo.
%   Key      Nombre de la propiedad.
%   Value    Valor: átomo, número o booleano.
node_prop(amigos, ana, nombre, 'Ana').
node_prop(amigos, ana, edad, 28).
node_prop(amigos, luis, nombre, 'Luis').
node_prop(amigos, luis, edad, 31).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Relaciones (edges) %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   edge(?Graph, ?Id_edge, ?From, ?To) is nondet.
%
%   Identifica las relaciones y sus nodos de origen y destino; toda relación tiene dirección.
%
%   Graph    Nombre del grafo.
%   Id_edge  Identificador único de la relación dentro del grafo.
%   From     Nodo de origen.
%   To       Nodo de destino.
edge(amigos, e1, ana, luis).
edge(amigos, e2, ana, luis).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Tipos de relaciones %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   edge_type(?Graph, ?Id_edge, ?Type) is nondet.
%
%   Asocia cada relación con su tipo; cada relación tiene uno solo.
%
%   Graph    Nombre del grafo.
%   Id_edge  Identificador de la relación.
%   Type     Tipo, por ejemplo 'CONOCE'.
edge_type(amigos, e1, 'AMIGO_DE').
edge_type(amigos, e2, 'CONOCE').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Propiedades de relaciones %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   edge_prop(?Graph, ?Id_edge, ?Key, ?Value) is nondet.
%
%   Almacena las propiedades de cada relación; una propiedad ausente se lee como null.
%
%   Graph    Nombre del grafo.
%   Id_edge  Identificador de la relación.
%   Key      Nombre de la propiedad.
%   Value    Valor: átomo o número.
edge_prop(amigos, e1, desde, 2015).
edge_prop(amigos, e2, desde, 2015).
