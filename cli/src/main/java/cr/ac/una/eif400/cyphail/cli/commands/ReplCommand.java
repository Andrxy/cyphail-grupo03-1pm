package cr.ac.una.eif400.cyphail.cli.commands;

import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import picocli.CommandLine.Command;

/**
 * Subcomando repl encargado de inicializar y lanzar la interfaz interactiva.
 *
 * <p>Implementa Runnable para definir su acción por defecto mediante el método run() de la interfaz,
 * además, delega la ejecución a la clase Repl para mantener la separación entre el subcomando
 * y el ciclo de vida del repl</p>
 *
 * @author Andrey Solano
 * @version P1.1
 */
@Command(
        name = "repl",
        description = "Launches the interactive REPL shell"
)
public class ReplCommand implements Runnable {

    /**
     * Acción del subcomando repl, crea una nueva instancia
     * del shell e inicia su ejecución
     */
    @Override
    public void run() {
        Repl repl = new Repl(Compiler.getInstance(), Broker.getInstance());
        repl.open();
    }
}