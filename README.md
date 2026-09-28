# Cyphail

Transpiler for a graph query language inspired by **Cypher**, targeting **SWI-Prolog** as its execution engine.

Course project for **Paradigmas de Programación (EIF400-II-2026)**, Escuela de Informática, Universidad Nacional de Costa Rica. Professor: **Carlos Loría-Sáenz**. Group 03, 1:00pm.

Team: Andrey Solano, Sebastián González, Alejandro Miranda, Wilson Umaña.

---

## Contents

- [What it does](#what-it-does)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Build and run](#build-and-run)
- [REPL commands](#repl-commands)
- [Language scope](#language-scope)
- [Test cases](#test-cases)
- [What is missing](#what-is-missing)
- [AI usage declaration](#ai-usage-declaration)
- [References](#references)

---

## What it does

Cyphail compiles a Cypher-like query into an abstract syntax tree, checks it, and resolves it against a graph.

```cypher
MATCH (m:Movie), (p:Person)
WHERE m.year > 2000
RETURN m.title AS title, p.name AS actor
```

The compiler runs three stages and stops at the first one that fails:

| Stage | Answers | Example error |
|---|---|---|
| Lexical | Are the words valid? | `MATCH (a) @ RETURN a` |
| Syntactic | Is the order valid? | `MATCH (m:Movie RETURN m.title` |
| Semantic | Does it make sense? | `WHERE q.age > 60` where `q` was never declared |

Only if the three pass does the query reach the broker.

In Sprint P1 the broker answers from JSON files instead of Prolog. Replacing it is the scope of later sprints; nothing else changes.

---

## Architecture

Four Maven modules. Dependencies run one way, with no cycles:

```
cli  ->  compiler  ->  broker  ->  common
```

| Module | Responsibility |
|---|---|
| `common` | `QueryResponse`, the contract shared by broker and CLI |
| `broker` | Bridge to the data. Reads `broker/data/*.json` with Gson |
| `compiler` | Lexer, parser, AST, semantic analyzer |
| `cli` | Picocli commands and the interactive REPL |

`Compiler` is the only class that knows the compiler stages. The CLI asks it for results and never calls the tokenizer, the grammar or the analyzer on its own.

---

## Requirements

**JDK 26.** Maven is not needed: the project ships the Maven Wrapper.

Verify with:

```
java -version
```

No IDE is required.

---

## Build and run

### 1. Get the project

Clone it, or download the ZIP and extract it. Then move into the folder that contains `pom.xml` and `cyphail.bat`:

```
cd cyphail-grupo03-1pm
```

> Extracting a GitHub ZIP usually produces a folder named after the branch, like `cyphail-grupo03-1pm-main`. Use the name you see on disk.

### 2. Build

| Terminal | Command |
|---|---|
| CMD | `mvnw.cmd clean package` |
| PowerShell | `.\mvnw.cmd clean package` |
| Linux | `./mvnw clean package` |

Builds the four modules, runs the tests, and produces `cli\target\cyphail.jar`. It ends with `BUILD SUCCESS`.

To skip the tests, add `-DskipTests`.

### 3. Run

**From the project root**, the same folder where you built:

| Terminal | Command |
|---|---|
| CMD | `cyphail repl` |
| PowerShell | `.\cyphail repl` |


---

## REPL commands

| Command | What it does |
|---|---|
| `.help` | Lists the available commands |
| `.about` | Shows project information |
| `.use` | With no argument lists the graphs; with a name selects one |
| `.tree` | Compiles a query and shows its syntax tree, or the error found |
| `.exit` | Closes the REPL |

Any line that does not start with `.` is treated as a query.

```
cyphail>>> .use Amigos
OK. 'Amigos' graph available after 1ms
cyphail (Amigos) >>> MATCH (p:Persona) RETURN p.nombre, p.edad
```

The graphs and the query answers are read from `broker/data/graphJ.json` and `broker/data/queries.json`. Editing those files needs no recompilation.

---

## Language scope

Sprint P1 implements exactly the subset defined by the course test cases:

- Node patterns only, no relationship patterns.
- One or more node patterns per `MATCH`, separated by commas.
- Comparisons with `<`, `>` and `<>` between simple expressions and literals.
- No nested `MATCH`.
- Every query starts with `MATCH`.

The resulting grammar:

```
query          : matchClause+ updatingClause* returnClause? EOF
matchClause    : MATCH pattern whereClause?
whereClause    : WHERE expression
updatingClause : createClause | deleteClause
createClause   : CREATE pattern
deleteClause   : DELETE expression ("," expression)*
returnClause   : RETURN projectionItem ("," projectionItem)*
projectionItem : expression (AS variable)?
pattern        : nodePattern ("," nodePattern)*
nodePattern    : "(" variable? nodeLabels? mapLiteral? ")"
nodeLabels     : (":" name)+
mapLiteral     : "{" property ("," property)* "}"
property       : name ":" expression
expression     : comparison | simple
simple         : propertyAccess | literal | variable
propertyAccess : variable "." name
```

`name` accepts keywords as well as identifiers, because in `(o:Order)` the word `Order` is a label even though `ORDER` is a keyword elsewhere. The lexer cannot tell them apart, since it does not know the position; the grammar can.

---

## AI usage declaration

The project SPEC prohibits agentic AI that generates the project with zero coding, and authorizes AI for study and consultation provided the team can defend any product of its work as its own. It also requires showing the prompts used and the model.

**Model used: Claude (Anthropic) and Gemini**

The team used it in two different ways, declared separately because they are not equivalent.

### Study, analysis and documentation

The larger part of the use. Understanding the SPEC and the assigned grammar, learning the libraries and tooling (Picocli, Maven, JUnit, ascii-table, Gson), reviewing design decisions, diagnosing build errors, auditing the code against the review criteria, and drafting documentation including this README. No code from these consultations entered the project.

### Assistance in implementation

### Prompts

#### Sprint P1

**Sebastián González**

1. Explanation of the fundamental concepts of the project: what a transpiler does, why compilation is split into lexical, syntactic and semantic stages, what parser combinators are, and why the lexer and the parser share the same machinery.
2. Analysis of the team's progress on the `develop` branch against the Sprint P1 review criteria, as a context summary after time away from the project.
3. Analysis of the code against the course grammar and the test cases specification, verified by compiling the modules and running the lexer on the eleven cases.
4. Consultation on how to express the test-case grammar with the project's own combinators, and on how to resolve the ambiguity of `Order` being both a keyword and a label.
5. Consultation on where the compiler pipeline should live so that `.tree` and query execution analyze identically, and on how an invalid query should be prevented from reaching the broker.
6. Diagnosis of a lexical bug: a query ending in a space or a newline produced a false error.
7. Review of the project against the review criteria and the test cases, verified by compiling and running the pipeline.
8. Update of this README for Sprint P1.

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

1. Analyze this code and suggest how to refactor it toward the DRY principle and Knuth's principle.
2. Technical masterclasses on Picocli internals, the Maven lifecycle with multi-module builds, and testing strategy with JUnit 5, with minimal illustrative snippets only.
3. Explain how Picocli works (annotations, reflection, code injection) to build the CLI submodule step by step.
4. Search for professional projects on GitHub to see how they handle submodules, READMEs, test cases and Javadoc.

**Alejandro Miranda**

1. How to run and package a Java Maven project from the console: fat jars, `maven-shade-plugin`, manifest entries.
2. What the Maven Wrapper is and which files belong in version control.
3. How to write a `.bat` that runs a jar and finds it regardless of where it is called from.
4. Why a command works in CMD but not in PowerShell.
5. What Picocli is, what `@Command` does, and the difference between `Runnable` and `Callable`.
6. How to make `.help` build its listing automatically from the registered subcommands, and what `@Spec` and `CommandSpec` are.
7. How a subcommand reaches information from its parent command.
8. How to tell commands apart from queries in the REPL dispatch.
9. Adding unit tests for compiler, CLI, broker and common.

**Wilson Umaña**

1. How to make a Picocli command receive an optional parameter.
2. How Freva's ascii-table library works.
3. How `StringBuilder` works and which tools fit for producing an execution log.
4. How to resolve a file path to the project root instead of the working directory.

**Sebastián González**

1. Explanation of the project based on the SPEC, the research SPEC and the architecture diagram.
2. Clarification of the Sprint 1.1 requirements and evaluation criteria.
3. Consultation on the design of the `.exit` and `.help` REPL commands and how to integrate them without modifying teammates' files.
4. Analysis of the team branches after the migration to Picocli and a multi-module structure, identifying integration inconsistencies.
5. Design consultation on the `Repl -> Compiler -> Broker -> Repl` flow, the Singleton pattern and the sample query format.
6. Review of the sprint branch in preparation for the defense, against the review criteria.

---

## References

- [opencypher/front-end](https://github.com/opencypher/front-end): reference parser, AST and semantic analyzer for Cypher.
- [microsoft/openCypherTranspiler](https://github.com/microsoft/openCypherTranspiler): openCypher to T-SQL transpiler.
- [cleishm/libcypher-parser](https://github.com/cleishm/libcypher-parser): cross-platform Cypher parser.
- [remkop/picocli](https://github.com/remkop/picocli): CLI library used by the project.
- [google/gson](https://github.com/google/gson): JSON library used by the broker.
- [munificent/craftinginterpreters](https://github.com/munificent/craftinginterpreters): reference for documenting a languages project.
