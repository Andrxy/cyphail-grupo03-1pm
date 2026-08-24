package com.cyphail.cli;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = ".use"
)
public class UseCommand implements Callable<Integer>{

    private static final Map<String, String> grafos_disponibles =  Map.of(

            "Amigos" , "Social Network",
            "Tasks", "Task and resources",
            "teams",   "Soccer Teams",
            "planets", "Planets in Solar System"

    );

    private void printGrafosDisponibles(){

        System.out.printf("%-10s %s%n", "Graph", "Description");
        System.out.println("-".repeat(20));

        grafos_disponibles.forEach((key, value) -> {
            System.out.printf("%-10s %s%n", key, value);
        });
    }

    private Integer conectarConGrafo(String graphName){

        if(!grafos_disponibles.containsKey(graphName)){
            System.out.println("Error the graph is not in the list");
            return -1;
        }

        System.out.println("OK " + graphName + "available");
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
