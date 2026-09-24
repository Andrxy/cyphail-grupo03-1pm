# Cyphail

**Cyphail** is a prototype graph-management application: a compiler/transpiler for a custom graph query language, inspired by **Cypher** (Neo4j's query language), targeting a real execution engine in **SWI-Prolog**.

Project developed for the course **Paradigmas de Programación (EIF400-II-2026)**, Escuela de Ciencias de la Computación e Informática, Universidad Nacional de Costa Rica. Professor: **Carlos Loría-Sáenz**. Group 03, 1:00pm section.

> **This README describes the project's state during Sprint P1.** The full vision of Cyphail includes a Prolog execution engine, which is the scope of later sprints. See the [Project Status](#project-status) section for the exact scope of what is built today.

---

## Table of contents

- [Project Description](#project-description)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [How to Run It](#how-to-run-it)
- [Using the REPL](#using-the-repl)
- [Test Cases](#test-cases)
- [Locating a Sprint Submission](#locating-a-sprint-submission)
- [Project Status](#project-status)
- [AI Usage Declaration](#ai-usage-declaration)
- [AI Prompts Used](#ai-prompts-used)
- [References](#references)

---

## Project Description

Cyphail is a compiler/transpiler for a custom query language, inspired by Cypher, targeting the SWI-Prolog execution engine. The core idea: instead of reinventing a graph engine from scratch, Cyphail delegates traversal, unification, and backtracking logic to a mature logic-programming engine, and focuses on the language side: parsing Cypher-like syntax, generating the equivalent Prolog code, and presenting results in a readable way.

The project is designed as an exercise across the three major programming paradigms covered in the course (OOP, FP, LP), each solving the part of the problem where it fits best.

**Sprint P1** focuses on the front end of the compiler: a lexer and a parser built by hand with **parser combinators** (no parser generators such as ANTLR or JavaCC are used, as required by the project SPEC), and a broker that reads its data from JSON files instead of hardcoded values.

The scope of P1, as defined by the course test cases, covers node patterns only (no relationship patterns), one or more node patterns per `MATCH`, comparisons with `<`, `>` and `<>`, and queries that always start with `MATCH`:

```cypher
MATCH (m:Movie), (p:Person)
WHERE m.year > 2000
RETURN m.title AS title,
       p.name AS actor
```

---

## Architecture

Cyphail is a **multi-module** Maven project, with 4 independent modules:

```
cyphail-grupo03-1pm/
│
├── common/      → Shared types across modules (query response contract)
│
├── broker/      → Bridge toward the execution engine. In P1 it reads graphs and
│                  query answers from JSON files in broker/data/ (Prolog in later sprints)
│
├── compiler/    → Compilation pipeline: lexer and parser built with parser combinators,
│                  and the abstract syntax tree (AST)
│
└── cli/         → Command-line interface and interactive REPL
```

### The combinator core

Both the lexer and the parser are built on a single generic functional interface:

```java
public interface IParser<I, O> {
    Result<I, O> parse(I source);
}
```

A parser receives an input `I` and returns a `Result`, which is a sealed type with two cases: `Ok` (the value produced plus the remaining input) or `Fail` (the reason). The lexer is a parser from text to tokens; the parser is a parser from tokens to AST nodes. Small parsers are combined into larger ones with combinators such as `Or`, `Sequence`, `Plus`, `Star` and `Optional`.

### Query flow

```
User (REPL) → Compiler.compile() → Broker.resolveQuery() → JSON data (broker/data)
                                                                   │
User (REPL) ← result table ← Broker ← Compiler ← ──────────────────┘
```

---

## Prerequisites

Cyphail requires **JDK 26**. Maven does **not** need to be installed separately: the project ships with the **Maven Wrapper** (`mvnw.cmd` on Windows, `mvnw` on Linux), which downloads and uses the correct Maven version automatically.

### Windows 11

- [JDK 26](https://jdk.java.net/26/) installed, with `JAVA_HOME` configured and `java` available from the terminal.
- Either **CMD** or **PowerShell**. The instructions below show both where the syntax differs.
- Verify with:
  ```
  java -version
  ```

### Linux

- JDK 26 installed (for example, via `sdkman`, your distribution's package manager, or a manual download from [jdk.java.net](https://jdk.java.net/26/)).
- Verify with:
  ```bash
  java -version
  ```

No IDE is required. The entire build and execution process happens from the terminal.

---

## How to Run It

### 1. Get the project

**Option A: Clone with Git** (requires Git installed):

```
git clone <repository-url>
cd cyphail-grupo03-1pm
```

**Option B: Download as a ZIP** (no Git needed):

1. On the repository page, click **Code → Download ZIP**.
2. Extract the downloaded `.zip` to wherever you want the project to live.
3. Open a terminal and move into the extracted folder:

```
cd <path-to-extracted-folder>
```

> **Note on the extracted folder name:** GitHub names the ZIP after the repository *and the branch*, so extracting it usually gives you a folder like `cyphail-grupo03-1pm-main` rather than `cyphail-grupo03-1pm`. Use whatever name you actually see on disk.

**Either way, confirm you are standing in the project root before continuing.** The root is the folder that directly contains `pom.xml`, `mvnw.cmd` and `cyphail.bat`, plus the four module folders (`common`, `broker`, `compiler`, `cli`). Verify with:

```
dir
```

### 2. Build the project

From the project root:

| Terminal | Command |
|---|---|
| CMD | `mvnw.cmd clean package` |
| PowerShell | `.\mvnw.cmd clean package` |
| Linux | `./mvnw clean package` |

This builds all 4 modules in the correct order, runs the tests, and produces the executable jar at:

```
cli\target\cyphail.jar
```

If it ends with `BUILD SUCCESS`, the project is ready to run.

> To build without running the tests, add `-DskipTests`.

### 3. Run the REPL

From the project root, run the launcher:

| Terminal | Command |
|---|---|
| CMD | `cyphail.bat repl` |
| PowerShell | `.\cyphail.bat repl` |

You should see Cyphail's welcome banner and the `cyphail>>>` prompt waiting for input. Inside the REPL, type `.exit` to close the session.

Other launcher commands: `cyphail.bat help` shows the command help, and `cyphail.bat` with no arguments prints a short message pointing to the REPL.

#### Running the jar directly

The launcher is the recommended way to start Cyphail, because it tells the program where the project is located. The broker needs that location to find its JSON files in `broker/data/`. If you prefer to run the jar yourself, pass the project root explicitly with the `cyphail.home` property:

```
java -Dcyphail.home=<project-root> -jar cli\target\cyphail.jar repl
```

Without `-Dcyphail.home`, the broker looks for `data\` relative to the current directory, so the program fails to load its data when started from the project root.

### 4. Optional: call `cyphail` from any folder

This step is **not required**. It only lets you type `cyphail` instead of `cyphail.bat` from any directory, by adding the project root to your user `PATH`. In PowerShell, from the project root:

```powershell
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
[Environment]::SetEnvironmentVariable("PATH", "$currentPath;$($PWD.Path)", "User")
```

The first line reads your current user-level `PATH`; the second writes it back with the project root appended. Reading it first matters: assigning a new value directly would erase every other entry. The change only takes effect in terminals opened afterwards. If you later move or rename the project folder, repeat this step with the new path.

---

## Using the REPL

Once inside the REPL, there are two kinds of input.

**Dot commands** (start with `.`):

| Command | What it does |
|---|---|
| `.help` | Shows the list of available commands |
| `.about` | Shows information about the project |
| `.use` | With no argument: lists the available graphs. With a name (e.g. `.use Amigos`): selects that graph |
| `.tree` | Analyzes a query and shows the tokens recognized by the lexer, e.g. `.tree MATCH (m:Movie) RETURN m.title` |
| `.exit` | Closes the REPL |

**Cypher-like queries** (any line that does not start with `.`):

```
cyphail>>> .use Amigos
OK. 'Amigos' graph available after 1ms
cyphail (Amigos) >>> MATCH (p:Persona) RETURN p.nombre, p.edad
```

The graphs listed by `.use` and the answers to queries are read from `broker/data/graphJ.json` and `broker/data/queries.json`. To change the sample data, edit those files; no recompilation is needed.

---

## Test Cases

The eleven course test cases for Sprint P1 are stored in:

```
compiler/src/test/resources/casos/c01.cyphail ... c11.cyphail
```

| Case | What it covers |
|---|---|
| C1 | One pattern, projection with an alias |
| C2 | One pattern, `WHERE` with a comparison, projection with an alias |
| C3 | One pattern with multiple labels |
| C4 | Two disconnected patterns, projections with aliases |
| C5 | Two patterns, comparison between attributes of different nodes |
| C6 | Two patterns, each with its own property map |
| C7 | `MATCH` of one pattern followed by `CREATE` |
| C8 | Pattern whose property value is a simple expression (`{personId: p.id}`) |
| C9 | `MATCH`, `CREATE` and `DELETE` in the same query |
| C10 | Detection of an undefined variable |
| C11 | Undefined variable caused by inverted order (`p.id` used before `p` is declared) |

---

## Locating a Sprint Submission

Each sprint deliverable is the last commit pushed before that sprint's deadline. The commands below find and check out that commit, given the cutoff in Costa Rica time (UTC−6).

Show the last commit before a cutoff:

```
git log -1 --before="<cutoff>" --date=iso-local --pretty=format:"%h  %ad  %an  %s"
```

For example, the Sprint P1.1 deadline was midnight at the end of Sunday, August 30, 2026, which is written as:

```
git log -1 --before="2026-08-31T00:00:00-06:00" --date=iso-local --pretty=format:"%h  %ad  %an  %s"
```

The bound is written as `2026-08-31T00:00:00` because "midnight on August 30" means the end of that day. The explicit `-06:00` pins the cutoff to Costa Rica time regardless of the machine's timezone.

Check out that commit to review it, and return to development afterwards:

```
git checkout <commit-hash>
git checkout develop
```

> After checking out an older commit, rebuild with `mvnw.cmd clean package` before testing, so the jar matches the checked-out sources.

---

## Project Status

**Current sprint: P1.**

What exists today:

- Everything from Sprint P1.1: multi-module Maven project, interactive REPL, and the `.help`, `.about`, `.use` and `.exit` commands.
- **Data read from JSON, not hardcoded.** The graph catalog and the query answers are loaded with Gson from `broker/data/`.
- **A lexer built with parser combinators**, recognizing keywords, identifiers, integers, string literals, comparison and arithmetic operators, delimiters and relationship symbols, and skipping whitespace and comments.
- A generic combinator core (`IParser`, `Result`, `Ok`, `Fail`) shared by the lexer and the parser.
- The `.tree` command, which shows the tokens of a query.
- The eleven course test cases as source files.

In progress during this sprint:

- The parser that builds the AST from the token stream, following the course grammar.
- The symbol table, used to detect undefined variables (cases C10 and C11).
- `.tree` output showing the syntax tree.

Out of scope for P1 (later sprints):

- Relationship patterns.
- A real connection to a Prolog engine.
- Prolog code generation.

---

## AI Usage Declaration

Per the project SPEC (Section "Uso de IA"): the use of agentic AI that generates the project with zero coding (e.g. Claude Code or similar) is prohibited and considered plagiarism. The use of AI to understand and study the project is authorized and encouraged, provided the team can defend any product of the group's work as its own. Authorized use may require showing the prompts used and the LLM model.

In line with this, **Claude (Anthropic)** was used as a study, design-consultation, and documentation aid: understanding the SPEC and the assigned architecture, learning the libraries and tooling used (Picocli, Maven, JUnit, ascii-table, Gson), reviewing design decisions, diagnosing build and compilation errors, and drafting documentation including this README. No agentic AI tool was used to generate the project.

Where AI assistance contributes directly to a source file, that file carries an attribution note in its Javadoc header. Every team member can individually explain and defend each part of the project as their own.

The prompts used are listed below, grouped by sprint and team member.

---

## AI Prompts Used

Summarized list of the consultations made to Claude. Duplicates and minor follow-ups have been omitted.

### Sprint P1

#### Sebastián González

1. Analysis of the team's progress on the `develop` branch against the Sprint P1 Review Criteria Guide, as a context summary after time away from the project.
2. Preparation of a short briefing for a team meeting about the state of the compiler module.
3. Analysis of the current code against the course grammar and the P1 test cases specification, verified by compiling the modules and running the lexer on the eleven test cases.
4. Update of this README for Sprint P1.

### Sprint P1.1

#### Andrey Solano

1. Analyze this code and suggest how to refactor it toward the DRY principle and Knuth's principle (writing code with future maintainers in mind).
2. Request for three in-depth technical masterclasses covering: (a) Picocli internals: reflection, annotation processing, `CommandSpec`, the decorator pattern via annotations, exit-code handling and nested subcommands; (b) the Maven lifecycle and multi-module builds: `dependencyManagement` vs `dependencies`, transitive dependency resolution, fat-JARs/Shade, console-only reproducible builds, and `maven-javadoc-plugin`; (c) professional testing strategy with JUnit 5: test lifecycle annotations, isolating CLI commands without triggering a real `System.exit()`, capturing `System.out`/`System.err`, and designing edge-case tests. Explicitly requested minimal illustrative snippets only, keeping a zero-coding rule for the actual project solution.
3. For a compiler project in a Programming Paradigms course, I need to build the CLI submodule with Picocli. Explain exactly how Picocli works (the annotation system, reflection, and code injection) so I can build it up step by step.
4. Search the web for professional CS-related projects on GitHub, to see how they handle submodules, READMEs, unit test cases, and Javadoc comments.

#### Alejandro Miranda

1. How do I run a Java Maven project from the Windows console?
2. What options are there for packaging a Java project into something executable? What is a fat jar and what is it for?
3. I have a multi-module Maven project. Which module should hold the packaging configuration?
4. How do I configure `maven-shade-plugin` to generate a jar with all dependencies, and how do I tell the jar which class is the main one?
5. My jar was generated but says "no main manifest attribute". What's missing?
6. What is the Maven Wrapper, which files should be committed to Git, and is it a project dependency?
7. How do I write a `.bat` file that runs a Java jar, passes the arguments through to it, and finds the jar regardless of where it is called from?
8. How do I add a folder to the Windows `PATH` so I can call my program from anywhere? Can a `.bat` add itself to the `PATH`?
9. Why does my command work in CMD but not in PowerShell?
10. My README shows strange spaces between every letter. What happened?
11. How do I build without running the tests?
12. What is Picocli and what is it used for in a console application? What does the `@Command` annotation do?
13. What is the difference between implementing `Runnable` and `Callable` in a Picocli command?
14. I have a `.help` command that prints a hand-written list of commands. How do I make it build itself automatically? How do I get the list of registered subcommands from code?
15. What is `CommandSpec` in Picocli, and what is the `@Spec` annotation for?
16. How does a subcommand access information from its parent command?
17. How do I extract the name and description of each subcommand from Picocli's model? Why does `usageMessage().description()` return an array instead of a single string?
18. In my REPL, the user can type either commands or MATCH queries. How do I tell them apart? What does `getSubcommands()` return, and why use `keySet()` on it?
19. How do I split a user-typed line into words in Java? Review of my dispatch method: is the order of the conditions correct?
20. Adding unit tests for compiler, CLI, broker, and common.

#### Wilson Umaña

1. How can I make a Picocli command optionally receive a parameter?
2. How do I build my project from the console?
3. Explain how Freva's ascii-table library works.
4. I need to produce a log simulating a system's flow. Which Java libraries/tools can I use? How does `StringBuilder` work?
5. Generate the class that creates the log from a `StringBuilder`.
6. The log file is being created wherever the console was started, instead of in the project root. How do I make it always resolve to the project root?

#### Sebastián González

1. Explanation of the Cyphail project based on the SPEC, the research SPEC, the architecture diagram, and the transcript of the professor's Q&A session.
2. Clarification of the specific requirements of Sprint 1.1 (dates, SETUP checklist, evaluation criteria) based on the official course communication and the Review Criteria Guide.
3. Review of code already advanced by the team, and guidance on an isolated module to start collaborating on.
4. Consultation on the design of the `.exit` and `.help` REPL commands, and how to integrate them without modifying teammates' files.
5. Preparation of a defense outline explaining the team's own design decisions.
6. Analysis of different team branches after the migration to Picocli and to a multi-module structure, identifying integration inconsistencies.
7. Design consultation on the `Repl → Compiler → Broker → Repl` flow ("Fake Match"), including the Singleton pattern and the format of the sample queries.
8. Review of the final sprint branch in preparation for the defense: component-by-component analysis and a checklist against the Review Criteria Guide.
9. Drafting of this README, including research on reference repositories with exemplary documentation.

---

## References

Repositories used as reference for the structure of this README and of the project in general:

- [microsoft/openCypherTranspiler](https://github.com/microsoft/openCypherTranspiler): transpiles openCypher to T-SQL; direct reference for documenting a transpilation pipeline.
- [opencypher/front-end](https://github.com/opencypher/front-end): reference implementation of a parser/AST/semantic analyzer for Cypher.
- [cleishm/libcypher-parser](https://github.com/cleishm/libcypher-parser): cross-platform (Linux/Mac/BSD) Cypher parser.
- [remkop/picocli](https://github.com/remkop/picocli): the CLI library used in this project.
- [google/gson](https://github.com/google/gson): the JSON library used by the broker to read its data files.
- [munificent/craftinginterpreters](https://github.com/munificent/craftinginterpreters): classic reference for documenting a course-level programming-languages project.
