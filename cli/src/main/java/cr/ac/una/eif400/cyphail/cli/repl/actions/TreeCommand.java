package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.cyphail.compiler.Compiler;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Comando {@code .tree}: muestra el árbol de sintaxis de una consulta, o el error encontrado.
 *
 * <p>El comando no analiza nada por su cuenta: le pasa la consulta al {@link Compiler}
 * y escribe lo que este devuelve.</p>
 *
 * @author Andrey Solano
 */
@Command(
        name = ".tree",
        description = "Shows the syntax tree of a query"
)
public class TreeCommand implements Runnable {

    /**
     * La consulta llega partida en palabras porque el REPL divide la línea por espacios
     * antes de entregarla a picocli.
     * No funciona de momento con queries multilínea
     */
    @Parameters(arity = "0..*", description = "Query to analyze")
    private String[] queryWords;

    /**
     * Implementación del {@code run()} del {@code Runnable}, recibe las consulta
     * y como es un "flujo alterno" se lo delega al {@code .tree()} del {@link Compiler}
     */
    @Override
    public void run() {
        String query = queryWords == null ? "" : String.join(" ", queryWords);
        IO.println(Compiler.getInstance().tree(query));
    }
}