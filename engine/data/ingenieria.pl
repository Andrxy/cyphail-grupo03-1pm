:- encoding(utf8).
:- multifile((graph/1, graph_desc/2, node/2, node_label/3, node_prop/4, edge/4, edge_type/3, edge_prop/4)).
:- dynamic((node/2, node_label/3, node_prop/4, edge/4, edge_type/3, edge_prop/4)).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Grafo %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Identifica los grafos disponibles.
graph(ingenieria).

% Guarda la descripción de cada grafo.
graph_desc(ingenieria, 'Personas, cursos y empresas de ingeniería').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Identifica los nodos que pertenecen al grafo.
node(ingenieria, alejandro).
node(ingenieria, andrey).
node(ingenieria, sebastian).
node(ingenieria, wilsinky).

node(ingenieria, paradigmas).
node(ingenieria, discretas).
node(ingenieria, bases_datos).
node(ingenieria, programacion).

node(ingenieria, microsoft).
node(ingenieria, hp).
node(ingenieria, intel).
node(ingenieria, universidad_nacional).
node(ingenieria, acme_tech).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Labels de nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Asocia cada nodo con sus etiquetas (labels).
node_label(ingenieria, alejandro, 'Persona').
node_label(ingenieria, andrey, 'Persona').
node_label(ingenieria, sebastian, 'Persona').
node_label(ingenieria, wilsinky, 'Persona').

node_label(ingenieria, alejandro, 'Person').
node_label(ingenieria, andrey, 'Person').
node_label(ingenieria, sebastian, 'Person').
node_label(ingenieria, wilsinky, 'Person').

node_label(ingenieria, alejandro, 'Hombre').
node_label(ingenieria, andrey, 'Hombre').
node_label(ingenieria, sebastian, 'Hombre').
node_label(ingenieria, wilsinky, 'Hombre').

node_label(ingenieria, paradigmas, 'Avanzada').
node_label(ingenieria, discretas, 'Intermedia').
node_label(ingenieria, bases_datos, 'Intermedia').
node_label(ingenieria, programacion, 'Principiante').

node_label(ingenieria, microsoft, 'Empresa').
node_label(ingenieria, hp, 'Empresa').
node_label(ingenieria, intel, 'Empresa').
node_label(ingenieria, universidad_nacional, 'Empresa').
node_label(ingenieria, acme_tech, 'Empresa').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Propiedades de nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Almacena las propiedades de cada nodo.
node_prop(ingenieria, alejandro, nombre, 'Alejandro').
node_prop(ingenieria, andrey, nombre, 'Andrey').
node_prop(ingenieria, sebastian, nombre, 'Sebastian').
node_prop(ingenieria, wilsinky, nombre, 'Wilson').
node_prop(ingenieria, alejandro, activo, true).
node_prop(ingenieria, andrey, edad, 31).

node_prop(ingenieria, paradigmas, profesor, 'Carlos Loria').
node_prop(ingenieria, paradigmas, dificultad, alta).
node_prop(ingenieria, discretas, profesor, 'Carlos Loria').
node_prop(ingenieria, bases_datos, profesor, 'Roger Leon').
node_prop(ingenieria, programacion, profesor, 'Oscar Benavides').

node_prop(ingenieria, microsoft, nombre, 'Microsoft inc').
node_prop(ingenieria, hp, nombre, 'Hewlett y Packard (HP)').
node_prop(ingenieria, intel, nombre, 'Intel inc').
node_prop(ingenieria, universidad_nacional, nombre, 'Universidad Nacional de Costa Rica').
node_prop(ingenieria, acme_tech, nombre, 'ACMETech').
node_prop(ingenieria, hp, pais, 'CR').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Relaciones (edges) %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Identifica las relaciones y sus nodos de origen y destino.
edge(ingenieria, e1, alejandro, hp).
edge(ingenieria, e2, andrey, microsoft).
edge(ingenieria, e3, sebastian, acme_tech).
edge(ingenieria, e4, alejandro, andrey).
edge(ingenieria, e5, andrey, sebastian).
edge(ingenieria, e6, andrey, hp).
edge(ingenieria, e7, andrey, hp).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Tipos de relaciones %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Asocia cada relación con su tipo.
edge_type(ingenieria, e1, 'TRABAJA_EN').
edge_type(ingenieria, e2, 'TRABAJA_EN').
edge_type(ingenieria, e3, 'TRABAJA_EN').
edge_type(ingenieria, e4, 'CONOCE').
edge_type(ingenieria, e5, 'CONOCE').
edge_type(ingenieria, e6, 'TRABAJA_EN').
edge_type(ingenieria, e7, 'SOCIO_DE').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Propiedades de relaciones %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Almacena las propiedades de cada relación.
edge_prop(ingenieria, e1, salario, 5000).
edge_prop(ingenieria, e2, salario, 6000).
edge_prop(ingenieria, e3, salario, 7000).
edge_prop(ingenieria, e6, salario, 6200).

edge_prop(ingenieria, e1, desde, 2021).
edge_prop(ingenieria, e2, desde, 2021).
edge_prop(ingenieria, e3, desde, 2022).
edge_prop(ingenieria, e4, desde, 2015).
edge_prop(ingenieria, e5, desde, 1999).
edge_prop(ingenieria, e6, desde, 2019).

edge_prop(ingenieria, e1, cargo, 'Dev').
edge_prop(ingenieria, e2, cargo, 'Desarrollador en Prolog').
edge_prop(ingenieria, e3, cargo, 'Administrador de bases de datos').
edge_prop(ingenieria, e6, cargo, 'Analista').
edge_prop(ingenieria, e7, porcentaje, 30.0).
