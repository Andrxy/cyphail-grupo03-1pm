package cr.ac.una.eif400.cyphail.cli.repl.actions;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Comando .help dentro del REPL.
 * Muestra la lista de acciones disponibles dentro del REPL.
 * Extrae la informaciòn dinámicamente del modelo de picocli,
 * leyendo los subcomandos registrados en el comando padre y sus descripciones
 *
 * <p>Implementa Runnable para cumplir con la interfaz requerida por Picocli, así,
 * cuando el usuario llame el comando desde el REPL se invoca su declaración del método run().
 * Además utiliza el CommandSpec para ver la lista de subdomandos</p>
 *
 * @author Sebastian Gonzalez
 * @author Andrey Solano
 * @author Alejandro Miranda
 * @version P1.1
 */
@Command(
        name = ".help",
        description = "Shows the list of available actions and usage instructions"
)
public class HelpCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    /**
     * Ejecuta el comando .help, imprimiendo en la consola el listado
     * de todos los subcomandos disponibles que obtiene consultando
     * el spec de su comando padre
     */
    @Override
    public void run() {
        IO.println("Cyphail REPL - available commands:\n");

        for (CommandLine subcommand : spec.parent().subcommands().values()) {
            CommandSpec command = subcommand.getCommandSpec();
            IO.println("  " + command.name() + " : " + String.join(" ", command.usageMessage().description()));
        }

        IO.println("\nType a command starting with '.' or a Cyphail statement");
    }
}
