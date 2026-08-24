package com.cyphail.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;
@Command(
        name = ".about",
        description = "Muestra informacion sobre cyphail"
)
public class AboutCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("cyphail - transpilador de consultas sobre grafos");
        return 0;
    }
}