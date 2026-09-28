package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.ast.PrettyPrinter;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.AstNode;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.parser.grammar.Grammar;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputString;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.result.*;
import cr.ac.una.eif400.cyphail.compiler.semantic.SemanticAnalyzer;

import java.util.List;

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
            case Ok(InputTokens _, AstNode _) -> broker.resolveQuery(source);
            case Fail(String reason) -> QueryResponse.error(reason);
        };
    }

    /**
     * Compila la consulta y devuelve su árbol de sintaxis dibujado, o el error encontrado.
     *
     * <p>Es lo que muestra el comando {@code .tree}.</p>
     *
     * @param source código fuente de la consulta.
     * @return el árbol como texto, o el mensaje de error.
     */

    public String tree(String source) {
        switch (Tokenizer.tokenize(source)) {
            case Fail(String reason) -> {
                return reason;
            }
            case Ok(InputString _, List<TokenString> tokens) -> {
                switch (Grammar.parse(tokens)) {
                    case Fail(String reason) -> {
                        return "Syntax error: " + reason;
                    }
                    case Ok(InputTokens _, AstNode ast) -> {
                        String treeStr = PrettyPrinter.print(ast);
                        List<String> errors = SemanticAnalyzer.check(ast);

                        if (!errors.isEmpty()) {
                            return treeStr + "\nSemantic error: " + String.join("; ", errors);
                        }
                        return treeStr + "\nSuccess";
                    }
                }
            }
        }
    }

    /**
     * Ejecuta las tres etapas de análisis, cortando en la primera que falle.
     *
     * <p>Está en un solo lugar para que {@link #compile} y {@link #tree} analicen exactamente
     * igual: lo que muestra el árbol es lo mismo que se manda a ejecutar.</p>
     *
     * @param source código fuente de la consulta.
     * @return el AST de la consulta, o el motivo del fallo.
     */
    private Result<InputTokens, AstNode, String> analyze(String source) {
        return switch (Tokenizer.tokenize(source)) {
            case Fail(String reason) -> new Fail<>(reason);
            case Ok(InputString _, List<TokenString> tokens) -> switch (Grammar.parse(tokens)) {
                case Fail(String reason) -> new Fail<>("Syntax error: " + reason);
                case Ok(InputTokens rest, AstNode ast) -> validate(rest, ast);
            };
        };
    }

    /**
     * Tercera etapa: revisa que toda variable usada haya sido declarada antes.
     *
     * @param rest entrada restante, que en este punto ya está consumida.
     * @param ast  árbol producido por el análisis sintáctico.
     * @return el mismo árbol si no hay errores, o el motivo del fallo.
     */
    private Result<InputTokens, AstNode, String> validate(InputTokens rest, AstNode ast) {
        List<String> reasons = SemanticAnalyzer.check(ast);

        return reasons.isEmpty()
                ? new Ok<>(rest, ast)
                : new Fail<>("Semantic error: " + String.join("; ", reasons));
    }
}