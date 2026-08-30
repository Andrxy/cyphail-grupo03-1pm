package cr.ac.una.eif400.cyphail.cli.repl;

import cr.ac.una.eif400.cyphail.cli.output.Formatter;
import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.cli.repl.actions.AboutCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.ExitCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.HelpCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.UseCommand;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Scanner;
import java.util.Set;

@Command(
        subcommands = {
                HelpCommand.class,
                ExitCommand.class,
                AboutCommand.class,
                UseCommand.class
        }
)
public class Repl {
    private final Broker broker = Broker.getInstance();
    private final Compiler compiler = Compiler.getInstance();
    private boolean running = true;

    public Broker getBroker() {
        return broker;
    }

    public void open() {
        CommandLine dispatcher = new CommandLine(this);
        Set<String> actionNames = dispatcher.getSubcommands().keySet();
        Scanner scanner = new Scanner(System.in);

        printBanner();

        while (running) {
            String currentGraph = broker.getCurrentGraph();
            String prompt = currentGraph != null
                    ? "cyphail (" + currentGraph + ") >>> "
                    : "cyphail>>> ";
            IO.print(prompt);

            if (!scanner.hasNextLine()) {
                IO.println();
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] args = line.split("\\s+");
            String firstWord = args[0];

            if (actionNames.contains(firstWord)) {
                dispatcher.execute(args);
            } else if (firstWord.startsWith(".")) {
                IO.println("Error. Unknown command \"" + firstWord + "\". Type \".help\" for the available commands.");
            } else {
                executeQuery(line);
            }
        }
    }

    public void executeQuery(String sourceCode) {
        QueryResponse response = compiler.compile(sourceCode);

        if (response.isOk()) {
            IO.println(Formatter.format(response.getHeaders(), response.getRows()));
            IO.println();
        }

        IO.println(response.getMessage());
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    private void printBanner() {
        IO.print(
                """
                Welcome to Cyphail-03-1pm v.0.1. August 2026. ESCINF/UNA EIF400-II-2026
                Visit www.whatiscyphail.com for more information
                Type ".help" for more information and commands
                Type ".exit" to quit
                """
        );
    }
}
