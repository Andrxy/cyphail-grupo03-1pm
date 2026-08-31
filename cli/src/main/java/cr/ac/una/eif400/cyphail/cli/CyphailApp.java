package cr.ac.una.eif400.cyphail.cli;

import cr.ac.una.eif400.cyphail.cli.commands.CyphailCommand;
import picocli.CommandLine;

/**
 * Clase principal de la aplicación.
 * Inicializa el flujo de ejecución del sistema recibiendo los argumentos
 * del cmd y delegándolos al comando raíz de Picocli, en este
 * caso sería el CyphailCommand.
 *
 * <p>Implementa un bloque try-catch que captura cualquier excepción no controlada
 * durante la ejecución, imprimiendo el mensaje correspondiente</p>
 *
 * @author Andrey Solano
 * @version P1.1
 */
public class CyphailApp {

    /**
     * Punto de entrada.
     * Ejecuta el comando raíz de picocli (CyphailCommand) con los argumentos
     * proporcionados por el usuario
     *
     * @param args argumentos recibidos desde el cmd
     */
    public static void main(String[] args) {
        try {
            int exitCode = new CommandLine(new CyphailCommand()).execute(args);
            System.exit(exitCode);
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
