package cr.ac.una.eif400.cyphail.cli.repl.actions;

import picocli.CommandLine.Command;

/**
 * Comando .about dentro del REPL.
 * Proporciona al usuario una descripción general del proyecto,
 * versión actual y equipo de trabajo
 *
 * <p>Implementa Runnable para cumplir con la interfaz requerida por Picocli, así, cuando
 * el usuario llame el comando desde el REPL se invoca su declaración del método run()</p>
 *
 * @author Wilson Umaña
 * @author Andrey Solano
 * @author Seb Gonzalez
 * @version P1
 */
@Command(
        name = ".about",
        description = "Shows a general description and details of the Cyphail project"
)
public class AboutCommand implements Runnable {

    /**
     * Ejecuta la acción del comando, imprimiendo en la consola
     * la información general, detalles académicos, versión y el equipo desarrollador
     * específicados en el SPEC del P1
     */
    public void run() {
        IO.println("""
            What is cyphail?
            It's a graph query transpiler inspired by the Cypher language, focused on graph management where users can select graphs and execute query statements
            
            It's still under development during Semester II, 2026 for EIF400: Paradigmas de Programación by Group 03-1pm under Professor Carlos Loría-Sáenz
            
            Current Version: P1 (As of September 27, 2026)
            Developed by the geniuses: Wilson Umaña, Andrey Solano, Sebastián González, and Alejandro Miranda
            """);
    }
}
