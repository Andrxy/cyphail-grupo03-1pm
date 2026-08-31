package cr.ac.una.eif400.cyphail.cli.repl;

import cr.ac.una.eif400.cyphail.cli.output.Formatter;
import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.cli.repl.actions.AboutCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.ExitCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.HelpCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.UseCommand;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import cr.ac.una.eif400.cyphail.compiler.support.FlowLog;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Scanner;
import java.util.Set;

/**
 * Bucle de lectura, evaluación e impresión (REPL) para el CLI de Cyphail.
 *
 * <p>Gestiona la entrada interactiva del usuario, coordina la ejecución de comandos del sistema
 * mediante picocli y delega la compilación y ejecución de consultas al compilador</p>
 *
 * @author Alejandro Miranda
 * @author Andrey Solano
 * @author Sebastián González
 * @author Wilson Umaña
 * @version P1.1
 */
@Command(
        subcommands = {
                HelpCommand.class,
                ExitCommand.class,
                AboutCommand.class,
                UseCommand.class
        }
)
public class Repl {
    private final CommandLine dispatcher;
    private final Broker broker;
    private final Compiler compiler;
    private boolean running = true;

    /**
     * Construye una nueva instancia del repl inicializando los componentes de compilación,
     * gestión de estados y el despachador de comandos de Picocli
     *
     * @param compiler motor de compilación para procesar las consultas
     * @param broker administrador del contexto y estado de los grafos
     */
    public Repl(Compiler compiler, Broker broker) {
        this.compiler = compiler;
        this.broker = broker;
        this.dispatcher = new CommandLine(this);
    }

    /**
     * Obtiene el broker asociado al repl
     *
     * @return la instancia actual de Broker
     */
    public Broker getBroker() {
        return broker;
    }

    /**
     * Verifica si el repl se encuentra en estado de ejecuciòn
     *
     * @return true si el bucle está activo, false en caso contrario
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Detiene la ejecución del bucle interactivo del repl
     */
    public void stop() {
        running = false;
    }

    /**
     * Inicia el ciclo interactivo del shell del repl
     * Muestra el banner de bienvenida y procesa de forma secuencial las líneas de entrada
     * proporcionadas por el usuario a través de la entrada estándar hasta que se indique la salida
     */
    public void open() {
        Scanner scanner = new Scanner(System.in);

        FlowLog.startSession();
        printBanner();

        while (running) {
            String prompt = buildPrompt();
            IO.print(prompt);

            if (!scanner.hasNextLine()) {
                IO.println();
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            dispatch(line);
        }
    }

    /**
     * Compila y ejecuta una sentencia de código fuente, formateando y mostrando el resultado
     * obtenido de manera tabular
     *
     * @param sourceCode el código fuente de la consulta
     */
    public void executeQuery(String sourceCode) {
        QueryResponse response = compiler.compile(sourceCode);

        if (response.isOk()) {
            IO.println(Formatter.format(response.getHeaders(), response.getRows()));
            IO.println();
        }

        IO.println(response.getMessage());
    }

    /**
     * Muestra en consola el banner de bienvenida y la información de versión de la aplicación
     */
    private void printBanner() {
        IO.print(
                """
                Welcome to Cyphail-03-1pm v.0.1. August 2026. ESCINF/UNA EIF400-II-2026
                Visit www.whatiscyphail.com for more information
                Type ".help" for more information and commands
                Type ".exit" to quit
                """
        );
    }

    /**
     * Construye el texto del prompt en función del contexto actual del grafo seleccionado
     *
     * @return una cadena con el prompt formateado
     */
    private String buildPrompt() {
        String currentGraph = broker.getCurrentGraph();
        return currentGraph != null
                ? "cyphail (" + currentGraph + ") >>> "
                : "cyphail>>> ";
    }

    /**
     * Enruta y procesa la línea de comandos introducida por el usuario.
     * Valida si es un subcomando interno del repl o si es una consulta para el compilador.
     *
     * @param line la cadena de texto ingresada en la consola
     */
    private void dispatch(String line) {
        String[] args = line.split("\\s+");
        String firstWord = args[0];
        Set<String> actionNames = dispatcher.getSubcommands().keySet();

        if (actionNames.contains(firstWord)) {
            dispatcher.execute(args);
        } else if (firstWord.startsWith(".")) {
            IO.println("Error. Unknown command '" + firstWord + "'. Type '.help' for the available commands");
        } else {
            executeQuery(line);
        }
    }
}
