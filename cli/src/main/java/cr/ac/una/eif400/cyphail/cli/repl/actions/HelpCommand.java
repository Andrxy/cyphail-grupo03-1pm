package cr.ac.una.eif400.cyphail.cli.repl.actions;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Comando ".help": muestra la lista de comandos punto disponibles en el REPL.
 * La lista se obtiene del modelo de picocli, leyendo los subcomandos
 * registrados en el comando padre y sus descripciones.
 *
 * @author Sebastian Gonzalez - Grupo 03-1pm
 * @author Andrey Solano
 * @version 1.1
 */

@Command(
        name = ".help",
        description = "Muestra la lista de comandos disponibles"
)

public class HelpCommand implements Runnable {
    @Override
    public void run() {
        IO.println(
                """
                Cyphail REPL - available commands:
                - .help
                - .exit
                - .about
                - .use
                Type a command starting with "." or a Cyphail statement.
                """
        );
    }
}
