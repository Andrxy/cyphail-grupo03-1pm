package cr.ac.una.eif400.cyphail.cli.commands;

import com.cyphail.cli.repl.commands.*;
import cr.ac.una.eif400.cyphail.cli.repl.ReplBanner;
import cr.ac.una.eif400.cyphail.cli.repl.commands.AboutCommand;
import cr.ac.una.eif400.cyphail.cli.repl.commands.ExitCommand;
import cr.ac.una.eif400.cyphail.cli.repl.commands.HelpCommand;
import cr.ac.una.eif400.cyphail.cli.repl.commands.UseCommand;
import cr.ac.una.eif400.cyphail.cli.repl.ReplSession;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * Implementación del subcomando repl encargado de cargar el shell
 *
 * @author Andrey Solano - Grupo 03-1pm
 * @version 1.1
 */

@Command(
        name = "repl",
        description = "Carga el REPL interactico de cyphail",
        subcommands = {
                UseCommand.class,
                AboutCommand.class,
                HelpCommand.class,
                ExitCommand.class
        }
)

public class ReplCommand implements Callable<Integer> {

    private final ReplSession session = new ReplSession();

    /**
     * @return sesion que mantiene el estado de este ciclo de lectura
     */
    public ReplSession session() {
        return session;
    }

    @Override
    public Integer call() {
        CommandLine dispatcher = new CommandLine(this);
        Scanner scanner = new Scanner(System.in);

        ReplBanner.print();

        while (session.isRunning()) {
            IO.print("cyphail> ");

            if (!scanner.hasNextLine()) {
                IO.println();
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            dispatcher.execute(line.split("\\s+"));
        }

        return 0;
    }
}
