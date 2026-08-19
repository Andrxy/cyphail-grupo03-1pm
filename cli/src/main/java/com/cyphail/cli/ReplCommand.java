package com.cyphail.cli;

import picocli.CommandLine.Command;

import java.util.Scanner;

/**
 * @author Andrey Solano
 */
@Command(
        name = "repl",
        description = "Carga el REPL interactico de cyphail"
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