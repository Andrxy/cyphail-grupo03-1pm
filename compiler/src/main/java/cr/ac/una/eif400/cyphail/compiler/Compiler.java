package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.ast.AST;
import cr.ac.una.eif400.cyphail.compiler.codegen.CodeGenerator;
import cr.ac.una.eif400.cyphail.compiler.parser.Parser;
import cr.ac.una.eif400.cyphail.compiler.semantic.SemanticAnalyser;
import cr.ac.una.eif400.cyphail.compiler.support.FlowLog;
import cr.ac.una.eif400.cyphail.compiler.tokenizer.Tokenizer;

/**
 * Encargado de dirigir el flujo de compilación y ejecución.
 * Conecta las etapas del proceso de compilación con el sistema que administra los grafos
 * y resuelve las consultas mediante el Broker.
 *
 * <p>Implementa el patrón de creación Singleton para garantizar un punto de acceso único y global al compilador.
 * Su método principal simula el flujo completo de compilación (source -> tokenizer -> parser -> semantic -> codegen)
 * y delega la resolución final al broker.</p>
 *
 * <p>Para efectos de esta versión P1.1, la lógica interna se mantiene simulada ya que
 * la evaluación actual se centra únicamente en la arquitectura y flujo.</p>
 *
 * @author Andrey Solano
 * @author Sebastian Gonzalez
 * @author Alejandro Miranda
 * @author Wilson Umaña
 * @version 2.0
 */
public class Compiler {
    private static final Compiler INSTANCE = new Compiler();
    private final Broker broker = Broker.getInstance();

    private final Tokenizer tokenizer = new Tokenizer();
    private final Parser parser = new Parser();
    private final SemanticAnalyser semanticAnalyser = new SemanticAnalyser();
    private final CodeGenerator codeGenerator = new CodeGenerator();

    private Compiler() {
    }

    /**
     * Proporciona la instancia única del compilador siguiendo el patrón Singleton.
     *
     * @return la instancia global de Compiler.
     */
    public static Compiler getInstance() {
        return INSTANCE;
    }

    /**
     * Coordina el proceso de compilación y ejecución de una consulta.
     * Simula el pipeline interno del compilador y delega la resolución al broker del sistema.
     *
     * @param source código fuente.
     * @return el resultado de la ejecución envuelto en un QueryResponse.
     */
    public QueryResponse compile(String source) {
        StringBuilder log = new StringBuilder();

        log.append("[Tokenizer] Iniciando el proceso de tokenizacion...\n");
        String tokens = tokenizer.tokenize(source);
        log.append("[Tokenizer] Tokenizacion finalizada con exito.\n");

        log.append("[Parser] Iniciando el proceso de parseo...\n");
        AST ast = parser.parse(tokens);
        log.append("[Parser] Parseo finalizado con exito.\n");

        log.append("[Analisis Semantico] Iniciando el proceso de analisis semantico...\n");
        semanticAnalyser.analyse(ast);
        log.append("[Analisis Semantico] Analisis semantico finalizado con exito.\n");

        log.append("[Generacion de codigo] Iniciando el proceso de generacion de codigo...\n");
        codeGenerator.generate(ast);
        log.append("[Generacion de codigo] Generacion de codigo finalizada con exito.\n");

        log.append("[Broker] Mandando al broker...\n");
        log.append("[Broker] Recibido en el broker.\n");

        log.append("-".repeat(80) + "\n");
        FlowLog.write(log.toString());
        return broker.resolveQuery(tokens);
    }
}