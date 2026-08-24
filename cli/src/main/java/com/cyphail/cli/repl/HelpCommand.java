package com.cyphail.cli.repl;

import java.io.PrintStream;
import java.util.List;

/**
 * Comando ".help": muestra la lista de comandos punto disponibles en el REPL.
 *
 * La lista se arma dinamicamente a partir de {@link DotCommandRegistry#all()},
 * asi que queda actualizada automaticamente a medida que el equipo registre
 * comandos nuevos (ej. cuando Wilson agregue ".use" y ".about"), sin tener
 * que tocar esta clase.
 *
 * @author Sebastian Gonzalez
 * @version 1.0
 */
public class HelpCommand implements DotCommand {

    private final DotCommandRegistry registry;

    /**
     * @param registry registro de comandos del cual esta ayuda lee la lista a mostrar
     */
    public HelpCommand(DotCommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "Show this list of available commands";
    }

    @Override
    public boolean execute(String[] args, PrintStream out) {
        List<DotCommand> commands = registry.all();

        int width = commands.stream()
                .mapToInt(c -> c.name().length())
                .max()
                .orElse(4);

        out.println("Cyphail REPL - available commands:");
        out.println();

        for (DotCommand command : commands) {
            out.printf("  .%-" + (width + 3) + "s%s%n", command.name(), command.description());
        }

        out.println();
        out.println("Type a command starting with \".\" or a Cyphail statement.");

        return true; // .help nunca detiene el REPL
    }
}
