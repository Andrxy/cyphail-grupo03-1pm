package cr.ac.una.eif400.cyphail.compiler.semantic;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.semantic.records.AnalysisResult;
import cr.ac.una.eif400.cyphail.compiler.semantic.records.Symbol;
import cr.ac.una.eif400.cyphail.compiler.semantic.records.SymbolTable;

import java.util.ArrayList;
import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.semantic.records.Type.*;
import static java.util.stream.Collectors.joining;

public class SemanticAnalyzer {

    private SymbolTable symbols = SymbolTable.create();
    private final List<String> reasons = new ArrayList<>();

    private SemanticAnalyzer() {
    }

    public static AnalysisResult analyze(AstNode query) {
        var checker = new SemanticAnalyzer();
        checker.visit(query);

        return new AnalysisResult(
                query,
                checker.symbols,
                checker.reasons
        );
    }

    public static List<String> check(AstNode query) {
        return analyze(query).diagnostics();
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
                var name = variable.stream()
                        .map(SemanticAnalyzer::name)
                        .collect(joining());

                properties.forEach(property ->
                        use(property, "properties of (" + name + ")"));

                variable.forEach(leaf -> declare(
                        new Symbol(name(leaf), NODE, !labels.isEmpty())
                ));
            }

            case Where(var condition) ->
                    use(condition, "WHERE");

            case Create(var patterns) -> {
                patterns.forEach(this::visit);
                patterns.forEach(this::checkCreatedRelationship);
            }

            case Delete(var _, var targets) ->
                    targets.forEach(target -> use(target, "DELETE"));

            case Return(var _, var projections, var modifiers) -> {
                projections.forEach(projection -> use(projection, "RETURN"));
                projections.forEach(this::declareAlias);
                modifiers.forEach(this::visit);
            }

            case Path(var from, var relationship, var to) -> {
                visit(from);
                visit(relationship);
                visit(to);
            }

            case Node(var items) ->
                    items.forEach(this::visit);

            case SetClause(var items) ->
                    items.forEach(this::visit);

            case SetProperty(var target, var value) -> {
                use(target, "SET target");
                use(value, "SET value");
            }

            case SetAllProperties(var variable, var value) -> {
                use(variable, "SET target");
                use(value, "SET value");
            }

            case SetLabels(var variable, var _) -> {
                use(variable, "SET labels");
                requireNode(variable, "SET");
            }

            case RemoveClause(var items) ->
                    items.forEach(this::visit);

            case RemoveProperty(var target) ->
                    use(target, "REMOVE property");

            case RemoveLabels(var variable, var _) -> {
                use(variable, "REMOVE labels");
                requireNode(variable, "REMOVE");
            }

            case Relationship(var variable, var _, var _, var properties, var _) -> {
                var name = variable.stream()
                        .map(SemanticAnalyzer::name)
                        .collect(joining());

                properties.forEach(property ->
                        use(property, "properties of [" + name + "]"));

                variable.forEach(leaf -> declare(
                        new Symbol(name(leaf), RELATIONSHIP, false)
                ));
            }

            case Order(var items) ->
                    items.forEach(item -> use(item, "ORDER BY"));

            case Skip(var expression) ->
                    use(expression, "SKIP");

            case Limit(var expression) ->
                    use(expression, "LIMIT");

