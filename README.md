# Cyphail

## Build and run

Four steps: check the tools, get the project, build it, run it. All commands are for **CMD** (Windows Command Prompt).

### 1. Check you have the tools

| Tool | Version | Check with |
|---|---|---|
| JDK | 26 | `java -version` |
| SWI-Prolog | A recent release (the team uses 10), with `swipl` in the `PATH` | `swipl --version` |

Both commands must print a version. If one is not found, install it and open a **new** terminal.

Old SWI-Prolog 9.0.x releases do not include the `swipl mqi` command that the broker uses to start the engine, so they fail with ``file `mqi' does not exist``.

Maven is not needed: the project ships the Maven Wrapper (`mvnw.cmd`). No IDE is required.

### 2. Get the project

Clone it, or download the ZIP and extract it. Then move into the folder that contains `pom.xml` and `cyphail.bat`:

```
cd cyphail-grupo03-1pm
```

> Extracting a GitHub ZIP usually produces a folder named after the branch, like `cyphail-grupo03-1pm-main`. Use the name you see on disk.

### 3. Build (once)

```
mvnw.cmd clean package
```

It compiles the four modules, runs the tests and produces `cli\target\cyphail.jar`. Wait for the final `BUILD SUCCESS`.

> In a hurry? Add `-DskipTests` to skip the tests. The tests that need SWI-Prolog are skipped on their own when `swipl` is not installed.

### 4. Run

Always from the **project root**, the same folder where you built:

```
cyphail repl
```

The REPL opens. Try it:

```
cyphail>>> .use
cyphail>>> .use ingenieria
cyphail (ingenieria) >>> MATCH (p:Persona)-[r:TRABAJA_EN]->(e:Empresa)
     ...> RETURN p.nombre, r.cargo, e.nombre;
cyphail (ingenieria) >>> .load "examples/pruebas-repl.cyphail"
cyphail (ingenieria) >>> .exit
```

`.use` lists the graphs, `.use ingenieria` selects one, any line that does not start with `.` is a query (it may span several lines and ends with `;`), `.load` runs a script, and `.exit` leaves. The rest of the commands are in [REPL commands](#repl-commands).

### If something fails

| Message | Cause | Fix |
|---|---|---|
| `No se encontro ... cyphail.jar` | The project has not been built | Run step 3 |
| `Error: SWI-Prolog (swipl) could not be started` | `swipl` is not in the `PATH`, or it is too old to have `swipl mqi` | Install a recent SWI-Prolog, open a new terminal, check `swipl --version` |
| `release version 26 not supported` | JDK older than 26 | Install JDK 26 and check `java -version` |
| `.use ingenieria` says the graph is not available | Run from a different folder, so `engine\engine.pl` was not found | `cd` to the project root and run again |
| `Error: No graph selected` | A query was run before choosing a graph | Run `.use ingenieria` first |
| Accented letters show as `?` | The console is not in UTF-8 | Run `chcp 65001` before `cyphail repl` |

---

Transpiler for a graph query language inspired by **Cypher**, targeting **SWI-Prolog** as its execution engine.

Course project for **Paradigmas de Programación (EIF400-II-2026)**, Escuela de Informática, Universidad Nacional de Costa Rica. Professor: **Carlos Loría-Sáenz**. Group 03, 1:00pm.

Team: Andrey Solano, Sebastián González, Alejandro Miranda, Wilson Umaña.

Current state: **Sprint P2**.

---

## Contents

- [Build and run](#build-and-run)
- [What it does](#what-it-does)
- [Architecture](#architecture)
- [REPL commands](#repl-commands)
- [Graphs](#graphs)
- [Language scope](#language-scope)
- [Transpilation to Prolog](#transpilation-to-prolog)
- [The engine](#the-engine)
- [Test cases](#test-cases)
- [What is missing](#what-is-missing)
- [AI usage declaration](#ai-usage-declaration)
- [References](#references)

---

## What it does

Cyphail reads a Cypher-like query, checks it, **transpiles** it to a Prolog goal and sends that goal to SWI-Prolog, which runs it over a graph stored as Prolog facts. The answer comes back as a table.

```
cyphail (ingenieria) >>> MATCH (a:Persona)-[r:CONOCE]->(b:Persona)
     ...> WHERE r.desde > 2010 AND b.edad >= 30
     ...> RETURN a.nombre, b.nombre, r.desde;
