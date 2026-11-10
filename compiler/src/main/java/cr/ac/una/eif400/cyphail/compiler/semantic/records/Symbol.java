
package cr.ac.una.eif400.cyphail.compiler.semantic.records;

/**
 * Representa un símbolo registrado en la tabla de símbolos.
 *
 * Prompt: Ahorita me encuentro depurando el tema del SemanticAnalyser para devolver la tabla de símbolos al CodeGen y que este la utilice. Está muy desordenado y quiero utilizar records de Symbol y de SymbolTable para tener una mejor organización. Qué cosas debería guardar el símbolo y la tabla de símbolos
 *
 * @author Andrey Solano
 * @version P2
 * @param name el nombre de la variable
 * @param type el tipo de símbolo (nodo, relación o alias)
 * @param labelsDeclared indica si las etiquetas del nodo ya fueron declaradas
 */
public record Symbol(String name, Type type, boolean labelsDeclared) {
}
