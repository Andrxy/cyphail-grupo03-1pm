package cr.ac.una.eif400.cyphail.compiler.semantic.records;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record SymbolTable(Map<String, Symbol> symbols) {

    public static SymbolTable create() {
        return new SymbolTable(Map.of());
    }

    public Optional<Symbol> lookup(String name) {
        return Optional.ofNullable(symbols.get(name));
    }

    public SymbolTable with(Symbol symbol) {
        var updated = new LinkedHashMap<>(symbols);
        updated.put(symbol.name(), symbol);

        return new SymbolTable(updated);
    }
}
