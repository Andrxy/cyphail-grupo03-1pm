package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.cyphail.compiler.ast.PrettyPrinter;
import cr.ac.una.eif400.cyphail.compiler.ast.specializations.Query;
import cr.ac.una.eif400.cyphail.compiler.parser.grammar.Grammar;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;
import cr.ac.una.eif400.cyphail.compiler.semantic.SemanticAnalyzer;
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
            var ast = Grammar.parse(value);

            if (ast instanceof Ok(var rest2, var valu2)) {
                List<String> reasons = SemanticAnalyzer.check(valu2);
                if(reasons.isEmpty()){
                    IO.println(PrettyPrinter.print(valu2));
                }
                else {
                    IO.println(reasons);
                }
            }
            else if (ast instanceof Fail(var reason)) {
                IO.print("Error de parseo: " + reason);
            }

        } else if (result instanceof Fail(var reason)) {
            IO.print("Error léxico: " + reason);
        }
    }
}