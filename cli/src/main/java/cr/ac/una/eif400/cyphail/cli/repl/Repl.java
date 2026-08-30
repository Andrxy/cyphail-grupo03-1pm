package cr.ac.una.eif400.cyphail.cli.repl;

import com.github.freva.asciitable.AsciiTable;
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

@Command(
        subcommands = {
                HelpCommand.class,
                AboutCommand.class,
                ExitCommand.class,
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
        Scanner scanner = new Scanner(System.in);

        IO.print(
                """
                Welcome to Cyphail-03-1pm v.0.1. August 2026. ESCINF/UNA EIF400-II-2026
                Visit www.whatiscyphail.com for more information
                Type ".help" for more information and commands
                Type ".exit" to quit
                """
        );

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

            if (firstWord.startsWith(".")) {
                dispatcher.execute(args);
            }
            else {
                executeQuery(line);
            }
        }
    }

    public void executeQuery(String sourceCode) {
        QueryResponse response = compiler.compile(sourceCode);

        if (response.isOk()) {
            IO.println(AsciiTable.getTable(response.getHeaders(), response.getRows()));
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
}
