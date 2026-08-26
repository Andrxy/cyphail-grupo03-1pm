package com.cyphail.cli.repl;

/**
 * Mensaje de bienvenida que se muestra al arrancar "cyphail repl", siguiendo
 * el formato pedido en el correo del sprint P1.1 (18 agosto 2026):
 *
 * <pre>
 * Welcome to Cyphail-01-5pm v.0.1. August 2026. ESCINF/UNA EIF400-II-2026
 * Visit www.whatiscyphail.com for more information
 * Type ".help" for more information and commands
 * Type ".exit" to quit
 * </pre>
 *
 * GROUP_CODE debe coincidir con el codigo real de grupo-horario
 * (ajustar si el repo no es grupo03-1pm).
 *
 * @author Sebastian Gonzalez - Grupo 03-1pm
 * @version 1.0
 */
public final class ReplBanner {

    private static final String GROUP_CODE = "03-1pm";
    private static final String VERSION = "0.1";

    private ReplBanner() {
        // clase de utilidad, no se instancia
    }

    /**
     * Imprime el banner de bienvenida en la salida estandar.
     */
    public static void print() {
        IO.println("Welcome to Cyphail-" + GROUP_CODE + " v." + VERSION + ". August 2026. ESCINF/UNA EIF400-II-2026");
        IO.println("Visit www.whatiscyphail.com for more information");
        IO.println("Type \".help\" for more information and commands");
        IO.println("Type \".exit\" to quit");
        IO.println();
    }
}
