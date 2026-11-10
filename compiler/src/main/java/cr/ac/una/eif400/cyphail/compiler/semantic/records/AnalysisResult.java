
package cr.ac.una.eif400.cyphail.compiler.semantic.records;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.AstNode;

import java.util.List;

/**
 * Resultado del análisis semántico de una consulta de Cyphail.
 * Contiene el AST, la tabla de símbolos y los diagnósticos generados.
 *
 * Prompt: Ahorita me encuentro depurando el tema del SemanticAnalyser para devolver la tabla de símbolos al CodeGen y que este la utilice. Está muy desordenado y quiero utilizar records de Symbol y de SymbolTable para tener una mejor organización. Qué cosas debería guardar el símbolo y la tabla de símbolos
 *
 * @author Andrey Solano
 * @version P2
 * @param ast el árbol de sintaxis abstracta analizado
 * @param symbols la tabla de símbolos construida durante el análisis
 * @param diagnostics la lista de errores semánticos encontrados
 */
public record AnalysisResult(
        AstNode ast,
        SymbolTable symbols,
        List<String> diagnostics
) {
    /**
     * Construye una copia inmutable de los diagnósticos.
     */
    public AnalysisResult {
        diagnostics = List.copyOf(diagnostics);
    }

    /**
     * Verifica si el análisis semántico fue exitoso.
     *
     * @return true si no existen errores semánticos, false en caso contrario
     */
    public boolean isValid() {
        return diagnostics.isEmpty();
    }
}
