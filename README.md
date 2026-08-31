# Cyphail

**Cyphail** is a prototype graph-management application: a compiler/transpiler for a custom graph query language, inspired by **Cypher** (Neo4j's query language), targeting a real execution engine in **SWI-Prolog**.

Project developed for the course **Paradigmas de Programación (EIF400-II-2026)**, Escuela de Ciencias de la Computación e Informática, Universidad Nacional de Costa Rica. Professor: **Carlos Loría-Sáenz**. Group 03, 1:00pm section.

> **This README describes the project's state during Sprint 1.1 only.** The full vision of Cyphail includes a real parser and a real Prolog execution engine — neither exists yet. See the [Project Status](#project-status) section for the exact scope of what is built today.

---

## Table of contents

- [Project Description](#project-description)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [How to Run It](#how-to-run-it)
- [Using the REPL](#using-the-repl)
- [Sprint 1.1 Submission Snapshot](#sprint-11-submission-snapshot)
- [Execution Log](#execution-log)
- [Project Status](#project-status)
- [AI Usage Declaration](#ai-usage-declaration)
- [AI Prompts Used in This Sprint](#ai-prompts-used-in-this-sprint)
- [References](#references)

---

## Project Description

Cyphail is a compiler/transpiler for a custom query language, inspired by Cypher, targeting the SWI-Prolog execution engine. The core idea: instead of reinventing a graph engine from scratch, Cyphail delegates traversal, unification, and backtracking logic to a mature logic-programming engine, and focuses on the language side — parsing Cypher-like syntax, generating the equivalent Prolog code, and presenting results in a readable way.

The project is designed as an exercise across the three major programming paradigms covered in the course (OOP, FP, LP), each solving the part of the problem where it fits best. This README documents specifically what was built for **Sprint 1.1**: the project setup and a REPL with faked commands and queries.

```cypher
MATCH (p:Persona) RETURN p.nombre, p.edad
MATCH (p1:Persona)-[r:AMIGO_DE]->(p2:Persona) RETURN p1.nombre AS Persona, type(r) AS Relacion, p2.nombre AS AmigoDe
```

---

## Architecture

Cyphail is a **multi-module** Maven project, with 4 independent modules:

```
cyphail-grupoXX-1pm/
│
├── common/      → Shared types across modules (query response contract; general-purpose
│                  module, anything shared by two or more modules belongs here)
│
├── broker/      → Bridge toward the execution engine (Prolog in the future, faked for now)
│
├── compiler/    → Compilation pipeline scaffold (tokenizer, parser, AST, semantic analysis, codegen)
│
└── cli/         → Command-line interface and interactive REPL
```

### Query flow (as it works in this sprint)

```
User (REPL) → Compiler.compile() → Broker.resolveQuery() → wired (fake) catalog
                                                                     │
User (REPL) ← result table ← Broker ← Compiler ← ────────────────────┘
```

In this sprint, `Broker` and `Compiler` do not talk to any external engine — they resolve against a fixed, wired catalog of sample graphs and queries. There is no real parser, no real AST, and no network connection. The flow between the pieces (`Repl` → `Compiler` → `Broker`) is real and has been implemented and tested; only the content of the response is faked.

---

## Prerequisites

Cyphail requires **JDK 26**. Maven does **not** need to be installed separately — the project ships with the **Maven Wrapper** (`mvnw.cmd` on Windows, `mvnw` on Linux), which downloads and uses the correct Maven version automatically.

### Windows 11

- [JDK 26](https://jdk.java.net/26/) installed, with `JAVA_HOME` configured and `java` available from the terminal.
- **PowerShell** (the build and run instructions below assume PowerShell, not CMD).
- Verify with:
  ```powershell
  java -version
  ```

### Linux

- JDK 26 installed (for example, via `sdkman`, your distribution's package manager, or a manual download from [jdk.java.net](https://jdk.java.net/26/)).
- Verify with:
  ```bash
  java -version
  ```

No IDE is required — the entire build and execution process happens from the terminal.

---

## How to Run It

### 1. Get the project

There are two ways to obtain the project. Either one works — pick whichever fits your situation.

**Option A — Clone with Git** (requires Git installed):

```powershell
git clone <repository-url>
cd cyphail-grupo03-1pm
```

**Option B — Download as a ZIP** (no Git needed):

1. On the repository page, click **Code → Download ZIP**.
2. Extract the downloaded `.zip` to wherever you want the project to live.
3. Open PowerShell and move into the extracted folder:

```powershell
cd <path-to-extracted-folder>
```

> **Note on the extracted folder name:** GitHub names the ZIP after the repository *and the branch*, so extracting it usually gives you a folder like `cyphail-grupoXX-1pm-main` rather than `cyphail-grupoXX-1pm`. Some tools also nest it one level deeper (a folder inside a folder of the same name). Use whatever name you actually see on disk.

**Either way, confirm you are standing in the project root before continuing.** The root is the folder that directly contains `pom.xml`, `mvnw.cmd` and `cyphail.bat`. Verify with:

```powershell
dir
```

You should see `pom.xml`, `mvnw.cmd`, `cyphail.bat`, and the four module folders (`common`, `broker`, `compiler`, `cli`). If you don't, you are one level too high or too low — move into the folder that does.

### 2. Build the project

From the project root, in PowerShell:

```powershell
.\mvnw.cmd clean package
```

This builds all 4 modules (`common`, `broker`, `compiler`, `cli`) in the correct order, runs the tests, and produces the executable jar at:

```
cli\target\cyphail.jar
```

If it ends with `BUILD SUCCESS`, the project is ready to run.

> To build without running the tests, add `-DskipTests`.

### 3. Add Cyphail to your PATH

So that the `cyphail` command works from any terminal, add the project root to your user `PATH`. Run this in PowerShell, **replacing `<project-root>` with the absolute path of the project folder on your machine** (for example `C:\Users\you\Cyphail\cyphail-grupo03-1pm`):

```powershell
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
[Environment]::SetEnvironmentVariable("PATH", "$currentPath;<project-root>", "User")
```

> If you are already standing in the project root, you can let PowerShell fill in the path for you by using `$PWD.Path` instead of typing it out:
>
> ```powershell
> $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
> [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$($PWD.Path)", "User")
> ```

What these two lines do:

- The first line reads the current value of your **user-level** `PATH` variable (the per-user one, not the system-wide one shared by all accounts) and stores it in `$currentPath`. Reading it first matters: if you skipped this and just assigned a new value, you would **erase every other entry** already in your `PATH`.
- The second line writes back that same value plus `;<project-root>` appended at the end. The semicolon is the separator Windows uses between `PATH` entries.

The result: Windows now also looks inside the project root when you type a command, so it finds `cyphail.bat` there and you can run `cyphail` from anywhere.

This change is **permanent** for your user account, but it only takes effect in terminals opened **after** running it. Close your current PowerShell window and open a new one.

> If you later move or rename the project folder, the `PATH` entry will point to a folder that no longer exists and `cyphail` will stop working. Re-run this step with the new path.

### 4. Verify the installation

```powershell
cyphail
```

If everything is set up, this prints Cyphail's command help.

### 5. Run the REPL

```powershell
cyphail repl
```

You should see Cyphail's welcome banner and the `cyphail>>>` prompt waiting for input.

### 6. Exit

Inside the REPL, type `.exit` to close the session.

---

## Using the REPL

Once inside the REPL, there are two kinds of input:

**Dot commands** (start with `.`):

| Command | What it does |
|---|---|
| `.help` | Shows the list of available commands |
| `.about` | Shows information about the project |
| `.use` | With no argument: lists available graphs. With a name (e.g. `.use Amigos`): selects that graph |
| `.exit` | Closes the REPL |

**Cypher-like queries** (any line that doesn't start with `.`):

```
cyphail>>> .use Amigos
OK. 'Amigos' graph available after 1ms
cyphail (Amigos) >>> MATCH (p:Persona) RETURN p.nombre, p.edad
p.nombre   p.edad
"Ana"      28
"Luis"     31
...
OK. Query resolved after 666 ms.
```

---

## Sprint 1.1 Submission Snapshot

The deliverable for this sprint is the last commit pushed **before Sunday, August 30, 2026 at midnight (Costa Rica time, UTC−6)**. The commands below let you inspect and check out exactly that commit.

### Show the last commit within the deadline

```powershell
git log -1 --before="2026-08-31T00:00:00-06:00" --date=iso-local --pretty=format:"%h  %ad  %an  %s"
```

This prints the short hash, author date, author, and message of the most recent commit made at or before the cutoff. The `--before` bound is written as `2026-08-31T00:00:00` because "midnight on August 30" means the *end* of that day — everything committed during August 30 is included; anything from August 31 onward is not. The explicit `-06:00` offset pins the cutoff to Costa Rica time regardless of the machine's local timezone.

To see a bit more context — for example the last five commits before the deadline — drop the `-1` and use `-5` instead.

### Check out that commit

```powershell
git checkout $(git rev-list -1 --before="2026-08-31T00:00:00-06:00" HEAD)
```

`git rev-list -1` resolves the same commit as above and returns only its hash, which is then handed to `git checkout`. Alternatively, copy the hash printed by the previous command and check it out directly:

```powershell
git checkout <commit-hash>
```

This leaves the repository in a **detached HEAD** state — you are viewing a snapshot rather than working on a branch, which is exactly what is wanted for reviewing a deliverable. To return to normal development afterwards:

```powershell
git checkout develop
```

> After checking out an older commit, re-run `.\mvnw.cmd clean package` before testing, so the jar matches the checked-out sources rather than a previous build.

---

## Execution Log

Every time a query goes through the compilation pipeline, Cyphail appends a trace of the simulated flow (tokenizer → parser → semantic analysis → code generation) to a file named `cyphail-flow.log`.

**The log is always written to the project root**, regardless of which directory the terminal session was started from. Since `cyphail` can be invoked from anywhere once it is on the `PATH`, resolving the log path relative to the current working directory would scatter log files across arbitrary folders. Instead, `FlowLog` asks the JVM for the physical location of its own compiled code (via `getProtectionDomain().getCodeSource()`) and walks up from there to the project root — so the destination depends on where the project lives on disk, not on where the user happened to be standing.

Example content of `cyphail-flow.log`:

```
[Tokenizer] Iniciando el proceso de tokenizacion...
[Tokenizer] Tokenizacion finalizada con exito.
[Parser] Iniciando el proceso de parseo...
[Parser] Parseo finalizado con exito.
[Analisis Semantico] Iniciando el proceso de analisis semantico...
[Analisis Semantico] Analisis semantico finalizado con exito.
[Generacion de codigo] Iniciando el proceso de generacion de codigo...
[Generacion de codigo] Generacion de codigo finalizada con exito.
```

---

## Project Status

**Current sprint: P1.1** — project setup and a REPL with faked commands.

What exists today:
- Multi-module Maven project structure, quasi-professional.
- Full interactive REPL, with all 4 dot commands (`.exit`, `.help`, `.about`, `.use`) working.
- Working `Repl → Compiler → Broker → Repl` flow, backed by a wired catalog of sample graphs and queries.
- At least 4 recognized `MATCH ... RETURN ...`-style queries, with table output.
- Executable jar plus a `cyphail.bat` launcher, runnable from any directory once added to `PATH`.
- Plain-text execution log of the simulated compilation pipeline, always written to the project root.

What does **not** exist yet (intentionally, as it is the scope of future sprints P1/P2):
- A real Cyphail parser (query recognition today is exact-text matching against a fixed catalog).
- A real connection to a Prolog engine.
- Real Prolog code generation.

---

## AI Usage Declaration

Per the project SPEC (Section "Uso de IA"): the use of agentic AI that generates the project with zero coding (e.g. Claude Code or similar) is prohibited and considered plagiarism. The use of AI to understand and study the project is authorized and encouraged, provided the team can defend any product of the group's work as its own. Authorized use may require showing the prompts used and the LLM model.

In line with this, **Claude (Anthropic)** was used during this sprint as a study, design-consultation, and documentation aid: understanding the SPEC and the assigned architecture, learning the libraries and tooling used (Picocli, Maven, JUnit, ascii-table), reviewing design decisions, diagnosing build and compilation errors, and drafting documentation including this README. No agentic AI tool was used to generate the project.

Where AI assistance contributed directly to a source file, that file carries an attribution note in its Javadoc header (see `FlowLog.java`). Every team member can individually explain and defend each part of the project as their own.

The prompts used are listed below, grouped by team member.

---

## AI Prompts Used in This Sprint

Summarized list of the consultations made to Claude during Sprint 1.1, grouped by team member. Duplicates and minor follow-ups have been omitted.

### Andrey Solano

1. Analyze this code and suggest how to refactor it toward the DRY principle and Knuth's principle (writing code with future maintainers in mind).
2. Request for three in-depth technical masterclasses covering: (a) Picocli internals — reflection, annotation processing, `CommandSpec`, the decorator pattern via annotations, exit-code handling and nested subcommands; (b) the Maven lifecycle and multi-module builds — `dependencyManagement` vs `dependencies`, transitive dependency resolution, fat-JARs/Shade, console-only reproducible builds, and `maven-javadoc-plugin`; (c) professional testing strategy with JUnit 5 — test lifecycle annotations, isolating CLI commands without triggering a real `System.exit()`, capturing `System.out`/`System.err`, and designing edge-case tests. Explicitly requested minimal illustrative snippets only, keeping a zero-coding rule for the actual project solution.
3. For a compiler project in a Programming Paradigms course, I need to build the CLI submodule with Picocli. Explain exactly how Picocli works — the annotation system, reflection, and code injection — so I can build it up step by step.
4. Search the web for professional CS-related projects on GitHub, to see how they handle submodules, READMEs, unit test cases, and Javadoc comments.

### Alejandro Miranda

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
19. How do I split a user-typed line into words in Java? Review of my dispatch method — is the order of the conditions correct?

### Wilson Umaña

1. How can I make a Picocli command optionally receive a parameter?
2. How do I build my project from the console?
3. Explain how Freva's ascii-table library works.
4. I need to produce a log simulating a system's flow — which Java libraries/tools can I use? How does `StringBuilder` work?
5. Generate the class that creates the log from a `StringBuilder`.
6. The log file is being created wherever the console was started, instead of in the project root. How do I make it always resolve to the project root?

### Sebastián González

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

- [microsoft/openCypherTranspiler](https://github.com/microsoft/openCypherTranspiler) — transpiles openCypher to T-SQL; direct reference for documenting a transpilation pipeline.
- [opencypher/front-end](https://github.com/opencypher/front-end) — reference implementation of a parser/AST/semantic analyzer for Cypher.
- [cleishm/libcypher-parser](https://github.com/cleishm/libcypher-parser) — cross-platform (Linux/Mac/BSD) Cypher parser.
- [remkop/picocli](https://github.com/remkop/picocli) — the CLI library used in this project.
- [munificent/craftinginterpreters](https://github.com/munificent/craftinginterpreters) — classic reference for documenting a course-level programming-languages project.
