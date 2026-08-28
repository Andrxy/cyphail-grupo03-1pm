package cr.ac.una.eif400.cyphail.cli.repl.commands;

import cr.ac.una.eif400.cyphail.cli.commands.ReplCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

/**
 * Comando ".exit": termina la sesion del REPL de Cyphail.
 * Segun el correo del sprint P1.1, confirmar la salida es opcional; esta
 * implementacion sale de una vez, sin pedir confirmacion.
 *
 * @author Alejandro Miranda - Grupo 03-1pm
 * @author Andrey Solano
 * @version 1.2
 */

@Command(
        name = ".exit",
        description = "Salir de Cyphail REPL"
)
public class ExitCommand implements Runnable {

    @ParentCommand
    private ReplCommand parent;

    @Override
    public void run() {
        IO.println("Bye! Thanks for using Cyphail.");
        // parent.session().requestExit();
    }
}