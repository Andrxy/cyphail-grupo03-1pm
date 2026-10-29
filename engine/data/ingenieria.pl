:- encoding(utf8).
:- multifile graph/1, graph_desc/2, node/2, node_label/3, node_prop/4.
:- multifile edge/4, edge_type/3, edge_prop/4.

% Identifica los grafos disponibles.
graph(ingenieria).

% Guarda la descripción de cada grafo.
graph_desc(ingenieria, 'Personas, cursos y empresas de ingeniería').

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

% Asocia cada nodo con sus etiquetas.
node_label(ingenieria, alejandro, 'Person').
node_label(ingenieria, andrey, 'Person').
node_label(ingenieria, sebastian, 'Person').
node_label(ingenieria, wilsinky, 'Person').

node_label(ingenieria, alejandro, 'Male').
node_label(ingenieria, andrey, 'Male').
node_label(ingenieria, sebastian, 'Male').
node_label(ingenieria, wilsinky, 'Male').

node_label(ingenieria, paradigmas, 'Avanzada').
node_label(ingenieria, discretas, 'Intermedia').
node_label(ingenieria, bases_datos, 'Intermedia').
node_label(ingenieria, programacion, 'Principiante').

node_label(ingenieria, microsoft, 'Company').
node_label(ingenieria, hp, 'Company').
node_label(ingenieria, intel, 'Company').
node_label(ingenieria, universidad_nacional, 'Company').
node_label(ingenieria, acme_tech, 'Company').

% Almacena las propiedades de cada nodo.
node_prop(ingenieria, alejandro, name, 'Alejandro').
node_prop(ingenieria, andrey, name, 'Andrey').
node_prop(ingenieria, sebastian, name, 'Sebastian').
node_prop(ingenieria, wilsinky, name, 'Wilson').

node_prop(ingenieria, paradigmas, profesor, 'Carlos Loria').
node_prop(ingenieria, paradigmas, dificultad, alta).
node_prop(ingenieria, discretas, profesor, 'Carlos Loria').
node_prop(ingenieria, bases_datos, profesor, 'Roger Leon').
node_prop(ingenieria, programacion, profesor, 'Oscar Benavides').

node_prop(ingenieria, microsoft, name, 'Microsoft inc').
node_prop(ingenieria, hp, name, 'Hewlett y Packard (HP)').
node_prop(ingenieria, intel, name, 'Intel inc').
node_prop(ingenieria, universidad_nacional, name, 'Universidad Nacional de Costa Rica').
node_prop(ingenieria, acme_tech, name, 'ACMETech').

% Identifica las relaciones y sus nodos de origen y destino.
edge(ingenieria, e1, alejandro, hp).
edge(ingenieria, e2, andrey, microsoft).
edge(ingenieria, e3, sebastian, acme_tech).
edge(ingenieria, e4, alejandro, andrey).
edge(ingenieria, e5, andrey, sebastian).

% Asocia cada relación con su tipo.
edge_type(ingenieria, e1, 'WORKS_AT').
edge_type(ingenieria, e2, 'WORKS_AT').
edge_type(ingenieria, e3, 'WORKS_AT').
edge_type(ingenieria, e4, 'KNOWS').
edge_type(ingenieria, e5, 'KNOWS').

% Almacena las propiedades de cada relación.
edge_prop(ingenieria, e1, salary, 5000).
edge_prop(ingenieria, e2, salary, 6000).
edge_prop(ingenieria, e3, salary, 7000).

edge_prop(ingenieria, e1, since, 2021).
edge_prop(ingenieria, e2, since, 2021).
edge_prop(ingenieria, e3, since, 2022).

edge_prop(ingenieria, e1, role, 'Developer Backend').
edge_prop(ingenieria, e2, role, 'Developer in Prolog').
edge_prop(ingenieria, e3, role, 'DBA').
