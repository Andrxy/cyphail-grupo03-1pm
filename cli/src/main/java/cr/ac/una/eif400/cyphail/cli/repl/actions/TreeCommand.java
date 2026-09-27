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
 * @author Grupo 03-1pm
 */
@Command(
        name = ".tree",
        description = "Shows the syntax tree of a query"
)
public class TreeCommand implements Runnable {

    /**
     * La consulta llega partida en palabras porque el REPL divide la línea por espacios
     * antes de entregarla a picocli.
     */
    @Parameters(arity = "0..*", description = "Query to analyze")
    @SuppressWarnings("unused") // Silencia la advertencia de IntelliJ ya que Picocli lo asigna vía reflexión
    private String[] queryWords;

    @Override
    public void run() {
        String query = queryWords == null ? "" : String.join(" ", queryWords);
        IO.println(Compiler.getInstance().tree(query));
    }
}