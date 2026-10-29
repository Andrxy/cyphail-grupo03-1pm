package cr.ac.una.eif400.cyphail.cli.repl;

import cr.ac.una.eif400.cyphail.cli.output.Formatter;
import cr.ac.una.eif400.cyphail.broker.Broker;
import cr.ac.una.eif400.cyphail.cli.repl.actions.AboutCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.ExitCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.HelpCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.LoadCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.TreeCommand;
import cr.ac.una.eif400.cyphail.cli.repl.actions.UseCommand;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Scanner;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Stream;

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
                TreeCommand.class,
                UseCommand.class,
                LoadCommand.class
        }
)
public class Repl {
    private static final String TREE_COMMAND = ".tree";
    private final CommandLine dispatcher;
    private final Broker broker;
    private final Compiler compiler;

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
     * Inicia el ciclo interactivo del shell del repl
     * Muestra el banner de bienvenida y procesa de forma secuencial las líneas de entrada
     * proporcionadas por el usuario a través de la entrada estándar hasta que se indique la salida.
     * Las consultas y .tree se completan con más líneas hasta el ';' o el fin de entrada.
     */
    public void open() {
        Scanner scanner = new Scanner(System.in);

        printBanner();

        Stream.generate(() -> readLine(scanner))
                .takeWhile(line -> line.isPresent())
                .map(line -> line.get())
                .filter(line -> !line.isBlank())
                .map(line -> spansLines(line) ? readStatement(scanner, line) : line.strip())
                .map(line -> dispatch(line))
                .anyMatch(code -> code == ExitCommand.EXIT_CODE);
    }

    /** Lee una línea con su prompt. Optional.empty indica que terminó la entrada. */
    private Optional<String> readLine(Scanner scanner) {
        IO.print(buildPrompt());
        if (!scanner.hasNextLine()) {
            IO.println();
            return Optional.empty();
        }
        return Optional.of(scanner.nextLine().stripLeading());
    }

    /**
     * Indica si la entrada es una consulta o .tree, los únicos casos que admiten varias líneas.
     */
    private static boolean spansLines(String line) {
        return !line.startsWith(".") || line.split("\\s+", 2)[0].equals(TREE_COMMAND);
    }

    /** Completa la consulta o el argumento de .tree sin enviar el nombre del comando al compilador. */
    private String readStatement(Scanner scanner, String firstLine) {
        StatementReader reader = new StatementReader();
        String[] parts = firstLine.split("\\s+", 2);
        if (parts[0].equals(TREE_COMMAND)) {
            String query = parts.length > 1 ? parts[1] : "";
            return TREE_COMMAND + " " + reader.read(scanner, query, source -> compiler.isTerminated(source));
        }
        return reader.read(scanner, firstLine, source -> compiler.isTerminated(source));
    }

    /**
     * Compila y muestra las respuestas de un script en orden. Deja de consumir el stream
     * en el primer error, por lo que las sentencias restantes no llegan al broker.
     *
     * @param sourceCode el código fuente de la consulta
     * @return true si la consulta se ejecutó correctamente
     */
    public boolean executeQuery(String sourceCode) {
        try (Stream<QueryResponse> responses = compiler.compileScript(sourceCode)) {
            return responses.allMatch(response -> {
                show(response);
                return response.isOk();
            });
        }
    }

    /** Imprime la tabla solo si hay encabezados y siempre muestra el mensaje de la respuesta. */
    private void show(QueryResponse response) {
        if (response.isOk() && response.getHeaders() != null && response.getHeaders().length > 0) {
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
                End queries with ";". Queries may span multiple lines.
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
     * Al subcomando se le entrega el resto de la línea como un único argumento, tal como se escribió,
     * para que Picocli no tome como opciones las palabras que empiezan con {@code -}, como en {@code -[:amigo]->}
     *
     * @param line la cadena de texto ingresada en la consola
     * @return el resultado del comando o la señal de salida de .exit
     */
    private int dispatch(String line) {
        String[] args = line.split("\\s+", 2);
        String firstWord = args[0];

        Set<String> actionNames = dispatcher.getSubcommands().keySet();

        if (actionNames.contains(firstWord)) {
            return dispatcher.execute(args);
        } else if (firstWord.startsWith(".")) {
            IO.println("Error. Unknown command '" + firstWord + "'. Type '.help' for the available commands");
            return 1;
        } else {
            return executeQuery(line) ? 0 : 1;
        }
    }
}
