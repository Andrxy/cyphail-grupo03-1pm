package cr.ac.una.eif400.cyphail.compiler.ast;

import cr.ac.una.eif400.cyphail.compiler.ast.specializations.*;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class PrettyPrinter {

    private static final String INDENT = "  ";

    public static String print(AstNode current) {
        return switch (current) {
            case Query(var matches, var updates, var ret) ->
                    block("Query", each(matches), "Updates: " + listify(updates), each(ret));
            case Leaf(var token) -> token.value();
            case Node(var items) -> listify(items);
            case Match(var patterns, var where) ->
                    lines(block("Match:", "Patterns: " + listify(patterns)), each(where));
            case Where(var condition) -> block("Where:", "Expr: " + print(condition));
            case Create(var patterns) -> block("Create:", "Patterns: " + listify(patterns));
            case Delete(var targets) -> block("Delete:", "Exprs: " + listify(targets));
            case Return(var projections) ->
                    block("Return:", block("Projection:", "Items: " + listify(projections), "Modifiers: []"));
            case Path(var from, var relationship, var to) ->
                    block("Path:", print(from), print(relationship), print(to));
            case Pattern(var variable, var labels, var properties) -> block("PatternNode:",
                    "var: " + each(variable),
                    "labels: " + inline(labels),
                    "properties: " + listify(properties));
            case Property(var key, var value) -> print(key) + ": " + print(value);
            case PropertyAccess(var variable, var key) -> "(. " + print(variable) + " " + print(key) + ")";
            case Comparison(var left, var operator, var right) ->
                    "(" + print(operator) + " " + print(left) + " " + print(right) + ")";
            case SimpleProjection(var expression) -> print(expression);
            case AliasedProjection(var expression, var alias) -> "(as " + print(expression) + " " + print(alias) + ")";
        };
    }

    // [ un elemento por línea ]
    private static <L extends AstNode> String listify(List<L> items) {
        return items.isEmpty() ? "[]" : "[\n" + indent(each(items)) + "\n]";
    }

    // [Movie, Employee] en una sola línea
    private static <L extends AstNode> String inline(List<L> items) {
        return "[" + items.stream().map(PrettyPrinter::print).collect(joining(", ")) + "]";
    }

    // un elemento por línea, sin corchetes (campos opcionales como where o ret)
    private static <L extends AstNode> String each(List<L> items) {
        return items.stream().map(PrettyPrinter::print).collect(joining("\n"));
    }

    private static String block(String name, String... fields) {
        var body = lines(fields);
        return body.isEmpty() ? name + " {}" : name + " {\n" + indent(body) + "\n}";
    }

    // une con saltos de línea, saltando las partes vacías
    private static String lines(String... parts) {
        return Arrays.stream(parts).filter(part -> !part.isEmpty()).collect(joining("\n"));
    }

    private static String indent(String text) {
        return text.lines().map(line -> INDENT + line).collect(joining("\n"));
    }
}