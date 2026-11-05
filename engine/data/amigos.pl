:- encoding(utf8).
:- multifile((graph/1, graph_desc/2, node/2, node_label/3, node_prop/4, edge/4, edge_type/3, edge_prop/4)).
:- dynamic((node/2, node_label/3, node_prop/4, edge/4, edge_type/3, edge_prop/4)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Grafo %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Identifica los grafos disponibles.
graph(amigos).

% Guarda la descripción de cada grafo.
graph_desc(amigos, 'Red de amigos').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Identifica los nodos que pertenecen al grafo.
node(amigos, ana).
node(amigos, luis).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Etiquetas de nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Asocia cada nodo con sus etiquetas (edges).
node_label(amigos, ana, 'Persona').
node_label(amigos, luis, 'Persona').
node_label(amigos, ana, 'Person').
node_label(amigos, luis, 'Person').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Propiedades de nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Almacena las propiedades de cada nodo.
node_prop(amigos, ana, nombre, 'Ana').
node_prop(amigos, ana, edad, 28).
node_prop(amigos, luis, nombre, 'Luis').
node_prop(amigos, luis, edad, 31).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Relaciones (edges) %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Identifica las relaciones y sus nodos de origen y destino.
edge(amigos, e1, ana, luis).
edge(amigos, e2, ana, luis).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Tipos de relaciones %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Asocia cada relación con su tipo.
edge_type(amigos, e1, 'AMIGO_DE').
edge_type(amigos, e2, 'CONOCE').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Propiedades de relaciones %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Almacena las propiedades de cada relación.
edge_prop(amigos, e1, desde, 2015).
edge_prop(amigos, e2, desde, 2015).
