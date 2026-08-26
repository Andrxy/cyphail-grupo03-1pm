package com.cyphail.cli.repl.commands;

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
 * @version 1.1
 */

@Command(
        name = ".help",
        description = "Muestra la lista de comandos disponibles"
)

public class HelpCommand implements Callable<Integer> {

    private static final int SEPARATION = 3;
    private static final int MINIMUM_WIDTH = 6;
    private static final String LAST_LISTED_COMMAND = ".exit";

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        Map<String, CommandLine> commands = availableCommands();
        int width = longestNameLength(commands) + SEPARATION;

        IO.println("Cyphail REPL - available commands:");
        IO.println();

        commands.forEach((name, command) ->
                IO.println(String.format("  %-" + width + "s%s", name, descriptionOf(command))));

        IO.println();
        IO.println("Type a command starting with \".\" or a Cyphail statement.");

        return 0;
    }

    private Map<String, CommandLine> availableCommands() {
        CommandSpec parent = spec.parent() != null ? spec.parent() : spec;
        return parent.subcommands().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(listingOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new));
    }

    private Comparator<String> listingOrder() {
        return Comparator
                .<String, Boolean>comparing(LAST_LISTED_COMMAND::equals)
                .thenComparing(Comparator.<String>naturalOrder());
    }

    private int longestNameLength(Map<String, CommandLine> commands) {
        return commands.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(MINIMUM_WIDTH);
    }

    private String descriptionOf(CommandLine command) {
        String[] description = command.getCommandSpec().usageMessage().description();
        return description.length > 0 ? description[0] : "";
    }
}
