package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.ast.ASTNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.ComparisonNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.CreateNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.DeleteNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.EmptyNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.LabelsNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.MatchNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.NodePatternNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.PatternNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.ProjectionNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.PropertiesNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.PropertyAccessNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.PropertyNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.QueryNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.ReturnNode;
import cr.ac.una.eif400.cyphail.compiler.ast.InternalNode.WhereNode;
import cr.ac.una.eif400.cyphail.compiler.ast.LeafNode;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.Grammar;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Fail;
import cr.ac.una.eif400.cyphail.compiler.parser.response.Ok;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Comando {@code .tree}: tokeniza y parsea una consulta, y muestra su arbol de sintaxis
 * o el error encontrado.
 *
 * <p>El dibujo del arbol usa solo caracteres ASCII para que se vea bien en la consola CMD.</p>
 *
 * <p>El dibujo del arbol fue escrito por el Grupo 03-1pm con la asistencia de
 * Claude (Anthropic), segun lo declarado en el README.</p>
 */
@Command(
        name = ".tree",
        description = "Shows the syntax tree of a query"
)
public class TreeCommand implements Runnable {

    @Parameters(arity = "0..1", description = "Query to analyze")
    private String queryWords;

    @Override
    public void run() {
    }
}