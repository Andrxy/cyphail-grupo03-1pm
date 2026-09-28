package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Cláusula de creación ({@code CREATE}) dentro del AST
 *
 * @author Andrey Solano
 * @version P1
 * @param patterns la lista de patrones de grafos a crear
 */
public record Create(List<Pattern> patterns) implements AstNode {
}