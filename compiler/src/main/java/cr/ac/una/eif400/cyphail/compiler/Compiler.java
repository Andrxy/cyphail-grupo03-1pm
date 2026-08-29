package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.compiler.ast.AST;
import cr.ac.una.eif400.cyphail.compiler.codegen.CodeGenerator;
import cr.ac.una.eif400.cyphail.compiler.exceptions.CompilationException;
import cr.ac.una.eif400.cyphail.compiler.parser.Parser;
import cr.ac.una.eif400.cyphail.compiler.semantic.SemanticAnalyser;
import cr.ac.una.eif400.cyphail.compiler.tokenizer.Tokenizer;

/**
 * @author Andrey Solano
 * @author Sebastian Gonzalez (singleton + flujo fake Repl -> Compiler -> Broker)
 * @version 2.0
 */
public class Compiler {
    private static final Compiler INSTANCE = new Compiler();
    private final Broker broker = Broker.getInstance();

    // Pipeline real (P1) todavia no está conectado al REPL. Ver
    // compileWithFullPipeline() más abajo.
    private final Tokenizer tokenizer;
    private final Parser parser;
    private final SemanticAnalyser semanticAnalyser;
    private final CodeGenerator codeGenerator;

    private Compiler() {
        this.tokenizer = new Tokenizer();
        this.parser = new Parser();
        this.semanticAnalyser = new SemanticAnalyser();
        this.codeGenerator = new CodeGenerator();
    }

    public static Compiler getInstance() {
        return INSTANCE;
    }

    /**
     * Punto de entrada que usa el REPL
     *
     * Todavia no hay análisis sintactico real: se reconoce el sourceCode
     * contra el catálogo fake del Broker (flujo Repl -> Compiler ->
     * Broker -> Repl, tal como se propuso en la reunion). En un futuro
     * (Sprint 1), esto va a delegar en compileWithFullPipeline() en vez de
     * pedirle la respuesta ya armada al Broker.
     *
     * @param sourceCode texto crudo tecleado por el usuario en el REPL
     * @return respuesta exitosa con la tabla, o de error si no se reconoce
     */
    public QueryResponse compile(String sourceCode) {
        try {
            String content = broker.resolveQuery(sourceCode);

            if (content == null) {
                return QueryResponse.error(
                        "Error: statement not recognized. This sprint only wires a fixed set of example queries."
                );
            }

            return QueryResponse.ok(content);
        } catch (Exception e) {
            return QueryResponse.error("Error: unexpected failure compiling the query (" + e.getMessage() + ").");
        }
    }

    /**
     * Pipeline real de compilacion: tokenizer -> parser -> AST -> analisis
     * semantico -> codegen
     *
     * @param sourceCode texto crudo del script Cyphail
     * @return codigo Prolog generado (stub por ahora)
     * @throws CompilationException si cualquier paso del pipeline falla
     */
    public String compileWithFullPipeline(String sourceCode) throws CompilationException {
        try {
            String tokens = tokenizer.tokenize(sourceCode);
            AST ast = parser.parse(tokens);

            semanticAnalyser.analyse(ast);

            return codeGenerator.generate(ast);
        } catch (Exception e) {
            throw new CompilationException("Error al compilar la query: " + e.getMessage());
        }
    }
}