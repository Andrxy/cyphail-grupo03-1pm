package cr.ac.una.eif400.cyphail.cli;

import cr.ac.una.eif400.cyphail.cli.commands.CyphailCommand;
import picocli.CommandLine;

/**
 * @Author Andrey Solano
 */
public class CyphailApp {
    static void main(String[] args) {
        try {
            int exitCode = new CommandLine(new CyphailCommand()).execute(args);
            System.exit(exitCode);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
