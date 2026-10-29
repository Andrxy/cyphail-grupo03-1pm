package cr.ac.una.eif400.cyphail.cli.repl.actions;
import cr.ac.una.eif400.cyphail.cli.output.Formatter;
import cr.ac.una.eif400.cyphail.broker.Broker;
import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * Comando .use dentro del REPL.
 * Gestiona los grafos almacenados actualmente.
 * Tiene dos usos: si se invoca sin arggumentos se listan los grafos disponibles
 * en una tabla, pero, si se le pasa un nombre de algún grafo, valida su existencia
 * desde el broker y lo selecciona como grafo activo.
 *
 * <p>utiliza un parámetro opcional con arity 0..1 para soportar las dos formas de uso</p>
 *
 * <p>Implementa Runnable para cumplir con la interfaz requerida por Picocli, así,
 * cuando el usuario llame el comando desde el REPL se invoca su declaración del método run().</p>
 *
 * @author Wilson Umaña
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @version P1.1
 */
@Command(
        name = ".use",
        description = "Lists available graphs or selects one to work."
)
public class UseCommand implements Runnable {
    @ParentCommand
    private Repl repl;

    @Parameters(index = "0", arity = "0..1", description = "Nombre del grafo a usar")
    private String selectedGraphName;

    /**
     * Ejecuta la acción del comando .use, evalùa si se proporcionó un nombre de grafo.
     * De ser así, valida e intenta establecerlo como activo, sino,
     * muestra la tabla con los grafos disponibles
     */
    @Override
    public void run() {
        Broker broker = repl.getBroker();

        if (selectedGraphName == null){
            String[][] availableGraphs = broker.getAvailableGraphs();
            String[] graphsHeaders = broker.getGraphHeaders();
            
            IO.println(Formatter.format(graphsHeaders, availableGraphs));
            IO.println("\nOK. Query available after 5 ms.");
        } else if (broker.graphExists(selectedGraphName)) {
            IO.println("OK. '" + selectedGraphName + "' graph available after 1ms");
            broker.setCurrentGraph(selectedGraphName);
        } else {
            IO.println("Error. '" + selectedGraphName + "' graph is not available. Type \".use\" to list the available graphs.");
        }
    }
}
