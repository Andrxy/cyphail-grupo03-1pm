package cr.ac.una.eif400.cyphail.cli.repl.commands;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Map;
import java.util.concurrent.Callable;

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

    @Parameters(index = "0", arity = "0..1", description = "Nombre del grafo a usar")
    private String graphName;

    @Override
    public void run() {
        if(graphName == null){
            // printGrafosDisponibles();
        }
        // return conectarConGrafo(graphName);

    }
}
