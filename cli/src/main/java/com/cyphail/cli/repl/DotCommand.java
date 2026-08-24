package com.cyphail.cli.repl;

import java.io.PrintStream;

/**
 * Contrato que debe cumplir cada comando punto (dot-command) del REPL de Cyphail.
 * Los comandos punto son instrucciones que empiezan con "." (ej. .help, .exit, .use, .about)
 * y se procesan directamente en el REPL, sin pasar por el parser formal de Cyphail
 * (ese parser aun no existe en este sprint).
 *
 * Cada integrante que agregue un comando nuevo debe implementar esta interfaz
 * y registrarlo en {@link DotCommandRegistry}.
 *
 * @author Sebastian Gonzalez
 * @version 1.0
 */
public interface DotCommand {

    /**
     * Nombre del comando, sin el punto inicial (ej. "exit" para ".exit").
     *
     * @return nombre del comando en minusculas
     */
    String name();

    /**
     * Descripcion corta en ingles, usada por ".help" para listar los comandos disponibles.
     *
     * @return descripcion de una linea
     */
    String description();

    /**
     * Ejecuta el comando y escribe su resultado en {@code out}.
     *
     * @param args argumentos despues del nombre del comando
     *             (ej. para ".use amigos", args = {@code ["amigos"]}; puede venir vacio)
     * @param out  stream de salida donde el comando debe escribir su resultado
     * @return {@code true} si el REPL debe seguir corriendo, {@code false} si debe terminar
     *         (usado por ".exit")
     */
    boolean execute(String[] args, PrintStream out);
}
