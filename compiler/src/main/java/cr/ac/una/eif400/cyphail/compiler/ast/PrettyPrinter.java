package cr.ac.una.eif400.cyphail.compiler.ast;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Visitor hecho con interfaz sellada para formatear el AST del parseo a JSON <br><br>
 *
 * Prompt: Deme una explicación de cómo funciona el patrón visitor y su doble despacho y cómo funciona con las interfaces selladas para efectos de un AST
 * <br><br>
 *
 * Prompt: Tengo private static String listify(List<AstNode> items) { ... } y no compila, deme sugerencias para ver qué puedo hacer
 * <br><br>
 *
 * Prompt: Cuál es la diferencia entre hacer {@code <? extends AstNode>}  y {@code <T extends AstNode>}
 *
 * @author Andrey Solano
 * @version P1
 */
public class PrettyPrinter {

    private static String INDENT = "  ";

    /**
     * Convierte recursivamente un nodo del AST en su representación textual formateada
     *
     * @param current el nodo actual del AST a evaluar e imprimir
     * @return una cadena de texto con la representación estructurada del nodo
     */
    public static String print(AstNode current) {
        return switch (current) {
            case Query(var matches, var updates, var ret) -> block("Query",  each(matches), "Updates: " + listify(updates), each(ret));

            case Leaf(var token) -> token.value();

            case Node(var items) -> listify(items);

            case Match(var patterns, var where) -> lines(block("Match:", "Patterns: " + listify(patterns)), each(where));

            case Where(var condition) -> block("Where:", "Expr: " + print(condition));

            case Create(var patterns) -> block("Create:", "Patterns: " + listify(patterns));

            case Delete(var targets) -> block("Delete:", "Exprs: " + listify(targets));

            case Return(var projections) -> block("Return:", block("Projection:", "Items: " + listify(projections), "Modifiers: []"));

            case Path(var from, var relationship, var to) -> block("Path:", print(from), print(relationship), print(to));

            case Pattern(var variable, var labels, var properties) -> block("PatternNode:", "var: " + each(variable), "labels: " + inline(labels), "properties: " + listify(properties));

            case Property(var key, var value) -> print(key) + ": " + print(value);

            case PropertyAccess(var variable, var key) -> "(. " + print(variable) + " " + print(key) + ")";

            case Comparison(var left, var operator, var right) -> "(" + print(operator) + " " + print(left) + " " + print(right) + ")";

            case SimpleProjection(var expression) -> print(expression);

            case AliasedProjection(var expression, var alias) -> "(as " + print(expression) + " " + print(alias) + ")";
        };
    }

    /**
     * Formatea la lista de nodos colocando cada elemento en una línea independiente delimitada por corchetes
     *
     * @param items la lista de elementos a listar
     * @param <L> el tipo del nodo que extiende de {@link AstNode}
     * @return una cadena formateada con los elementos en múltiples líneas
     */
    private static <L extends AstNode> String listify(List<L> items) {
        return items.isEmpty() ? "[]" : "[\n" + indent(each(items)) + "\n]";
    }

    /**
     * Formatea la lista de nodos en una sola línea separados por comas y encerrados entre corchetes
     *
     * @param items la lista de elementos a mostrar en línea
     * @param <L> el tipo del nodo que extiende de {@link AstNode}
     * @return una cadena con los elementos separados por comas
     */
    private static <L extends AstNode> String inline(List<L> items) {
        return "[" + items.stream().map(PrettyPrinter::print).collect(joining(", ")) + "]";
    }

    /**
     * Imprime cada elemento de la lista en una línea independiente, sin corchetes.
     *
     * @param items la lista de elementos a procesar
     * @param <L> el tipo del nodo que extiende de {@link AstNode}
     * @return una cadena con los elementos unidos por saltos de línea
     */
    private static <L extends AstNode> String each(List<L> items) {
        return items.stream().map(PrettyPrinter::print).collect(joining("\n"));
    }

    /**
     * Genera un bloque con nombre y un cuerpo de campos indentados
     *
     * @param name el nombre o etiqueta del bloque
     * @param fields los campos o líneas que forman el contenido del bloque
     * @return una cadena que representa el bloque estructurado
     */
    private static String block(String name, String... fields) {
        var body = lines(fields);
        return body.isEmpty() ? name + " {}" : name + " {\n" + indent(body) + "\n}";
    }

    /**
     * Une múltiples partes de texto mediante saltos de línea, omitiendo aquellas que estén vacías.
     *
     * @param parts las partes de texto a unir
     * @return la cadena combinada resultante
     */
    private static String lines(String... parts) {
        return Arrays.stream(parts).filter(part -> !part.isEmpty()).collect(joining("\n"));
    }

    /**
     * Aplica el nivel de indentación configurado a cada línea de un texto dado.
     *
     * @param text el texto al cual se le aplicará la indentación
     * @return el texto indentado
     */
    private static String indent(String text) {
        return text.lines().map(line -> INDENT + line).collect(joining("\n"));
    }
}