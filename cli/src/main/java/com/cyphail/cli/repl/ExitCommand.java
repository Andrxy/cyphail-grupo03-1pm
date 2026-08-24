package com.cyphail.cli.repl;

import java.io.PrintStream;

/**
 * Comando ".exit": termina la sesion del REPL de Cyphail.
 * Segun el correo del sprint P1.1, confirmar la salida es opcional; esta
 * implementacion sale de una vez, sin pedir confirmacion.
 *
 * @author Sebastian Gonzalez
 * @version 1.0
 */
public class ExitCommand implements DotCommand {

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public String description() {
        return "Exit the Cyphail REPL";
    }

    @Override
    public boolean execute(String[] args, PrintStream out) {
        out.println("Bye! Thanks for using Cyphail.");
        return false; // false = el REPL debe detener su loop principal
    }
}
