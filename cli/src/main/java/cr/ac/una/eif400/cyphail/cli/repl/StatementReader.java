package cr.ac.una.eif400.cyphail.cli.repl;

import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Acumula líneas hasta que el compilador indique que la entrada se puede despachar.
 * No interpreta strings, comentarios ni separa sentencias: eso lo hace el compilador.
 *
 * @version P2
 */
public final class StatementReader {
    /**
     * Lee una consulta multilínea. Al terminar la entrada devuelve lo leído, aunque falte ';'.
     *
     * @param scanner la entrada de consola
     * @param firstLine la primera línea de la consulta
     * @param isTerminated la comprobación de terminación del compilador
     * @return el texto acumulado, conservando los saltos de línea
     */
    public String read(Scanner scanner, String firstLine, Predicate<String> isTerminated) {
        String statement = firstLine;
        while (!isTerminated.test(statement)) {
            IO.print("     ...> ");
            if (!scanner.hasNextLine()) {
                IO.println();
                break;
            }
            statement += "\n" + scanner.nextLine();
        }
        return statement;
    }
}
