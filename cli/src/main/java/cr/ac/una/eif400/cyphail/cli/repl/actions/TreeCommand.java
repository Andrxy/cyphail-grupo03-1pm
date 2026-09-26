package cr.ac.una.eif400.cyphail.cli.repl.actions;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Comando {@code .tree}: tokeniza y parsea una consulta, y muestra su arbol de sintaxis
 * o el error encontrado.
 *
 * <p>El dibujo del arbol usa solo caracteres ASCII para que se vea bien en la consola CMD.</p>
 *
 * <p>El dibujo del arbol fue escrito por el Grupo 03-1pm con la asistencia de
 * Claude (Anthropic), segun lo declarado en el README.</p>
 */
@Command(
        name = ".tree",
        description = "Shows the syntax tree of a query"
)
public class TreeCommand implements Runnable {

    @Parameters(arity = "0..1", description = "Query to analyze")
    private String queryWords;

    @Override
    public void run() {
    }
}