package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;


@Command(
        name = ".tree",
        description = "Muestra las keywords reconocidas en una consulta"
)
public class TreeCommand implements Runnable {

    @Parameters(arity = "0..*", description = "Consulta a analizar")
    private String[] queryWords;

    @Override
    public void run() {
        if (queryWords == null) return;

        String query = String.join(" ", queryWords);

        List<TokenString> tokens = Tokenizer.tokenize(query);

        for (var t : tokens) IO.println(t.toString());
    }
}