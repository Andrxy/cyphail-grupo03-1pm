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

### The combinator core

Lexer and parser are built on one functional interface:

```java
public interface IParser<I, T, R> {
    Result<I, T, R> parse(I source);
}
```

`I` is the input, `T` what it produces on success, `R` the failure reason. `Result` is a sealed type with two cases: `Ok(rest, value)` and `Fail(reason)`. The `rest` is what allows chaining: each parser returns what it did not consume, and the next one starts there.

A lexer reads text and produces tokens; a parser reads tokens and produces AST nodes. Both are `IParser`, so the combinators in `Combinators.java` (`Sequence`, `OrFlat`, `Star`, `Plus`, `Optional`, `TFlat`, `Lazy`) are written once and serve both stages.

No parser generator is used, as the project SPEC requires.

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
| CMD | `cyphail.bat repl` |
| PowerShell | `.\cyphail.bat repl` |

`cyphail.bat help` prints the command help.

The launcher must be run from the project root because it passes the project location to the program, which the broker needs to find its JSON files in `broker/data/`. Running the jar directly requires passing that location by hand:

```
java -Dcyphail.home=<project-root> -jar cli\target\cyphail.jar repl
```

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

## Test cases

The eleven course test cases live in `compiler/src/test/resources/casos/`:

| Case | Covers |
|---|---|
| C1 | One pattern, projection with an alias |
| C2 | `WHERE` with a comparison |
| C3 | Multiple labels on one node |
| C4 | Two disconnected patterns |
| C5 | Comparison between attributes of different nodes |
| C6 | Two patterns, each with its own property map |
| C7 | `MATCH` followed by `CREATE` |
| C8 | Property whose value is an expression (`{personId: p.id}`) |
| C9 | `MATCH`, `CREATE` and `DELETE` in one query |
| C10 | Undefined variable |
| C11 | Undefined variable caused by inverted declaration order |

C1 to C9 produce a syntax tree. C10 and C11 also produce their tree, followed by the semantic error: they are syntactically valid, and the problem is that they use a variable that was never declared.

---

## What is missing

Out of scope for P1, and therefore not implemented:

- **Relationship patterns** (`-[:REL]->`). The grammar rule is declared but returns `not implemented yet`.
- **`SET` and `REMOVE`.**
- **`AND`, `OR`, `NOT`, arithmetic, `IS NULL`.**
- **`DISTINCT`, `ORDER BY`, `SKIP`, `LIMIT`.**
- **Function calls** such as `type(r)`.
- **Prolog code generation and the MQI connection.** The broker still answers from JSON.

Known limitation: because the P1 grammar has no relationship patterns, the Sprint P1.1 sample query that used `-[:AMIGO_DE]->` no longer compiles. The other three P1.1 queries still work.

---

## AI usage declaration

The project SPEC prohibits agentic AI that generates the project with zero coding, and authorizes AI for study and consultation provided the team can defend any product of its work as its own. It also requires showing the prompts used and the model.

**Model used: Claude (Anthropic).**

The team used it in two different ways, declared separately because they are not equivalent.

### Study, analysis and documentation

The larger part of the use. Understanding the SPEC and the assigned grammar, learning the libraries and tooling (Picocli, Maven, JUnit, ascii-table, Gson), reviewing design decisions, diagnosing build errors, auditing the code against the review criteria, and drafting documentation including this README. No code from these consultations entered the project.

### Assistance in implementation

Three files in the current code were written with AI assistance and then reviewed, modified and integrated by the team:

| File | Extent |
|---|---|
| `compiler/Compiler.java` | The `analyze()` and `validate()` methods, which chain the three stages |
| `compiler/lexer/Tokenizer.java` | Initial version, later modified by the team |
| `cli/repl/actions/TreeCommand.java` | Initial version, later modified by the team |

Each of these files carries a note in its Javadoc marking the assistance and the prompt it came from.

Three further files were written with assistance and have since been **entirely replaced** by team implementations, so no assisted code from them remains: the first version of `Grammar.java` (rewritten by the team as `Grammar` plus `GrammarHelpers`), `AstPrinter.java` (replaced by `PrettyPrinter.java`), and a `Map` combinator (superseded by `TFlat`).

Everything else is the team's own work: the lexers, the token maps, the combinator library, the parser result and input types, the current grammar, the fifteen AST records, `AstBuilders`, `PrettyPrinter`, the `SemanticAnalyzer`, the broker and the rest of the CLI.

Every member can explain and defend each part of the project.

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