+-----------+----------+---------+
| a.nombre  | b.nombre | r.desde |
+-----------+----------+---------+
| Alejandro |   Andrey |    2015 |
+-----------+----------+---------+

OK. Query resolved after 3 ms.
```

Java translates and carries the messages; Prolog finds the data. The compiler runs four stages and stops at the first one that fails, and every error says which stage found it:

| Stage | Answers | Example error |
|---|---|---|
| Lexical | Are the words valid? | `MATCH (__p) RETURN __p;` gives `Lexical Error: Unexpected character at index 7` |
| Syntactic | Is the order valid? | `MATCH (p:Persona RETURN p;` gives `Syntax error: ...` |
| Semantic | Does it make sense? | `MATCH (p:Persona) RETURN q;` gives `Semantic error: Undefined variable 'q' in RETURN` |
| Code generation | Can it be translated? | `RETURN DISTINCT ...` gives `Code generation error: Not supported yet: RETURN DISTINCT` |

Only a query that passes the four stages reaches the engine. Errors raised by the engine itself (for example `Error: No graph selected`) come back through the broker.

---

## Architecture

Four Maven modules plus the Prolog engine. Dependencies run one way, with no cycles:

```
cli  ->  compiler  ->  broker  ->  common
                          |
                          |  socket TCP, protocol MQI
                          v
                    engine (SWI-Prolog)
```

| Module | Responsibility |
|---|---|
| `common` | `QueryResponse`, the contract shared by broker and CLI (ok or error, headers, rows, message) |
| `compiler` | Lexer, parser, AST, semantic analyzer with symbol table, code generator (`CodeGen`) |
| `broker` | Starts SWI-Prolog, talks to it through MQI, parses the JSON answer with Gson |
| `cli` | Picocli commands and the interactive REPL (JLine) |
| `engine` | SWI-Prolog code: the graph data and the `cy_*` predicates that the generated code calls |

`Compiler` is the only class that knows the compiler stages. The CLI asks it for results and never calls the tokenizer, the grammar or the analyzer on its own.

### The path of a query

```
 query text
     |
 cli       Repl reads until ";" and sends the text to Compiler
     |
 compiler  1. Tokenizer         text -> tokens            (parser combinators over characters)
     |     2. Statements        script -> one Statement per ";"
     |     3. Grammar           tokens -> AST             (parser combinators over tokens)
     |     4. SemanticAnalyzer  AST -> AnalysisResult(ast, SymbolTable, diagnostics)
     |     5. CodeGen           AnalysisResult -> Prolog goal: cy_exec_query(...) or cy_exec_update(...)
     |
 broker    Broker prepends set_current_graph('<graph>') and wraps the goal in catch/3
     |     PrologEngine starts swipl once; MqiClient sends run((Goal), -1) through the socket
     |
 engine    findall/3 collects every solution; format_rows/3 turns them into the Rows text
     |     (updates run inside transaction/1)
     |
 broker    JSON -> MqiResponseParser -> PrologResult -> RowsParser -> QueryResponse
     |
 cli       Formatter (ascii-table) prints the table
```

### Broker and MQI

MQI (Machine Query Interface) is the SWI-Prolog library that lets another program send goals to Prolog through a TCP socket and receive the answers as JSON. The broker follows the design of the course demo `MQI_ResponseDemo`.

| Class | Responsibility |
|---|---|
| `Broker` | Singleton. Starts the engine on the first query, adds the current graph and the error guard, turns the result into a `QueryResponse` |
| `PrologEngine` | The `swipl` process: starts `swipl -l engine/engine.pl mqi --write_connection_values=true`, reads the port and password, connects, closes |
| `MqiClient` | The socket: authenticates, sends `run((Goal), -1)`, frames each message as `<bytes>.\n<message>.\n` in UTF-8 |
| `MqiResponseParser` | JSON to `PrologResult` (with Gson) |
| `PrologResult` | Sealed: `Success`, `Failure` or `PrologError` |
| `RowsParser` | The `Rows` text to headers and rows |

The engine starts once per session and is reused by every query. A shutdown hook closes the connection and stops `swipl` when the REPL ends.

---

## REPL commands

| Command | What it does |
|---|---|
| `.help` | Lists the available commands |
| `.about` | Shows project information |
| `.use` | With no argument lists the graphs; with a name selects one |
| `.tree` | Compiles a query and shows its syntax tree, or the error found |
| `.load "path/script.cyphail"` | Runs every statement of a script, shows each one before its result, and stops at the first error |
| `.clear` | Clears the screen |
| `.exit` | Closes the REPL |

Any line that does not start with `.` is a query. A query may span several lines and ends with `;`; the REPL shows `...>` while it waits for the rest. Several queries may go on one line. A `;` inside a string or a comment does not end a query. In a terminal, the arrow keys browse the history of previous queries.

---

## Graphs

The graphs live in `engine/data/`, one file per graph. Both are loaded when the engine starts.

| Graph | Content |
|---|---|
| `ingenieria` | People, courses and companies. Labels `Persona`, `Empresa`; relationships `CONOCE`, `TRABAJA_EN`, `SOCIO_DE`; properties such as `nombre`, `edad`, `desde`, `cargo`, `salario`. It uses the names of the course test cases, so those cases run as written |
| `amigos` | Small example: Ana and Luis, joined by `AMIGO_DE` and `CONOCE` |

Changes made with `CREATE`, `SET`, `REMOVE` and `DELETE` live in the memory of the engine while the REPL is open. They are not written back to the `.pl` files, so every session starts from the original data.

---

## Language scope

Sprint P2 adds relationship patterns and the updating clauses to the node patterns of P1.

### Supported, end to end

- `MATCH` with one or more patterns separated by commas. A pattern is a node, or node, relationship, node.
- Node patterns with variable, labels and properties: `(p:Persona {nombre: 'Andrey'})`.
- Relationship patterns in the three directions: `-[r:CONOCE]->`, `<-[r:CONOCE]-`, `-[r:CONOCE]-`, with types (`[:A|B]`) and properties (`[r {desde: 2015}]`).
- Repeating a node variable to join patterns: `(p)-[:TRABAJA_EN]->(e), (c)-[:TRABAJA_EN]->(e)`.
- `WHERE` with `=`, `<>`, `<`, `>`, `<=`, `>=`, `AND`, `OR`, `NOT`, `IS NULL`, `IS NOT NULL`, with `null` handled as in Cypher.
- `RETURN` with expressions, aliases (`AS`), `*`, arithmetic (`+ - * / %`) and `type(r)`. Returning a pattern variable returns only its id, as the P2 SPEC allows. `RETURN` may also appear alone: `RETURN 10 AS primero;`.
- `CREATE` of nodes and relationships, with or without a previous `MATCH`.
- `SET` of properties, of node labels, and `SET x.p = null` to remove a property.
- `REMOVE` of properties and node labels.
- `DELETE` of relationships and nodes (a node with relationships needs `DETACH DELETE`).
- Variable-length relationships such as `-[:CONOCE*1..2]->` (an extra beyond the P2 scope).

### Parsed but not translated yet

These are recognized by the grammar and shown by `.tree`, but code generation answers `Not supported yet`: `RETURN DISTINCT`, `ORDER BY`, `SKIP`, `LIMIT`, functions other than `type`, `SET x = {...}`, and a variable on a variable-length relationship.

### Semantic checks

| Rule | Message |
|---|---|
| Every variable used must be declared | `Undefined variable 'q' in RETURN` |
| A relationship variable cannot be repeated (P2 SPEC) | `Relationship variable 'r' cannot be repeated in the same query` |
| A variable cannot be both a node and a relationship (P2 SPEC) | `Variable 'a' cannot be used as both a node and a relationship` |
| The labels of a node variable are declared once | `Labels of node variable 'e' can only be declared once` |
| A created relationship has exactly one type, a direction and no length range | `A relationship created with CREATE must have a direction` |
| Labels can only be changed on nodes | `Labels can only be changed with SET on node variables, 'r' is a relationship` |

Variables cannot start with two underscores (`__`), as the P2 SPEC requires; the lexer rejects them. That prefix is reserved for the Prolog variables that `CodeGen` creates.

### Grammar

```
cyphail             : singlePartQuery EOF
singlePartQuery     : matchClause* returnClause
                    | matchClause* updatingClause+ returnClause?
matchClause         : "MATCH" pattern whereClause?
whereClause         : "WHERE" expression
updatingClause      : createClause | setClause | deleteClause | removeClause
createClause        : "CREATE" pattern
setClause           : "SET" setItem ("," setItem)*
setItem             : propertyExpression "=" expression
                    | variable "=" expression
                    | variable nodeLabels
deleteClause        : "DETACH"? "DELETE" expression ("," expression)*
removeClause        : "REMOVE" removeItem ("," removeItem)*
removeItem          : variable nodeLabels | propertyExpression
returnClause        : "RETURN" "DISTINCT"? projectionItems projectionModifiers
projectionItems     : "*" ("," projectionItem)* | projectionItem ("," projectionItem)*
projectionItem      : expression ("AS" variable)?
projectionModifiers : ("ORDER" "BY" sortItem ("," sortItem)*)? ("SKIP" expression)? ("LIMIT" expression)?
pattern             : patternElement ("," patternElement)*
patternElement      : nodePattern (relationshipPattern nodePattern)? | "(" patternElement ")"
nodePattern         : "(" variable? nodeLabels? properties? ")"
nodeLabels          : (":" labelName)+
relationshipPattern : "-" relationshipDetail? "->"
                    | "-" relationshipDetail? "-"
                    | "<-" relationshipDetail? "-"
relationshipDetail  : "[" variable? relTypes? relRange? properties? "]"
relTypes            : ":" relTypeName ("|" relTypeName)*
relRange            : "*" (integer (".." integer?)? | ".." integer)?
properties          : "{" (propertyKeyName ":" expression ("," propertyKeyName ":" expression)*)? "}"
expression          : orExpression
orExpression        : andExpression ("OR" andExpression)*
andExpression       : notExpression ("AND" notExpression)*
notExpression       : "NOT"* comparisonExpression
comparisonExpression: addOrSubtract (("=" | "<>" | "<" | ">" | "<=" | ">=") addOrSubtract)*
addOrSubtract       : multiplyDivideModulo (("+" | "-") multiplyDivideModulo)*
multiplyDivideModulo: unary (("*" | "/" | "%") unary)*
unary               : ("+" | "-")* nullTest
nullTest            : propertyOrLabels ("IS" "NOT"? "NULL")?
propertyOrLabels    : atom ("." propertyKeyName | nodeLabels)*
atom                : literal | "(" expression ")" | functionInvocation | variable
literal             : number | string | "TRUE" | "FALSE" | "NULL" | properties
```

Each rule is one method of `Grammar.java`, built with the project's parser combinators (`Sequence`, `OrFlat`, `Star`, `Plus`, `Optional`, `TFlat`, `Lazy`). Operator precedence comes from the order of the expression rules: the lower the rule, the tighter it binds. A pattern has at most one relationship; chained patterns such as `(a)-[]->(b)-[]->(c)` are outside the P2 scope and are written as two patterns that share `b`.

Keywords are case-insensitive. A label or property may still be a word that is a keyword elsewhere, as in `(o:Order)`.

---

## Transpilation to Prolog

`CodeGen` is a visitor over the AST. It turns each part of the query into a Prolog goal that calls a `cy_*` predicate of the engine, joins them with commas (the Prolog "and"), and wraps them in `cy_exec_query/4`, or in `cy_exec_update/4` when the query changes the graph.

Variables are renamed to Prolog variables: `a` becomes `__A`, and a property read `a.nombre` becomes `__Anombre`, read once. Relationships without a name get `__1`, `__2`, and so on. This symbol mapping, together with the `SymbolTable` built by the semantic analyzer, is the variable table required by the P2 SPEC.

```
MATCH (a:Persona)-[r:CONOCE]->(b:Persona)
WHERE r.desde > 2010 AND b.edad >= 30
RETURN a.nombre, b.nombre, r.desde
```

becomes

```prolog
cy_exec_query((cy_edge(__R, __A, __B), cy_edge_type(__R, 'CONOCE'),
   cy_node_label(__A, 'Persona'), cy_node_label(__B, 'Persona'),
   cy_edge_prop(__R, 'desde', __Rdesde), cy_node_prop(__B, 'edad', __Bedad),
   cy_where(and(cy_greater_than(__Rdesde, 2010), cy_greater_or_equal(__Bedad, 30))),
   cy_node_prop(__A, 'nombre', __Anombre), cy_node_prop(__B, 'nombre', __Bnombre)),
 ['a.nombre', 'b.nombre', 'r.desde'], [__Anombre, __Bnombre, __Rdesde], Rows)
```

| Cyphail | Prolog |
|---|---|
| `(p)` | `cy_node(__P)` |
| `(p:Persona)` | `cy_node(__P), cy_node_label(__P, 'Persona')` |
| `(p {nombre: 'Ana'})` | `cy_node_prop(__P, 'nombre', 'Ana')` |
| `(a)-[r]->(b)` | `cy_edge(__R, __A, __B)` |
| `(a)<-[r]-(b)` | `cy_edge(__R, __B, __A)` |
| `(a)-[r]-(b)` | `(cy_edge(__R, __A, __B) ; cy_edge(__R, __B, __A))` |
| `[r:CONOCE]` | `cy_edge_type(__R, 'CONOCE')` |
| two relationships in one `MATCH` | `cy_not_equal(__R, __S)` (a relationship is used once) |
| `WHERE` condition | `cy_where(and(...))`, `or(...)`, `not(...)`, `is_null(...)` |
| `p.edad + 1` | `cy_calculate(__Pedad + 1, __1)` |
| `CREATE (a:Persona {nombre: 'Marta'})` | `cy_create_node(['Persona'], ['nombre'-'Marta'], __A)` |
| `CREATE (a)-[:CONOCE]->(b)` | `cy_create_edge(__A, __B, 'CONOCE', [], __1)` |
| `SET a.edad = 40` | `cy_set_node_prop(__A, 'edad', 40)` |
| `REMOVE a:Temporal` | `cy_remove_node_label(__A, 'Temporal')` |
| `DELETE r` / `DETACH DELETE n` | `cy_delete_edge(__R)` / `cy_detach_delete_node(__N)` |

The P2 SPEC suggested creating a temporary rule with `cy_create_query`, running it and erasing it, and naming the predicates after the graph (`user_amigos_edge`). The team passes the goal directly to `cy_exec_query/4` instead: there are no temporary rules to create or clean up, and the graph is an argument, so adding a graph only means adding a data file.

---

## The engine

The engine is the SWI-Prolog code in `engine/`. The broker starts `swipl` with `engine/engine.pl`.

| Folder | Content |
|---|---|
| `engine.pl` | Entry point: `cy_exec_query/4`, `cy_exec_update/4`, `cy_graphs/1` |
| `data/` | The facts of each graph: `graph/1`, `node/2`, `node_label/3`, `node_prop/4`, `edge/4`, `edge_type/3`, `edge_prop/4`, all with the graph as first argument |
| `graph/` | The current graph (`set_current_graph/1`) and the `cy_*` views over it: nodes, relationships and paths |
| `utils/` | Comparisons, the three-valued `WHERE`, arithmetic with `null`, low-level mutations and the `Rows` format |

Key decisions:

- **Logic programming as the database.** The data are facts, a query is a goal, unification joins patterns that share a variable, and backtracking finds every answer. `findall/3` collects them into the `Rows` text, so MQI returns a single solution that already holds the whole table.
- **Current graph.** The `cy_*` predicates read the graph from `current_graph/1`, so `CodeGen` never needs the graph name; the broker prepends `set_current_graph/1`.
- **Atomic updates.** `cy_exec_update/4` runs inside `transaction/1`: if a goal fails with an error halfway, no change is kept.
- **Three-valued logic.** A missing property reads as `null`; comparisons with `null` give `null`, and only `true` passes a `WHERE`.
- **multifile and dynamic.** Every data file adds clauses to the same predicates (`multifile`), and the updating clauses change them at run time (`dynamic`).

Every `.pl` file is documented with PlDoc (the Javadoc of Prolog): a module header per file and, per predicate, its modes (`+` input, `-` output, `?` either), its determinism, its arguments and the errors it may raise. The notation is explained in the header of `engine.pl`.

---

## Test cases

- **Unit tests (JUnit 5)** for every module: lexer, combinators, grammar, semantic analyzer, code generator, broker (MQI client, response parser, `Rows` parser, engine start-up), CLI commands and REPL. `CasosPruebaP2Test` runs the ten P2 test cases from the course through the lexer, grammar, semantic analyzer and code generator. The tests that need SWI-Prolog are skipped when `swipl` is not installed.
- **Manual suite** in `examples/pruebas-repl.cyphail`: 24 commented cases with their expected results (multi-line queries, strings with `;`, comments, null, arithmetic, `CREATE`, `SET`, `REMOVE`, `DELETE`), followed by an intentional error that must stop `.load`. Run it with:

```
.use ingenieria
.load "examples/pruebas-repl.cyphail"
```

The mutations of the suite only touch nodes labelled `PruebaRepl`, and the suite cleans them at the start.

---

## What is missing

- `RETURN DISTINCT`, `ORDER BY`, `SKIP`, `LIMIT` and functions other than `type` are parsed but not translated.
- Chained patterns in one element, `(a)-[]->(b)-[]->(c)`, are outside the P2 scope; they give a syntax error.
- Returning a pattern variable returns only its id, not the whole node or relationship (P2 restriction).
- Changes to the graphs are not saved to disk.
- `.use` prints fixed times (`after 5 ms`, `after 1ms`); query times are measured.

---

## AI usage declaration

The project SPEC prohibits agentic AI that generates the project with zero coding, and authorizes AI for study and consultation provided the team can defend any product of its work as its own. It also requires showing the prompts used and the model.

**Model used: Claude (Anthropic) and Gemini**

The team used it in two different ways, declared separately because they are not equivalent.

### Study, analysis and documentation

The larger part of the use. Understanding the SPEC and the assigned grammar, learning the libraries and tooling (Picocli, Maven, JUnit, ascii-table, Gson, JLine, SWI-Prolog and its MQI), reviewing design decisions, diagnosing build errors, auditing the code against the review criteria, and drafting documentation including this README. No code from these consultations entered the project.

### Assistance in implementation

**Sprint P2, broker with MQI (Sebastián González).** The `broker.mqi` package and the rewrite of `Broker` follow the team's Plan #3 and the design of the course demo `MQI_ResponseDemo` (same types, protocol and process start-up). Claude (Anthropic) was used as a programming assistant to write them. Sebastián integrated them in four commits, ran them on Windows with SWI-Prolog 10, found the protocol failures there (`Invalid MQI message header`, a hanging test) that led to the fixes, verified each commit (blob ids, diff stats, full test suite, manual REPL test) and studied the code to defend it.

### Prompts

#### Sprint P1

**Sebastián González**

1. Explicación de los conceptos fundamentales del proyecto: qué hace un transpilador, por qué la compilación se divide en etapas léxica, sintáctica y semántica, qué son los combinadores de parsers y por qué el lexer y el parser comparten la misma maquinaria.
2. Análisis del avance del equipo en la rama `develop` frente a los criterios de revisión del Sprint P1, como resumen de contexto después de un tiempo fuera del proyecto.
3. Análisis del código frente a la gramática del curso y la especificación de los casos de prueba, verificado compilando los módulos y corriendo el lexer sobre los once casos.
4. Consulta sobre cómo expresar la gramática de los casos de prueba con los combinadores propios del proyecto, y sobre cómo resolver la ambigüedad de que `Order` sea a la vez palabra clave y etiqueta.
5. Consulta sobre dónde debe vivir el pipeline del compilador para que `.tree` y la ejecución de consultas analicen igual, y sobre cómo evitar que una consulta inválida llegue al broker.
6. Diagnóstico de un error léxico: una consulta que terminaba en un espacio o un salto de línea producía un error falso.
7. Revisión del proyecto frente a los criterios de revisión y los casos de prueba, verificada compilando y corriendo el pipeline.
8. Actualización de este README para el Sprint P1.

**Andrey Solano**
1. En base a la gramática, extraiga los tokens necesarios para armar un enumerador de TokenType
2. Explíqueme qué es una tabla de símbolos y cómo sabe ella el scope de las cosas para hacer revisiones semánticas
3. Deme una explicación de cómo funciona el patrón visitor y su doble despacho y cómo funciona con las interfaces selladas para efectos de un AST
4. Analice constructivamente mis combinadores, a la hora de extender la gramática a futuro, qué otro combinador se podría hacer mezclando los míos con el fin que el código sea DRY
5. De acuerdo a los siguientes Lexers que representan elementos del lenguaje cypher agrupados por "familias", por cada una cree un Map<String, TokenType>
6. Tengo private static String listify(List<AstNode> items) { ... } y no compila, deme sugerencias para ver qué puedo hacer
7. Cuál es la diferencia entre hacer <? extends AstNode> y <T extends AstNode>
8. A nivel de diseño, cuáles son los nodos más representativos para la consturcción del AST
9. Como solo tengo que revisar la definición de las variables, se puede de momento usar solo un Set?
10. Proporcióneme un ejemplo de cómo funciona una verificación semántica básica en base a un AST de un lenguaje

**Alejandro Miranda**
1. Qué hace reduce en Java y cómo se usa para construir un parser?
2. Qué significa el símbolo | dentro de una gramática tipo ANTLR?
3. Cómo se traduce una regla de gramática con alternativas (|) a código, es un combinador OR?
4. Cómo hace Picocli para bindear varios argumentos de la línea de comandos en un arreglo en vez de un solo String?
5. Cómo funciona el pattern matching (instanceof y switch) en Java?
6. Qué diferencia hay entre anotar un parámetro @Parameters como String[] o como String en Picocli?
7. Cuál es la diferencia entre un tipo genérico sin límite (<T>) y uno acotado (<T extends X>)?
8. Qué diferencia hay entre castear con (N) valor y con type.cast(valor)?


**Wilson Umaña**

1. Explicame como funciona el proceso de la grammar con el arbol AST y sugiereme como es adecuado manejarlo, segun estos casos de prueba.
2. Como paso de JSON a estructuras de java usando GSON
3. Me recomiendas usar records para guardar los datos del JSON?
4. En que parte de mi arquitectura me recomendarias guardar estos JSON y los records
5. Como puedo conseguir el path físico de mis archivos JSON
6. Modifica mi .bat para lograr eso
7. Que combinators me recomiendas usar aparte de los que tengo para facilitar el desarrollo de esta gramática
8. Que nodos nos recomiendas implementar en el AST segun este SPEC

****

#### Sprint P1.1

**Andrey Solano**

1. Analice este código y sugiera cómo refactorizarlo hacia el principio DRY y el principio de Knuth.
2. Clases técnicas sobre el funcionamiento interno de Picocli, el ciclo de vida de Maven con builds multimódulo y la estrategia de pruebas con JUnit 5, solo con fragmentos ilustrativos mínimos.
3. Explique cómo funciona Picocli (anotaciones, reflexión, inyección de código) para construir el submódulo CLI paso a paso.
4. Busque proyectos profesionales en GitHub para ver cómo manejan submódulos, READMEs, casos de prueba y Javadoc.

**Alejandro Miranda**

1. Cómo ejecutar y empaquetar un proyecto Java con Maven desde la consola: fat jars, `maven-shade-plugin`, entradas del manifest.
2. Qué es el Maven Wrapper y qué archivos van en el control de versiones.
3. Cómo escribir un `.bat` que ejecute un jar y lo encuentre sin importar desde dónde se llame.
4. Por qué un comando funciona en CMD pero no en PowerShell.
5. Qué es Picocli, qué hace `@Command` y cuál es la diferencia entre `Runnable` y `Callable`.
6. Cómo hacer que `.help` arme su listado automáticamente a partir de los subcomandos registrados, y qué son `@Spec` y `CommandSpec`.
7. Cómo un subcomando accede a información de su comando padre.
8. Cómo distinguir los comandos de las consultas en el despacho del REPL.
9. Agregar pruebas unitarias para compiler, CLI, broker y common.

**Wilson Umaña**

1. Cómo hacer que un comando de Picocli reciba un parámetro opcional.
2. Cómo funciona la librería ascii-table de Freva.
3. Cómo funciona `StringBuilder` y qué herramientas sirven para producir un log de ejecución.
4. Cómo resolver la ruta de un archivo respecto a la raíz del proyecto en vez del directorio de trabajo.

**Sebastián González**

1. Explicación del proyecto a partir del SPEC, el SPEC de la investigación y el diagrama de arquitectura.
2. Aclaración de los requisitos y los criterios de evaluación del Sprint 1.1.
3. Consulta sobre el diseño de los comandos `.exit` y `.help` del REPL y cómo integrarlos sin modificar archivos de los compañeros.
4. Análisis de las ramas del equipo después de la migración a Picocli y a una estructura multimódulo, identificando inconsistencias de integración.
5. Consulta de diseño sobre el flujo `Repl -> Compiler -> Broker -> Repl`, el patrón Singleton y el formato de las consultas de ejemplo.
6. Revisión de la rama del sprint en preparación para la defensa, frente a los criterios de revisión.

#### Sprint P2

**Sebastián González**

1. Resumen del proyecto: notas sintetizadas para tener una visión general del estado del proyecto (rama develop, SPEC del P2, revisión del P1).
2. Análisis de tareas: documentación de la implementación preliminar del Plan #3 (broker con MQI, basado en las notas del equipo) con pruebas.
3. Depuración multiplataforma: análisis del comportamiento propio de Windows para diagnosticar el error "Invalid MQI message header: 203" en PrologEngineTest.
4. Depuración multiplataforma: análisis de trazas para diagnosticar por qué PrologEngineTest se quedaba colgado después de la primera corrección.
5. Listas de verificación: generación de una lista para validar manualmente cada commit (blob ids, estadísticas del diff, suite completa, prueba en el REPL).
6. Material de inducción para el equipo: formato de la guía de estudio del proyecto, del módulo broker y de la documentación del equipo.
7. Análisis comparativo: comparación de las clases del broker con MQI_ResponseDemo para declarar con precisión dónde termina la referencia oficial.
8. Documentación del código: documentación de cada archivo .pl del engine con comentarios PlDoc (modos, determinismo, argumentos, errores), siguiendo el encabezado de módulo del demo del curso MQI_ResponseDemo y la notación de modos vista en clase, sin cambiar el código.

---

## References

- [opencypher/front-end](https://github.com/opencypher/front-end): reference parser, AST and semantic analyzer for Cypher.
- [microsoft/openCypherTranspiler](https://github.com/microsoft/openCypherTranspiler): openCypher to T-SQL transpiler.
- [cleishm/libcypher-parser](https://github.com/cleishm/libcypher-parser): cross-platform Cypher parser.
- [remkop/picocli](https://github.com/remkop/picocli): CLI library used by the project.
- [google/gson](https://github.com/google/gson): JSON library used by the broker.
- [SWI-Prolog MQI](https://www.swi-prolog.org/pldoc/package/mqi): Machine Query Interface, used by the broker to talk to the engine.
- [SWI-Prolog PlDoc](https://www.swi-prolog.org/pldoc/package/pldoc): documentation format used in the engine.
- [jline/jline3](https://github.com/jline/jline3): line editing and history in the REPL.
- [munificent/craftinginterpreters](https://github.com/munificent/craftinginterpreters): reference for documenting a languages project.
