package cr.ac.una.eif400.cyphail.cli.commands;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.nio.file.Files;
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

    @Parameters(index = "0", arity = "1", description = "Ruta de la ubicación del archivo")
    private Path inFilePath;

    @Option(names = {"--out", "-o"})
    private Path outFilePath;

    @Override
    public Integer call() throws Exception {
        try {
            if (!Files.exists(inFilePath)) throw new IllegalArgumentException("El archivo fuente no fue encontrado");

            IO.println("Mandar a compilar");

            return 0;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());

            return 1;
        }
    }
}
