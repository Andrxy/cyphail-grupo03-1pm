
package cr.ac.una.eif400.cyphail.compiler.semantic.records;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tabla de símbolos utilizada durante el análisis semántico.
 * Permite registrar y consultar las variables de una consulta.
 *
 * Prompt: Ahorita me encuentro depurando el tema del SemanticAnalyser para devolver la tabla de símbolos al CodeGen y que este la utilice. Está muy desordenado y quiero utilizar records de Symbol y de SymbolTable para tener una mejor organización. Qué cosas debería guardar el símbolo y la tabla de símbolos
 *
 * @author Andrey Solano
 * @version P2
 * @param symbols el mapa de nombres de variables y sus símbolos asociados
 */
public record SymbolTable(Map<String, Symbol> symbols) {

    /**
     * Crea una tabla de símbolos vacía.
     *
     * @return una nueva tabla de símbolos vacía
     */
    public static SymbolTable create() {
        return new SymbolTable(Map.of());
    }

    /**
     * Busca un símbolo por su nombre.
     *
     * @param name el nombre del símbolo a buscar
     * @return un Optional con el símbolo encontrado, o vacío si no existe
     */
    public Optional<Symbol> lookup(String name) {
        return Optional.ofNullable(symbols.get(name));
    }

    /**
     * Registra o reemplaza un símbolo en una nueva tabla.
     *
     * @param symbol el símbolo que se desea registrar
     * @return una nueva tabla de símbolos con el símbolo actualizado
     */
    public SymbolTable with(Symbol symbol) {
        var updated = new LinkedHashMap<>(symbols);
        updated.put(symbol.name(), symbol);

        return new SymbolTable(updated);
    }
}
