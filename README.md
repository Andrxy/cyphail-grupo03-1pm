# cyphail-grupo03-1pm

Transpilador de consultas de grafos inspirado en el lenguaje Cypher.
EIF400 Paradigmas de Programación — II Semestre 2026, Grupo 03-1pm. ECI/UNA.

## Build

Requiere **JDK 26 en el `PATH`** (`java -version` debe reportar `26.x`).
Maven no hace falta instalarlo: el wrapper lo descarga solo.

Desde la raíz del repositorio:

```
mvnw.cmd clean package
```

En Linux o macOS: `./mvnw clean package`

Genera el jar ejecutable en `cli\target\cyphail.jar`.

## Ejecución

Desde la raíz del repositorio, en `cmd`:

```
cyphail                              Muestra la ayuda inicial
cyphail help                         Lista los comandos disponibles
cyphail repl                         Abre la shell interactiva
cyphail compile archivo.cy -o salida Compila un archivo
cyphail run archivo.cy               Ejecuta un archivo
```

En PowerShell hay que anteponer `.\`, por ejemplo `.\cyphail.bat repl`.

Dentro del REPL: `.help`, `.about`, `.use`, `.exit` y consultas `MATCH`.
