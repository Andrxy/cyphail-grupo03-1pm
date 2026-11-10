package cr.ac.una.eif400.cyphail.compiler.ast.node_specializations;

import java.util.List;

/**
 * Verificación de etiquetas dentro del AST.
 *
 * @author Alejandro Miranda
 * @version P2
 * @param expression la expresión sobre la que se verifican las etiquetas
 * @param labels la lista de hojas que contienen las etiquetas
 */
public record LabelTest(AstNode expression, List<Leaf> labels) implements AstNode {
}