            case Leaf _, Property _, PropertyAccess _, Comparison _,
                 SimpleProjection _, AliasedProjection _, SortItem _,
                 RelRange _, Or _, And _, Not _, Arithmetic _, Unary _,
                 NullTest _, FunctionCall _, LabelTest _ -> {
            }
        }
    }

    /**
     * Registra una variable nueva o valida una nueva aparición
     * de una variable existente.
     */
    private void declare(Symbol incoming) {
        var previous = symbols.lookup(incoming.name());

        if (previous.isEmpty()) {
            symbols = symbols.with(incoming);
            return;
        }

        var existing = previous.get();

        if (existing.type() != incoming.type()) {
            reasons.add(
                    "Variable '%s' cannot be used as both a node and a relationship"
                            .formatted(incoming.name())
            );
            return;
        }

        if (incoming.type() == RELATIONSHIP) {
            reasons.add(
                    "Relationship variable '%s' cannot be repeated in the same query"
                            .formatted(incoming.name())
            );
            return;
        }

        if (incoming.labelsDeclared()) {
            if (existing.labelsDeclared()) {
                reasons.add(
                        "Labels of node variable '%s' can only be declared once"
                                .formatted(incoming.name())
                );
            } else {
                symbols = symbols.with(incoming);
            }
        }
    }

    private void checkCreatedRelationship(AstNode pattern) {
        if (!(pattern instanceof Path(var _, var relationship, var _))) {
            return;
        }

        if (relationship.types().size() != 1) {
            reasons.add(
                    "A relationship created with CREATE must have exactly one type"
            );
        }

        if (relationship.direction() == Relationship.Direction.NONE) {
            reasons.add(
                    "A relationship created with CREATE must have a direction"
            );
        }

        if (!relationship.range().isEmpty()) {
            reasons.add(
                    "A relationship created with CREATE cannot have a length range"
            );
        }
    }

    private void requireNode(Leaf variable, String clause) {
        symbols.lookup(name(variable))
                .filter(symbol -> symbol.type() == RELATIONSHIP)
                .ifPresent(symbol -> reasons.add(
                        "Labels can only be changed with %s on node variables, '%s' is a relationship"
                                .formatted(clause, symbol.name())
                ));
    }

    /**
     * Conserva el comportamiento actual: un alias nuevo queda disponible
     * para los modificadores, sin reemplazar símbolos existentes.
     */
    private void declareAlias(AstNode projection) {
        if (projection instanceof AliasedProjection(var _, var alias)) {
            var aliasName = name(alias);

            if (symbols.lookup(aliasName).isEmpty()) {
                symbols = symbols.with(
                        new Symbol(aliasName, ALIAS, false)
                );
            }
        }
    }

    private void use(AstNode expression, String place) {
        for (var name : variables(expression)) {
            if (symbols.lookup(name).isEmpty()) {
                reasons.add(
                        "Undefined variable '%s' in %s".formatted(name, place)
                );
            }
        }
    }

    private static String name(Leaf leaf) {
        return leaf.token().value();
    }

    private static List<String> variables(AstNode expression) {
        return switch (expression) {
            case Leaf(var token) ->
                    token.type() == TokenType.IDENTIFIER
                            ? List.of(token.value())
                            : List.of();

            case PropertyAccess(var target, var _) ->
                    variables(target);

            case Property(var _, var value) ->
                    variables(value);

            case Comparison(var left, var _, var right) ->
                    variablesOf(List.of(left, right));

            case Arithmetic(var left, var _, var right) ->
                    variablesOf(List.of(left, right));

            case Or(var left, var right) ->
                    variablesOf(List.of(left, right));

            case And(var left, var right) ->
                    variablesOf(List.of(left, right));

            case Not(var inner) ->
                    variables(inner);

            case Unary(var _, var inner) ->
                    variables(inner);

            case NullTest(var inner, var _) ->
                    variables(inner);

            case LabelTest(var inner, var _) ->
                    variables(inner);

            case FunctionCall(var _, var arguments) ->
                    variablesOf(arguments);

            case SimpleProjection(var inner) ->
                    variables(inner);

            case AliasedProjection(var inner, var _) ->
                    variables(inner);

            case SortItem(var inner, var _) ->
                    variables(inner);

            case Node(var items) ->
                    variablesOf(items);

            case Query _, Match _, Where _, Create _, Delete _, Return _,
                 Pattern _, Path _, Relationship _, RelRange _, Order _,
                 Skip _, Limit _, SetClause _, SetProperty _,
                 SetAllProperties _, SetLabels _, RemoveClause _,
                 RemoveProperty _, RemoveLabels _ ->
                    List.of();
        };
    }

    private static List<String> variablesOf(List<? extends AstNode> nodes) {
        return nodes.stream()
                .flatMap(node -> variables(node).stream())
                .toList();
    }
}