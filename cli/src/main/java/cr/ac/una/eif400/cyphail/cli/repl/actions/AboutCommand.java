package cr.ac.una.eif400.cyphail.cli.repl.actions;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

/**
 * Comando ".about": muestra una breve descripcion de que es Cyphail y para
 * que sirve, sin salir de la sesion del REPL.
 *
 * @author Wilson Umaña
 * @author Andrey Solano
 * @version 1.0
 */

@Command(
        name = ".about",
        description = "Muestra informacion sobre cyphail"
)
public class AboutCommand implements Runnable {
    public void run() {
        IO.println("""
            cyphail
            --------------------------------------------------------
            A transpiler from a graph query language to SWI-Prolog,
            bundled with its own execution engine.
    
            Supports building, modifying, and persisting in-memory
            graphs (CREATE, SET, DELETE, SAVE, LOAD) as well as
            querying them via node and relationship pattern-matching
            (MATCH, WHERE, RETURN), executing queries directly on a
            live SWI-Prolog process.
    
            Course Project for EIF400 - Programming Paradigms
            School of Informatics, National University of Costa Rica (UNA)
    
            Group 03-1pm
            Developed by the geaniouses:
              - Andrey Jesus Solano Rojas
              - Sebastian David Gonzalez Masis
              - Alejandro Obed Miranda Gomez
              - Wilson Jose Umaña Rios
              
            --------------------------------------------------------
            """);
    }
}