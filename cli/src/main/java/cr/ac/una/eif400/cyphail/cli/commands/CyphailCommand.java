package cr.ac.una.eif400.cyphail.cli.commands;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Comando raiz encargado
 *
 * @author Andrey Solano - Grupo 03-1pm
 * @version 1.0
 */
@Command(
        name = "cyphail",
        subcommands = {
                CompileCommand.class,
                RunCommand.class,
                CommandLine.HelpCommand.class,
                ReplCommand.class,
        },
        version = "1.0"
)
public class CyphailCommand implements Runnable {
    @Override
    public void run() {
        IO.println("El CLI de Cyphail ha iniciado correctamente");
    }
}
