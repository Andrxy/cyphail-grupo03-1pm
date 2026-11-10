:- encoding(utf8).

/* Datos del grafo ingenieria

Grafo principal de pruebas: personas, cursos y empresas de ingeniería, con relaciones CONOCE, TRABAJA_EN y SOCIO_DE.

Cada hecho lleva el nombre del grafo como primer argumento; así varios grafos
conviven en el mismo engine. El código generado no consulta estos hechos
directamente: usa las vistas cy_* de graph/, que fijan el grafo con
get_current_graph/1.

Usa los nombres de los casos de prueba del profesor (Persona, Empresa, CONOCE, TRABAJA_EN, SOCIO_DE, nombre, desde, cargo), de modo que esos casos se pueden correr tal cual.

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
graph(ingenieria).

%   graph_desc(?Graph, ?Description) is nondet.
%
%   Guarda la descripción de cada grafo; la muestra el comando .use.
%
%   Graph        Nombre del grafo.
%   Description  Texto descriptivo.
graph_desc(ingenieria, 'Personas, cursos y empresas de ingeniería').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   node(?Graph, ?Id_node) is nondet.
%
%   Identifica los nodos que pertenecen al grafo.
%
%   Graph    Nombre del grafo.
%   Id_node  Identificador único del nodo dentro del grafo.
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

%   node_label(?Graph, ?Id_node, ?Label) is nondet.
%
%   Asocia cada nodo con sus etiquetas (labels); un nodo puede tener varias.
%
%   Graph    Nombre del grafo.
%   Id_node  Identificador del nodo.
%   Label    Etiqueta, por ejemplo 'Persona'.
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

node_label(ingenieria, microsoft, 'USA').
node_label(ingenieria, hp, 'Costa Rica').
node_label(ingenieria, intel, 'Costa Rica').
node_label(ingenieria, universidad_nacional, 'Costa Rica').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Propiedades de nodos %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   node_prop(?Graph, ?Id_node, ?Key, ?Value) is nondet.
%
%   Almacena las propiedades de cada nodo; una propiedad ausente se lee como null.
%
%   Graph    Nombre del grafo.
%   Id_node  Identificador del nodo.
%   Key      Nombre de la propiedad.
%   Value    Valor: átomo, número o booleano.
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

%   edge(?Graph, ?Id_edge, ?From, ?To) is nondet.
%
%   Identifica las relaciones y sus nodos de origen y destino; toda relación tiene dirección.
%
%   Graph    Nombre del grafo.
%   Id_edge  Identificador único de la relación dentro del grafo.
%   From     Nodo de origen.
%   To       Nodo de destino.
edge(ingenieria, e1, alejandro, hp).
edge(ingenieria, e2, andrey, microsoft).
edge(ingenieria, e3, sebastian, acme_tech).
edge(ingenieria, e4, alejandro, andrey).
edge(ingenieria, e5, andrey, sebastian).
edge(ingenieria, e8, wilsinky, sebastian).
edge(ingenieria, e6, andrey, hp).
edge(ingenieria, e7, andrey, hp).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Tipos de relaciones %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   edge_type(?Graph, ?Id_edge, ?Type) is nondet.
%
%   Asocia cada relación con su tipo; cada relación tiene uno solo.
%
%   Graph    Nombre del grafo.
%   Id_edge  Identificador de la relación.
%   Type     Tipo, por ejemplo 'CONOCE'.
edge_type(ingenieria, e1, 'TRABAJA_EN').
edge_type(ingenieria, e2, 'TRABAJA_EN').
edge_type(ingenieria, e3, 'TRABAJA_EN').
edge_type(ingenieria, e4, 'CONOCE').
edge_type(ingenieria, e5, 'CONOCE').
edge_type(ingenieria, e8, 'CONOCE').
edge_type(ingenieria, e6, 'TRABAJA_EN').
edge_type(ingenieria, e7, 'SOCIO_DE').

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Propiedades de relaciones %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%   edge_prop(?Graph, ?Id_edge, ?Key, ?Value) is nondet.
%
%   Almacena las propiedades de cada relación; una propiedad ausente se lee como null.
%
%   Graph    Nombre del grafo.
%   Id_edge  Identificador de la relación.
%   Key      Nombre de la propiedad.
%   Value    Valor: átomo o número.
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
