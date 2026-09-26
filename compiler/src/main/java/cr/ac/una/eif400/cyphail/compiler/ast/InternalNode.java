package cr.ac.una.eif400.cyphail.compiler.lexer.ast;

import java.util.List;
import java.util.Optional;

public sealed interface InternalNode extends ASTNode {
    record LabelsNode(List<ASTNode> labelsName) implements InternalNode {
    }

    record MatchNode(ASTNode pattern, Optional<ASTNode> where) implements InternalNode {}

    record WhereNode(ASTNode expression) implements InternalNode {}
}
