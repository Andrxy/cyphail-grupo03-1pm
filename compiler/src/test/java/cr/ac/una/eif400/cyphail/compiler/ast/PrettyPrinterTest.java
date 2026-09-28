package cr.ac.una.eif400.cyphail.compiler.ast;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alejandro Miranda
 */
@DisplayName("Pruebas del PrettyPrinter")
public class PrettyPrinterTest {

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

    @Test
    @DisplayName("Una hoja se imprime como el texto de su token")
    void hoja() {
        assertEquals("Movie", PrettyPrinter.print(id("Movie")));
    }

    @Test
    @DisplayName("Un acceso a propiedad se imprime en notacion prefija")
    void accesoAPropiedad() {
        assertEquals("(. m title)", PrettyPrinter.print(access("m", "title")));
    }

    @Test
    @DisplayName("Una comparacion se imprime con el operador primero")
    void comparacion() {
        assertEquals("(> (. m year) 2000)", PrettyPrinter.print(greaterThan(access("m", "year"), num("2000"))));
    }

    @Test
    @DisplayName("Una proyeccion simple se imprime como su expresion")
    void proyeccionSimple() {
        assertEquals("(. m title)", PrettyPrinter.print(new SimpleProjection(access("m", "title"))));
    }

    @Test
    @DisplayName("Una proyeccion con alias se imprime con as")
    void proyeccionConAlias() {
        assertEquals("(as (. m year) year)", PrettyPrinter.print(new AliasedProjection(access("m", "year"), id("year"))));
    }

    @Test
    @DisplayName("Una propiedad se imprime como clave: valor")
    void propiedad() {
        assertEquals("id: 1", PrettyPrinter.print(new Property(id("id"), num("1"))));
    }

    @Test
    @DisplayName("Un Node vacio se imprime como corchetes vacios")
    void nodeVacio() {
        assertEquals("[]", PrettyPrinter.print(new Node(List.of())));
    }

    @Test
    @DisplayName("Un Node se imprime con un elemento indentado por linea")
    void nodeConElementos() {
        assertEquals("""
                [
                  a
                  b
                ]""", PrettyPrinter.print(new Node(List.of(id("a"), id("b")))));
    }

    @Test
    @DisplayName("Un patron de nodo muestra variable, etiquetas en linea y propiedades")
    void patronDeNodo() {
        Pattern pattern = new Pattern(
                List.of(id("a")),
                List.of(id("Person"), id("Employee")),
                List.of(new Property(id("id"), num("1"))));

        assertEquals("""
                PatternNode: {
                  var: a
                  labels: [Person, Employee]
                  properties: [
                    id: 1
                  ]
                }""", PrettyPrinter.print(pattern));
    }

    @Test
    @DisplayName("Un WHERE muestra su expresion")
    void where() {
        assertEquals("""
                Where: {
                  Expr: (> (. m year) 2000)
                }""", PrettyPrinter.print(new Where(greaterThan(access("m", "year"), num("2000")))));
    }

    @Test
    @DisplayName("Un DELETE muestra sus expresiones")
    void delete() {
        assertEquals("""
                Delete: {
                  Exprs: [
                    o
                  ]
                }""", PrettyPrinter.print(new Delete(List.of(id("o")))));
    }

    @Test
    @DisplayName("Un CREATE muestra sus patrones")
    void create() {
        Pattern pattern = new Pattern(List.of(id("c")), List.of(id("Certificate")), List.of());

        assertEquals("""
                Create: {
                  Patterns: [
                    PatternNode: {
                      var: c
                      labels: [Certificate]
                      properties: []
                    }
                  ]
                }""", PrettyPrinter.print(new Create(List.of(pattern))));
    }

    @Test
    @DisplayName("Imprime una consulta completa con MATCH, WHERE y RETURN")
    void consultaCompleta() {
        Query query = new Query(
                List.of(new Match(
                        List.of(new Pattern(List.of(id("m")), List.of(id("Movie")), List.of())),
                        List.of(new Where(greaterThan(access("m", "year"), num("2000")))))),
                List.of(),
                List.of(new Return(List.of(
                        new SimpleProjection(access("m", "title")),
                        new AliasedProjection(access("m", "year"), id("year"))))));

        assertEquals("""
                Query {
                  Match: {
                    Patterns: [
                      PatternNode: {
                        var: m
                        labels: [Movie]
                        properties: []
                      }
                    ]
                  }
                  Where: {
                    Expr: (> (. m year) 2000)
                  }
                  Updates: []
                  Return: {
                    Projection: {
                      Items: [
                        (. m title)
                        (as (. m year) year)
                      ]
                      Modifiers: []
                    }
                  }
                }""", PrettyPrinter.print(query));
    }
}
