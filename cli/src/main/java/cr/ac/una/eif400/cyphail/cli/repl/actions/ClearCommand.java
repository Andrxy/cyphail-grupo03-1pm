package cr.ac.una.eif400.cyphail.cli.repl.actions;

import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

/**
 * Comando .clear dentro del REPL.
 * Limpia la pantalla de la terminal enviando secuencias de escape ANSI y no modifica
 * el estado del REPL ni el grafo seleccionado.
 *
 * @author Alejandro Miranda
 * @version P2
 */
@Command(
        name = ".clear",
        description = "Clears the screen"
)
public class ClearCommand implements Callable<Integer> {
    /** Mueve el cursor a la esquina superior izquierda y borra la pantalla visible. */
    private static final String CLEAR_SCREEN = "\033[H\033[2J";

    /**
     * Ejecuta el comando .clear, imprimiendo la secuencia que limpia la pantalla.
     *
     * @return 0 siempre, ya que limpiar la pantalla no puede fallar
     */
    @Override
    public Integer call() {
        IO.print(CLEAR_SCREEN);
        return 0;
    }
}
