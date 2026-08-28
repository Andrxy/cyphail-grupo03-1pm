package cr.ac.una.eif400.cyphail.cli.repl;

import cr.ac.una.eif400.cyphail.cli.repl.commands.AboutCommand;
import cr.ac.una.eif400.cyphail.cli.repl.commands.ExitCommand;
import cr.ac.una.eif400.cyphail.cli.repl.commands.HelpCommand;
import cr.ac.una.eif400.cyphail.cli.repl.commands.UseCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Scanner;

@Command(
        subcommands = {
                HelpCommand.class,
                AboutCommand.class,
                ExitCommand.class,
                UseCommand.class
        }
)
public class Repl {
    private final Session session = new Session();

    public void open() {
        CommandLine dispatcher = new CommandLine(this);
        Scanner scanner = new Scanner(System.in);

        IO.print("""
                Welcome to Cyphail-01-5pm v.0.1. August 2026. ESCINF/UNA EIF400-II-2026
                Visit www.whatiscyphail.com for more information
                Type ".help" for more information and commands
                Type ".exit" to quit
                """);

        while (session.isRunning()) {
            IO.print("cyphail>>> ");

            if (!scanner.hasNextLine()) {
                IO.println();
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            dispatcher.execute(line.split("\\s+"));
        }
    }
}
