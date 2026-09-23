package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = ".tree",
        description = "Muestra los tokens reconocidos en una consulta"
)
public class TreeCommand implements Runnable {

    @Parameters(arity = "0..*", description = "Consulta a analizar")
    private String[] queryWords;

    @Override
    public void run() {
        analizar(queryWords == null ? "" : String.join(" ", queryWords));
    }

    /** Recibe la consulta cruda. El REPL debe llamar este metodo directamente
     *  para que picocli no trate los guiones como opciones. */
    public static void analizar(String query) {
        try {
            Tokenizer.tokenize(query).forEach(t -> IO.println(t.toString()));
        } catch (RuntimeException e) {
            IO.println("Error lexico: " + e.getMessage());
        }
    }
}