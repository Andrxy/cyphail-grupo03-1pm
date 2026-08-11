package com.cyphail.cli;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "cyphail",
        version = "1.0"
)
public class CyphailCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("El CLI de Cyphail ha iniciado correctamente");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CyphailCommand()).execute(args);
        System.exit(exitCode);
    }
}
