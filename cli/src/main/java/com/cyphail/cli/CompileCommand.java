package com.cyphail.cli;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * @author Andrey Solano
 * @version 1.0
 */
@Command(
        name = "compile"
)
public class CompileCommand implements Callable<Integer> {

    /**
     *
     */
    @Parameters(index = "0", arity = "1", description = "Ruta de la ubicación del archivo")
    private Path filePath;

    /**
     *
     */
    @Option(names = {"--out", "-o"})
    private Path outPath;

    /**
     *
     * @return
     */
    @Override
    public Integer call() {
        return 0; // falta agregar validaciones
    }
}
