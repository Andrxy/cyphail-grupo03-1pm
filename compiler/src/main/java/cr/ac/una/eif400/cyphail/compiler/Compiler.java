package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.cyphail.broker.Broker;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.ast.PrettyPrinter;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.AstNode;
import cr.ac.una.eif400.cyphail.compiler.codegen.CodeGen;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.Statements;
import cr.ac.una.eif400.cyphail.compiler.parser.Statement;
import cr.ac.una.eif400.cyphail.compiler.parser.grammar.Grammar;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.result.*;
import cr.ac.una.eif400.cyphail.compiler.semantic.records.AnalysisResult;
import cr.ac.una.eif400.cyphail.compiler.semantic.SemanticAnalyzer;

import java.util.List;
import java.util.stream.Stream;

/**
 * Encargado de dirigir el flujo de compilación y ejecución.
 *
 * <p>Es el único punto del proyecto que conoce las etapas del compilador. El CLI le pide
 * resultados ({@link #compile}) o el árbol de una consulta ({@link #tree}), y nunca invoca
 * al Tokenizer, a la Grammar ni al SemanticAnalyzer por su cuenta.</p>
 *
 * <p>El flujo es: código fuente, análisis léxico, análisis sintáctico, análisis semántico,
 * y solo si las tres etapas pasan se delega la resolución al broker.</p>
 *
 * <p>Implementa el patrón Singleton para garantizar un punto de acceso único y global.</p>
 *
 * @author Andrey Solano
 * @author Sebastian Gonzalez
 * @author Alejandro Miranda
 * @author Wilson Umaña
 *
 * <p>Los metodos {@code analyze()} y {@code validate()}, que encadenan las tres
 * etapas del compilador, se escribieron con la asistencia de Claude (Anthropic)
 * y fueron revisados e integrados por el equipo. Corresponde al prompt 5 del
 * Sprint P1 declarado en el README.</p>
 *
 * @version P1
 */
public class Compiler {
    private static final Compiler INSTANCE = new Compiler();
    private final Broker broker = Broker.getInstance();

    private Compiler() {
    }

    /**
     * Proporciona la instancia única del compilador siguiendo el patrón Singleton
     *
     * @return la instancia global de Compiler.
     */
    public static Compiler getInstance() {
        return INSTANCE;
    }

    /**
     * Compila la consulta y, si es válida, la manda a resolver al broker.
     *
     * <p>Una consulta con errores no llega al broker: se devuelve como un {@link QueryResponse}
     * de error, para que el REPL lo muestre como mensaje y nunca como una tabla vacía
     * o mal formada, tal como lo pide el SPEC.</p>
     *
     * @param source código fuente de la consulta.
     * @return el resultado de la ejecución, o el error de compilación.
     */
    public QueryResponse compile(String source) {
        return switch (analyze(source)) {
            case Ok(InputTokens _, AnalysisResult analysis) ->
                    resolve(analysis);

            case Fail(String reason) ->
                    QueryResponse.error(reason);
        };
    }

    /**
     * Ejecuta un script de forma perezosa: cada respuesta se calcula cuando se solicita.
     * Quien deje de consumir el stream evita ejecutar las sentencias restantes.
     *
     * @param script una o varias sentencias separadas por punto y coma
     * @return las respuestas en orden, o un error léxico si el script no se puede tokenizar
     */
    public Stream<QueryResponse> compileScript(String script) {
        return switch (readScript(script)) {
            case Fail(String reason) -> Stream.of(QueryResponse.error(reason));
            case Ok(InputString _, List<Statement> statements) ->
                    statements.stream().map(statement -> compileStatement(statement));
        };
    }

    /** Separa el script y conserva su texto sin ejecutar ninguna sentencia. */
    public Result<InputString, List<Statement>, String> readScript(String script) {
        return switch (Tokenizer.tokenizeWithComments(script)) {
            case Fail(String reason) -> new Fail<>(reason);
            case Ok(InputString rest, List<TokenString> tokens) ->
                    new Ok<>(rest, Statements.withSource(script, tokens));
        };
    }

    /**
     * Revisa si el último token es un punto y coma. Un error léxico también termina
     * la lectu ra para que el REPL lo muestre de inmediato.
     *
     * @param source el texto acumulado hasta ahora
     * @return true si se puede despachar, false si hacen falta más líneas
     */
    public boolean isTerminated(String source) {
        return switch (Tokenizer.tokenize(source)) {
            case Fail(String _) -> true;
            case Ok(InputString _, List<TokenString> tokens) ->
                    !tokens.isEmpty() && tokens.getLast().type() == TokenType.SEMICOLON;
        };
    }

