package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Lee un script UTF-8 y lo entrega al REPL para ejecutar sus sentencias sobre la sesión actual.
 * La separación y compilación le corresponden al Compiler, igual que en la entrada interactiva.
 *
 * @version P2
 */
@Command(name = ".load", description = "Loads and executes a .cyphail script")
public class LoadCommand implements Callable<Integer> {
    @ParentCommand
    private Repl repl;

    @Parameters(index = "0", arity = "0..1", description = "Path to the .cyphail script")
    private String file;

    @Override
    public Integer call() {
        if (file == null) {
            IO.println("Error. Missing path. Usage: .load \"path/to/script.cyphail\"");
            return 1;
        }
        String filename = unquote(file);
        if (filename.isBlank()) {
            IO.println("Error. A script path is required.");
            return 1;
        }

        String content;
        try {
            content = Files.readString(Path.of(filename), StandardCharsets.UTF_8);
        } catch (IOException | InvalidPathException | SecurityException error) {
            IO.println("Error. Cannot read script '" + filename + "': " + error.getMessage());
            return 1;
        }
        if (content.startsWith("\uFEFF")) content = content.substring(1);

        return repl.executeQuery(content, true) ? 0 : 1;
    }

    /** El REPL entrega el argumento completo, incluyendo las comillas que rodean una ruta. */
    private static String unquote(String value) {
        String path = value.strip();
        if (path.length() >= 2 && ((path.startsWith("\"") && path.endsWith("\""))
                || (path.startsWith("'") && path.endsWith("'")))) {
            return path.substring(1, path.length() - 1);
        }
        return path;
    }
}
