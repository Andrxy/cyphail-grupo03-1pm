package cr.ac.una.eif400.cyphail.cli.repl.actions;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Comando ".help": muestra la lista de comandos punto disponibles en el REPL.
 * La lista se obtiene del modelo de picocli, leyendo los subcomandos
 * registrados en el comando padre y sus descripciones.
 *
 * @author Sebastian Gonzalez - Grupo 03-1pm
 * @author Andrey Solano - Grupo 03-1pm
 * @author Alejandro Miranda - Grupo 03-1pm
 * @version 1.1
 */

@Command(
        name = ".help",
        description = "Muestra la lista de comandos disponibles"
)

public class HelpCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        IO.println("Cyphail REPL - available commands:");
        IO.println();
        for (CommandLine subcommand : spec.parent().subcommands().values()) {
            CommandSpec command = subcommand.getCommandSpec();
            IO.println("  " + command.name() + " : " + String.join(" ", command.usageMessage().description()));
        }
        IO.println();
        IO.println("Type a command starting with \".\" or a Cyphail statement.");
    }
}
