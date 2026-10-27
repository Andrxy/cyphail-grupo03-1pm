:- encoding(utf8).
% Identifica los grafos disponibles.
graph(amigos).

% Guarda la descripción de cada grafo.
graph_desc(amigos, 'Red de amigos').

% Identifica los nodos que pertenecen al grafo.
node(amigos, ana).
node(amigos, luis).

% Asocia cada nodo con sus etiquetas.
node_label(amigos, ana, 'Persona').
node_label(amigos, luis, 'Persona').

% Almacena las propiedades de cada nodo.
node_prop(amigos, ana, nombre, 'Ana').
node_prop(amigos, ana, edad, 28).
node_prop(amigos, luis, nombre, 'Luis').
node_prop(amigos, luis, edad, 31).

% Identifica las relaciones y sus nodos de origen y destino.
edge(amigos, e1, ana, luis).

% Asocia cada relación con su tipo.
edge_label(amigos, e1, 'AMIGO_DE').

% Almacena las propiedades de cada relación.
edge_prop(amigos, e1, desde, 2015).
