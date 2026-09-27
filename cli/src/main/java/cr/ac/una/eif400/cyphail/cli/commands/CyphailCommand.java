package cr.ac.una.eif400.cyphail.cli.commands;

import picocli.CommandLine.Command;

/**
 * Comando raíz de la aplicación.
 * Es el punto de entrada principal para picocli, agrupando y estructurando
 * los subcomandos disponibles del cli.
 *
 * @author Andrey Solano
 * @author Sebastián Gonzalez
 * @version P1
 */
@Command(
        name = "cyphail",
        subcommands = {
                ReplCommand.class,
        },
        version = "P1"
)
public class CyphailCommand implements Runnable {

    /**
     * Acción por defecto del comando raíz cuando se invoca sin subcomandos,
     * sugiere al usuario el comando abrir el repl.
     */
    @Override
    public void run() {
        IO.println("Cyphail CLI (vP1). Type 'cyphail repl' to open the repl shell");
    }
}