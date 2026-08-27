package cr.ac.una.eif400.cyphail.cli.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * @author Andrey Solano - Grupo 03-1pm
 * @version 1.0
 */
@Command(
        name = "run",
        description = "Comando para ejecutar un archivo .cyphail en la ruta relativa indicada por parámetro"
)
public class RunCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "1", description = "Ruta de la ubicación del archivo")
    private Path inFilePath;

    @Override
    public Integer call() throws Exception {
        try {
            if (!Files.exists(inFilePath)) throw new IllegalArgumentException("El archivo no fue encontrado");

            IO.println("Mandar a compilar");

            return 0;
        } catch (IllegalArgumentException e) {
            System.err.print(e.getMessage());

            return 1;
        }
    }
}
