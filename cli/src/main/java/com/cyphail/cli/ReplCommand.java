package com.cyphail.cli;

import picocli.CommandLine.Command;

import java.util.Scanner;

/**
 * Implementación del subcomando repl encargado de cargar el shell
 *
 * @author Andrey Solano
 * @version 1.0
 */
@Command(
        name = "repl",
        description = "Carga el REPL interactico de cyphail",
        subcommands = { UseCommand.class,
                        AboutCommand.class
                        }
)


public class ReplCommand implements Runnable {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("cyphail> ");

            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;

            if (line.equalsIgnoreCase("exit")) break;
        }
    }
}