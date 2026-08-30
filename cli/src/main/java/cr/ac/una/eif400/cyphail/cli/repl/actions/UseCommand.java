package cr.ac.una.eif400.cyphail.cli.repl.actions;
import com.github.freva.asciitable.AsciiTable;
import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * Comando ".use": sin argumento lista en forma tabular los grafos
 * disponibles; con el nombre de un grafo lo selecciona para trabajar.
 *
 * @author Wilson Umaña - Grupo 03-1pm
 * @author Alejandro Miranda - Grupo 03-1pm
 * @author Andrey Solano - Grupo 03-1pm
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
    private String selectedGraphName;

    @Override
    public void run() {
        Broker broker = repl.getBroker();

        if (selectedGraphName == null){
            String[][] availableGraphs = broker.getAvailableGraphs();
            String[] graphsHeaders = broker.getGraphHeaders();
            
            IO.println(AsciiTable.getTable(graphsHeaders, availableGraphs));
            IO.println();
            IO.println("OK. Query available after 5 ms.");
        } else if (broker.graphExists(selectedGraphName)) {
            IO.println("OK. \"" + selectedGraphName + "\" graph available after 1ms");
            broker.setCurrentGraph(selectedGraphName);
        } else {
            IO.println("Error. \"" + selectedGraphName + "\" graph is not available. Type \".use\" to list the available graphs.");
        }
    }
}
