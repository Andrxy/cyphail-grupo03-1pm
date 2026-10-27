package cr.ac.una.eif400.cyphail.cli.repl.actions;

import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

/**
 * Comando .exit dentro del REPL.
 * Devuelve una señal para que el flujo de entrada termine sin modificar el estado del REPL.
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @version P2
 */
@Command(
        name = ".exit",
        description = "Exits the REPL session"
)
public class ExitCommand implements Callable<Integer> {
    /** Señal interna que distingue .exit de un comando exitoso o un error. */
    public static final int EXIT_CODE = 100;

    /**
     * Ejecuta el comando .exit, mostrando un mensaje de despedida
     * y devolviendo la señal que detiene el flujo del REPL.
     *
     * @return la señal de salida
     */
    @Override
    public Integer call() {
        IO.println("Bye! Hope I dont see you again");
        return EXIT_CODE;
    }
}
