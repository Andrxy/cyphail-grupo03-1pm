package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.*;
import static cr.ac.una.eif400.cyphail.compiler.parser.grammar.GrammarTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas de la jerarquia de expresiones en la gramatica del sprint P2.
 *
 * @version P2
 */
@DisplayName("Pruebas de las expresiones en la gramatica")
public class GrammarExpressionsTest {

    private static Comparison greaterThan(AstNode left, AstNode right) {
        return new Comparison(left, leaf(GREATER_THAN, ">"), right);
    }

    private static Comparison equal(AstNode left, AstNode right) {
        return new Comparison(left, leaf(EQUALS, "="), right);
    }

    @Test
    @DisplayName("AND une dos comparaciones")
    void and() {
        assertEquals(
                new And(greaterThan(access("a", "age"), num("18")), equal(access("a", "age"), access("b", "age"))),
                whereOf("MATCH (a) WHERE a.age > 18 AND a.age = b.age RETURN a"));
    }

    @Test
    @DisplayName("AND liga mas fuerte que OR")
    void andPrecedeAOr() {
        assertEquals(
                new Or(equal(access("a", "x"), num("1")),
                        new And(equal(access("a", "y"), num("2")), equal(access("a", "z"), num("3")))),
                whereOf("MATCH (a) WHERE a.x = 1 OR a.y = 2 AND a.z = 3 RETURN a"));
    }

    @Test
    @DisplayName("Los parentesis cambian la precedencia")
    void parentesis() {
        assertEquals(
                new And(new Or(equal(access("a", "x"), num("1")), equal(access("a", "y"), num("2"))),
                        equal(access("a", "z"), num("3"))),
                whereOf("MATCH (a) WHERE (a.x = 1 OR a.y = 2) AND a.z = 3 RETURN a"));
    }

    @Test
    @DisplayName("Cada NOT envuelve la expresion")
    void not() {
        assertEquals(new Not(new Not(access("a", "active"))),
                whereOf("MATCH (a) WHERE NOT NOT a.active RETURN a"));
    }

    @Test
    @DisplayName("NOT liga mas fuerte que AND")
    void notPrecedeAAnd() {
        assertEquals(new And(new Not(access("a", "x")), access("a", "y")),
                whereOf("MATCH (a) WHERE NOT a.x AND a.y RETURN a"));
    }

    @Test
    @DisplayName("La multiplicacion liga mas fuerte que la suma")
    void precedenciaAritmetica() {
        assertEquals(
                new Arithmetic(access("a", "x"), leaf(PLUS, "+"),
                        new Arithmetic(num("2"), leaf(MULTIPLY, "*"), num("3"))),
                projectionOf("MATCH (a) RETURN a.x + 2 * 3"));
    }

    @Test
    @DisplayName("La resta asocia a la izquierda")
    void restaAsociaALaIzquierda() {
        assertEquals(
                new Arithmetic(new Arithmetic(id("a"), leaf(MINUS, "-"), id("b")), leaf(MINUS, "-"), id("c")),
                projectionOf("MATCH (a) RETURN a - b - c"));
    }

    @Test
    @DisplayName("Un signo unario envuelve el numero")
    void unario() {
        assertEquals(greaterThan(access("a", "x"), new Unary(leaf(MINUS, "-"), num("1"))),
                whereOf("MATCH (a) WHERE a.x > -1 RETURN a"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {"IS NULL|false", "IS NOT NULL|true"})
    @DisplayName("IS NULL e IS NOT NULL producen NullTest")
    void pruebaDeNulo(String test, boolean negated) {
        assertEquals(new NullTest(access("a", "x"), negated), whereOf("MATCH (a) WHERE a.x " + test + " RETURN a"));
    }

    @Test
    @DisplayName("Una llamada a funcion guarda su nombre y sus argumentos")
    void llamadaAFuncion() {
        assertEquals(new FunctionCall(id("type"), List.of(id("r"))), projectionOf("MATCH (r) RETURN type(r)"));
        assertEquals(new FunctionCall(id("substring"), List.of(access("a", "name"), num("1"))),
                projectionOf("MATCH (a) RETURN substring(a.name, 1)"));
    }

    @Test
    @DisplayName("Una etiqueta sobre una variable produce LabelTest")
    void pruebaDeEtiqueta() {
        assertEquals(new LabelTest(id("n"), List.of(id("Person"))), whereOf("MATCH (n) WHERE n:Person RETURN n"));
    }

    @Test
    @DisplayName("TRUE, FALSE y NULL son literales")
    void literales() {
        assertEquals(List.of(
                        new SimpleProjection(leaf(TRUE, "TRUE")),
                        new SimpleProjection(leaf(FALSE, "FALSE")),
                        new SimpleProjection(leaf(NULL, "NULL"))),
                query("MATCH (a) RETURN TRUE, FALSE, NULL").ret().getFirst().projections());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {"<>|NOTEQUALS", "<|LESS_THAN", ">|GREATER_THAN", "<=|LESS_THAN_OR_EQUAL",
            ">=|GREATER_THAN_OR_EQUAL", "=|EQUALS"})
    @DisplayName("Cada operador de comparacion produce su Comparison")
    void operadoresDeComparacion(String operator, String type) {
        assertEquals(new Comparison(access("a", "x"), leaf(valueOf(type), operator), num("1")),
                whereOf("MATCH (a) WHERE a.x " + operator + " 1 RETURN a"));
    }
}