    /**
     * Transpila el AST validado y entrega al broker el goal de Prolog generado.
     * Las construcciones aún no soportadas se devuelven como errores de compilación.
     *
     * @param analysis
     * @return la respuesta del broker
     */
    private QueryResponse resolve(AnalysisResult analysis) {
        try {
            return broker.resolveQuery(CodeGen.generate(analysis));
        } catch (UnsupportedOperationException error) {
            return QueryResponse.error(
                    "Code generation error: " + error.getMessage()
            );
        }
    }

    /**
     * Compila los tokens de una sentencia y ejecuta el Prolog generado.
     *
     * @param statement la sentencia separada del script con sus tokens
     * @return la respuesta de la ejecución o del análisis
     */
    public QueryResponse compileStatement(Statement statement) {
        return switch (analyze(statement.tokens())) {
            case Ok(InputTokens _, AnalysisResult analysis) ->
                    resolve(analysis);

            case Fail(String reason) ->
                    QueryResponse.error(reason);
        };
    }

    /** Quita el token terminador antes de entregar la sentencia a la gramática. */
    private static List<TokenString> withoutTerminator(List<TokenString> tokens) {
        return !tokens.isEmpty() && tokens.getLast().type() == TokenType.SEMICOLON
                ? tokens.subList(0, tokens.size() - 1)
                : tokens;
    }

    /**
     * Dibuja un árbol por sentencia, separados por una línea en blanco.
     *
     * <p>Es lo que muestra el comando {@code .tree}.</p>
     *
     * @param source código fuente de una o varias sentencias.
     * @return el árbol como texto, o el mensaje de error.
     */

    public String tree(String source) {
        switch (Tokenizer.tokenize(source)) {
            case Fail(String reason) -> {
                return reason;
            }
            case Ok(InputString _, List<TokenString> tokens) -> {
                List<List<TokenString>> statements = Statements.split(tokens);
                if (statements.isEmpty()) {
                    return treeOf(tokens);
                }

                StringBuilder trees = new StringBuilder();
                for (List<TokenString> statement : statements) {
                    if (!trees.isEmpty()) {
                        trees.append("\n\n");
                    }
                    trees.append(treeOf(statement));
                }
                return trees.toString();
            }
        }
    }

    /** Dibuja el AST y los errores semánticos de una sentencia ya separada. */
    private String treeOf(List<TokenString> tokens) {
        switch (Grammar.parse(tokens)) {
            case Fail(String reason) -> {
                return "Syntax error: " + reason;
            }
            case Ok(InputTokens _, AstNode ast) -> {
                String treeStr = PrettyPrinter.print(ast);
                List<String> errors = SemanticAnalyzer.check(ast);
                if (errors.isEmpty()) {
                    return treeStr + "\nSuccess";
                }
                return treeStr + "\nSemantic error: " + String.join("; ", errors);
            }
        }
    }

    /**
     * Ejecuta las tres etapas de análisis, cortando en la primera que falle.
     *
     * <p>El análisis sintáctico y semántico se comparte con compileScript mediante
     * la sobrecarga que recibe tokens.</p>
     *
     * @param source código fuente de la consulta.
     * @return el AST de la consulta, o el motivo del fallo.
     */
    private Result<InputTokens, AnalysisResult, String> analyze(String source) {
        return switch (Tokenizer.tokenize(source)) {
            case Fail(String reason) ->
                    new Fail<>(reason);

            case Ok(InputString _, List<TokenString> tokens) ->
                    analyze(tokens);
        };
    }

    /** Ejecuta el análisis sintáctico y semántico de una sentencia con o sin terminador. */
    private Result<InputTokens, AnalysisResult, String> analyze(List<TokenString> tokens) {
        return switch (Grammar.parse(withoutTerminator(tokens))) {
            case Fail(String reason) ->
                    new Fail<>("Syntax error: " + reason);

            case Ok(InputTokens rest, AstNode ast) ->
                    validate(rest, ast);
        };
    }

    /**
     * Tercera etapa: revisa que toda variable usada haya sido declarada antes.
     *
     * @param rest entrada restante, que en este punto ya está consumida.
     * @param ast  árbol producido por el análisis sintáctico.
     * @return el mismo árbol si no hay errores, o el motivo del fallo.
     */
    private Result<InputTokens, AnalysisResult, String> validate(InputTokens rest, AstNode ast) {
        var analysis = SemanticAnalyzer.analyze(ast);

        return analysis.isValid()
                ? new Ok<>(rest, analysis)
                : new Fail<>(
                "Semantic error: "
                        + String.join("; ", analysis.diagnostics())
        );
    }
}
