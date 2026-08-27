package cr.ac.una.eif400.cyphail.cli.cmd;

/**
 * Estado compartido de una sesion interactiva del REPL de Cyphail.
 *
 * Los comandos punto no pueden detener el ciclo de lectura mediante su valor
 * de retorno, porque picocli lo interpreta como codigo de salida del proceso.
 * Esta clase es el canal por el cual ".exit" le avisa al ciclo que la sesion
 * termino.
 *
 * @author Alejandro Miranda - Grupo 03-1pm
 * @version 1.0
 */
public class ReplSession {

    private boolean running = true;

    /**
     * @return true mientras la sesion siga activa
     */
    public boolean isRunning() {
        return running;
    }

    public void requestExit() {
        running = false;
    }
}
