package com.cyphail.cli.repl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registro central de comandos punto del REPL de Cyphail.
 *
 * Cada integrante agrega su comando aca una sola vez, en el constructor,
 * llamando {@link #register(DotCommand)}. Esto evita que la logica de
 * despacho (dispatch) quede desparramada en if/else dentro de ReplCommand.
 *
 * NOTA PARA EL EQUIPO (Wilson): cuando termines UseCommand y AboutCommand,
 * agregalos aca con:
 *   register(new UseCommand());
 *   register(new AboutCommand());
 * No hace falta tocar ninguna otra clase; ".help" los va a listar solo.
 *
 * @author Sebastian Gonzalez
 * @version 1.0
 */
public class DotCommandRegistry {

    private final Map<String, DotCommand> commands = new LinkedHashMap<>();

    public DotCommandRegistry() {
        register(new ExitCommand());
        register(new HelpCommand(this));

        // Wil: register(new UseCommand());
        // Wil: register(new AboutCommand());
    }

    /**
     * Registra un comando nuevo. Si ya existe uno con el mismo nombre, lo reemplaza.
     *
     * @param command comando a registrar
     */
    public void register(DotCommand command) {
        commands.put(command.name(), command);
    }

    /**
     * @return todos los comandos registrados, ordenados alfabeticamente por nombre
     */
    public List<DotCommand> all() {
        List<DotCommand> list = new ArrayList<>(commands.values());
        list.sort(Comparator.comparing(DotCommand::name));
        return list;
    }

    /**
     * Procesa una linea completa tecleada en el REPL que empieza con ".".
     * Separa el nombre del comando de sus argumentos y despacha al DotCommand
     * correspondiente. Si el comando no existe, imprime un error en ingles
     * y deja el REPL corriendo.
     *
     * @param line linea cruda tecleada por el usuario (incluye el "." inicial)
     * @param out  stream de salida
     * @return {@code true} si el REPL debe seguir corriendo, {@code false} si debe terminar
     */
    public boolean dispatch(String line, PrintStream out) {
        String withoutDot = line.substring(1).trim();
        String[] parts = withoutDot.split("\\s+", 2);
        String name = parts[0].toLowerCase();
        String[] args = (parts.length > 1 && !parts[1].isBlank())
                ? parts[1].trim().split("\\s+")
                : new String[0];

        DotCommand command = commands.get(name);
        if (command == null) {
            out.println("Error: unknown command \"." + name + "\". Type \".help\" for available commands.");
            return true;
        }

        return command.execute(args, out);
    }
}
