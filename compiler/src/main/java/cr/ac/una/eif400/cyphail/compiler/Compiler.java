package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.common.QueryResponse;

/**
 * @author Andrey Solano
 * @author Sebastian Gonzalez
 * @author Alejandro Miranda
 * @version 2.0
 */

public class Compiler {
    private static final Compiler INSTANCE = new Compiler();
    private final Broker broker = Broker.getInstance();

    private Compiler() {
    }

    public static Compiler getInstance() {
        return INSTANCE;
    }

    public QueryResponse compile(String source) {
        // Compilación simulada (source -> tokenizer -> parser -> ast + st -> sematic -> ast + st -> codegen -> target)

        String target = source;
        return broker.resolveQuery(target);
    }
}