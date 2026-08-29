package cr.ac.una.eif400.cyphail.cli.repl.actions;
import com.github.freva.asciitable.AsciiTable;
import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * Comando ".use": sin argumento lista en forma tabular los grafos
 * disponibles; con el nombre de un grafo lo selecciona para trabajar.
 *
 * @author Wilson Umaña - Grupo 03-1pm
 * @author Andrey Solano
 * @version 1.0
 */

@Command(
        name = ".use",
        description = "Lista los grafos disponibles o selecciona uno para trabajar"
)
public class UseCommand implements Runnable {

    @ParentCommand
    private Repl repl;

    @Parameters(index = "0", arity = "0..1", description = "Nombre del grafo a usar")
    private String graphName;

    @Override
    public void run() {
        Broker broker = repl.getBroker();

        if (graphName == null){
            String[][] graphs = broker.getGraphs();

            AsciiTable.get(header, graphs);
        }

        broker.setCurrentGraph(graphName);
    }
}
