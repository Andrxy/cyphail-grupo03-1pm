
package cr.ac.una.eif400.cyphail.compiler.semantic.records;

/**
 * Tipos de símbolos reconocidos durante el análisis semántico.
 *
 * Prompt: Ahorita me encuentro depurando el tema del SemanticAnalyser para devolver la tabla de símbolos al CodeGen y que este la utilice. Está muy desordenado y quiero utilizar records de Symbol y de SymbolTable para tener una mejor organización. Qué cosas debería guardar el símbolo y la tabla de símbolos
 *
 * @author Andrey Solano
 * @version P2
 */
public enum Type {
    NODE,
    RELATIONSHIP,
    ALIAS
}
