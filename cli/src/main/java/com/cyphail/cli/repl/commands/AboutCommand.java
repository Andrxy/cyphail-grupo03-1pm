package com.cyphail.cli.repl.commands;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

/**
 * Comando ".about": muestra una breve descripcion de que es Cyphail y para
 * que sirve, sin salir de la sesion del REPL.
 *
 * @author Wilson Umaña - Grupo 03-1pm
 * @version 1.0
 */

@Command(
        name = ".about",
        description = "Muestra informacion sobre cyphail"
)

public class AboutCommand implements Callable<Integer> {
    @Override
    public Integer call() {
        IO.println("cyphail - transpilador de consultas sobre grafos");
        return 0;
    }
}