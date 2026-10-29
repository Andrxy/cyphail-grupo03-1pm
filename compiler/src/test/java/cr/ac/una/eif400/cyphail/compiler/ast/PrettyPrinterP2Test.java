package cr.ac.una.eif400.cyphail.compiler.ast;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.Relationship.Direction;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.grammar.Grammar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del PrettyPrinter con los nodos del sprint P2.
 *
 * @version P2
 */
@DisplayName("Pruebas del PrettyPrinter con los nodos del sprint P2")
public class PrettyPrinterP2Test {

    private static Leaf id(String name) {
        return new Leaf(new TokenString(TokenType.IDENTIFIER, name));
    }

    private static Leaf num(String value) {
        return new Leaf(new TokenString(TokenType.NUMBER, value));
    }

    private static PropertyAccess access(String variable, String key) {
        return new PropertyAccess(id(variable), id(key));
    }

    private static Comparison greaterThan(AstNode left, AstNode right) {
        return new Comparison(left, new Leaf(new TokenString(TokenType.GREATER_THAN, ">")), right);
    }

    private static String printed(String source) {
        return PrettyPrinter.print(assertOk(Grammar.parse(assertOk(Tokenizer.tokenize(source)).value())).value());
    }

    @Test
    @DisplayName("Una relacion muestra variable, tipos, direccion y propiedades")
    void relacion() {
        Relationship relationship =
                new Relationship(List.of(id("r")), List.of(id("KNOWS")), List.of(), List.of(), Direction.RIGHT);

        assertEquals("""
                Relationship: {
                  var: r
                  types: [KNOWS]
                  direction: RIGHT
                  properties: []
                }""", PrettyPrinter.print(relationship));
    }

    @Test
    @DisplayName("Una relacion con rango muestra el rango")
    void relacionConRango() {
        Relationship relationship = new Relationship(List.of(), List.of(id("K")),
                List.of(new RelRange(List.of(num("1")), List.of(num("3")))), List.of(), Direction.LEFT);

        assertEquals("""
                Relationship: {
                  var:\s
                  types: [K]
                  range: *1..3
                  direction: LEFT
                  properties: []
                }""", PrettyPrinter.print(relationship));
    }

    @ParameterizedTest(name = "{2}")
    @CsvSource(delimiter = '|', value = {"||*", "2|2|*2", "2||*2..", "|3|*..3", "1|3|*1..3"})
    @DisplayName("Un rango se escribe como en la consulta")
    void rangos(String min, String max, String expected) {
        RelRange range = new RelRange(
                min == null ? List.of() : List.of(num(min)),
                max == null ? List.of() : List.of(num(max)));

        assertEquals(expected, PrettyPrinter.print(range));
    }

    @Test
    @DisplayName("Un AND se imprime en notacion prefija")
    void and() {
        assertEquals("(AND (> (. a age) 18) (> (. b age) 20))",
                PrettyPrinter.print(new And(
                        greaterThan(access("a", "age"), num("18")),
                        greaterThan(access("b", "age"), num("20")))));
    }

    @Test
    @DisplayName("OR, NOT, IS NULL y las funciones se imprimen en notacion prefija")
    void otrasExpresiones() {
        assertTrue(printed("MATCH (a) WHERE NOT a.x OR a.y IS NULL RETURN a")
                .contains("Expr: (OR (NOT (. a x)) (IS NULL (. a y)))"));
    }

    @Test
    @DisplayName("Un SET muestra sus items")
    void set() {
        assertEquals("""
                Set: {
                  Items: [
                    (= (. p precio) 19.99)
                  ]
                }""", PrettyPrinter.print(new SetClause(List.of(new SetProperty(access("p", "precio"), num("19.99"))))));
    }

    @Test
    @DisplayName("Un REMOVE muestra sus items")
    void remove() {
        assertEquals("""
                Remove: {
                  Items: [
                    (. p discount)
                  ]
                }""", PrettyPrinter.print(new RemoveClause(List.of(new RemoveProperty(access("p", "discount"))))));
    }

    @Test
    @DisplayName("Un DELETE con detach lo indica")
    void deleteConDetach() {
        assertEquals("""
                Delete: {
                  Detach: true
                  Exprs: [
                    a
                  ]
                }""", PrettyPrinter.print(new Delete(true, List.of(id("a")))));
    }

    @Test
    @DisplayName("Un RETURN DISTINCT con LIMIT muestra la marca y los modificadores")
    void returnConModificadores() {
        assertEquals("""
                Return: {
                  Distinct: true
                  Projection: {
                    Items: [
                      m
                    ]
                    Modifiers: [
                      (LIMIT 5)
                    ]
                  }
                }""", PrettyPrinter.print(new Return(true, List.of(new SimpleProjection(id("m"))), List.of(new Limit(num("5"))))));
    }

    @Test
    @DisplayName("ORDER BY muestra la direccion de cada criterio")
    void orderBy() {
        assertEquals("""
                Order: {
                  Items: [
                    (DESC (. m year))
                    (ASC (. m title))
                  ]
                }""", PrettyPrinter.print(new Order(List.of(
                new SortItem(access("m", "year"), true),
                new SortItem(access("m", "title"), false)))));
    }

    @Test
    @DisplayName("Un Path muestra el nodo de origen, la relacion y el nodo de destino")
    void path() {
        String tree = printed("MATCH (a:Person)-[r:KNOWS]->(b:Person) RETURN r");

        assertTrue(tree.contains("Path: {"));
        assertTrue(tree.contains("direction: RIGHT"));
        assertTrue(tree.indexOf("var: a") < tree.indexOf("var: r") && tree.indexOf("var: r") < tree.indexOf("var: b"));
    }
}
