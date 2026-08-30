package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

/**
 * Comando .exit dentro del REPL.
 * Se encarga de terminar inmediatamente la sesion activa del REPL.
 * Esto lo hace sin solicitud previa del usuario, ya que en las especificaciones
 * del sprint se menciona que es opcional.
 *
 * <p>Implementa Runnable para cumplir con la interfaz requerida por Picocli, así,
 * cuando el usuario llame el comando desde el REPL se invoca su declaración del método run()</p>
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @version P1.1
 */
@Command(
        name = ".exit",
        description = "Exits the REPL session"
)
public class ExitCommand implements Runnable {

    @ParentCommand
    private Repl repl;

    /**
     * Ejecuta la acción del comando .exit, mostrando un mensaje de despedida
     * y deteniendo la ejecución del REPL, esto invocando el metodo stop usando
     * el comando padre
     */
    @Override
    public void run() {
        IO.println("Bye! Hope I dont see you again");
        repl.stop();
    }
}