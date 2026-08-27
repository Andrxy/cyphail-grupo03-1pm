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
 * @version 1.0
 */

@Command(
        name = ".use",
        description = "Lista los grafos disponibles o selecciona uno para trabajar"
)

public class UseCommand implements Callable<Integer>{

    private static final Map<String, String> grafos_disponibles =  Map.of(
            "Amigos", "Social Network",
            "Tasks", "Task and resources",
            "teams",   "Soccer Teams",
            "planets", "Planets in Solar System"
    );

    private void printGrafosDisponibles(){
        IO.println(String.format("%-10s %s", "Graph", "Description"));
        IO.println("-".repeat(20));

        grafos_disponibles.forEach((key, value) -> {
            IO.println(String.format("%-10s %s", key, value));
        });
    }

    private Integer conectarConGrafo(String graphName){
        if(!grafos_disponibles.containsKey(graphName)){
            IO.println("Error the graph is not in the list");
            return -1;
        }

        IO.println("OK " + graphName + " available");
        return 0;
    }

    @Parameters(index = "0", arity = "0..1", description = "Nombre del grafo a usar")
    private String graphName;

    @Override
    public Integer call() throws Exception {
        if(graphName == null){
            printGrafosDisponibles();
            return 0;
        }
        return conectarConGrafo(graphName);
    }
}
