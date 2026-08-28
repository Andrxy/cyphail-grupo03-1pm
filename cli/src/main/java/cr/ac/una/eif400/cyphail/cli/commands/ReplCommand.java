package cr.ac.una.eif400.cyphail.cli.commands;

import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import picocli.CommandLine.Command;

/**
 * Implementación del subcomando repl encargado de cargar el shell
 *
 * @author Andrey Solano - Grupo 03-1pm
 * @version 1.1
 */

@Command(
        name = "repl",
        description = "Carga el REPL interactico de cyphail"
)
public class ReplCommand implements Runnable {
    @Override
    public void run() {
        Repl repl = new Repl();
        repl.open();
    }
}
