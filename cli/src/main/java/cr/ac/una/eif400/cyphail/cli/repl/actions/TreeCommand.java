package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer.tokenize;

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

    @Parameters(arity = "0..*", description = "Query to analyze")
    private String[] queryWords;

    @Override
    public void run() {
        if (queryWords == null || queryWords.length == 0) {
            IO.println("No query provided");
            return;
        }

        String query = String.join(" ", queryWords);

        var result = tokenize(query);
        if (result instanceof Ok(var rest, var value)) {
            for (var v : value) {
                IO.println(v);
            }
        } else if (result instanceof Fail(var reason)) {
            IO.print("Error léxico: " + reason);
        }

    }
}