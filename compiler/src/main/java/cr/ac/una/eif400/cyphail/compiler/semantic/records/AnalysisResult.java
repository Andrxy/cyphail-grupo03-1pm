package cr.ac.una.eif400.cyphail.compiler.semantic.records;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.AstNode;

import java.util.List;

public record AnalysisResult(
        AstNode ast,
        SymbolTable symbols,
        List<String> diagnostics
) {
    public AnalysisResult {
        diagnostics = List.copyOf(diagnostics);
    }

    public boolean isValid() {
        return diagnostics.isEmpty();
    }
}
