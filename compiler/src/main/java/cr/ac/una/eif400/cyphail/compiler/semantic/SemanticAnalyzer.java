package cr.ac.una.eif400.cyphail.compiler.semantic;

import cr.ac.una.eif400.cyphail.compiler.ast.specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * A minimalist Semantic Analyzer for P1, later it will introduce
 * a more accurate one Mapping String with a Symbol and its values
 *
 * @author Anrdrey Solano
 */

public class SemanticAnalyzer {
    private Set<String> defined = new HashSet<>();
    private List<String> reasons = new ArrayList<>();

    private SemanticAnalyzer() {}

    public static List<String> check(AstNode query) {
        var checker = new SemanticAnalyzer();
        checker.visit(query);

        return checker.reasons;
    }

    private void visit(AstNode node) {
        switch (node) {
            case Query(var matches, var updates, var ret) -> {
                matches.forEach(this::visit);
                updates.forEach(this::visit);
                ret.forEach(this::visit);
            }
            case Match(var patterns, var where) -> {
                patterns.forEach(this::visit);
                where.forEach(this::visit);
            }
            case Pattern(var variable, var labels, var properties) -> {
                var name = variable.stream().map(SemanticAnalyzer::name).collect(joining());
                properties.forEach(property -> use(property, "properties of (" + name + ")"));
                variable.forEach(leaf -> defined.add(name(leaf)));
            }
            case Where(var condition) -> use(condition, "WHERE");
            case Create(var patterns) -> patterns.forEach(this::visit);
            case Delete(var targets) -> targets.forEach(target -> use(target, "DELETE"));
            case Return(var projections) -> projections.forEach(projection -> use(projection, "RETURN"));
            case Path(var from, var relationship, var to) -> {
                visit(from);
                visit(relationship);
                visit(to);
            }
            case Node(var items) -> items.forEach(this::visit);
            // las expresiones se revisan completas con use(), no nodo por nodo
            case Leaf _, Property _, PropertyAccess _, Comparison _, SimpleProjection _, AliasedProjection _ -> {}
        }
    }

    private void use(AstNode expression, String place) {
        for (var name : variables(expression))
            if (!defined.contains(name))
                reasons.add("Undefined variable '%s' in %s".formatted(name, place));
    }

    private static String name(Leaf leaf) {
        return leaf.token().value();
    }

    private static List<String> variables(AstNode expression) {
        return switch (expression) {
            case Leaf(var token) -> token.type() == TokenType.IDENTIFIER ? List.of(token.value()) : List.of();
            case PropertyAccess(var variable, var _) -> List.of(name(variable));   // la clave no es variable
            case Property(var _, var value) -> variables(value);                  // la clave no es variable
            case Comparison(var left, var _, var right) ->
                    Stream.concat(variables(left).stream(), variables(right).stream()).toList();
            case SimpleProjection(var inner) -> variables(inner);
            case AliasedProjection(var inner, var _) -> variables(inner);         // el alias no es uso
            case Node(var items) -> items.stream().flatMap(item -> variables(item).stream()).toList();
            case Query _, Match _, Where _, Create _, Delete _, Return _, Pattern _, Path _ -> List.of();
        };
    }
}
