package cr.ac.una.eif400.cyphail.compiler.semantic;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.AstNode;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.parser.grammar.Grammar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Pruebas del analizador semantico")
public class SemanticAnalyzerTest {

    private static List<String> check(String source) {
        AstNode ast = assertOk(Grammar.parse(assertOk(Tokenizer.tokenize(source)).value())).value();
        return SemanticAnalyzer.check(ast);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "MATCH (m:Movie) RETURN m.title, m.year AS year",
            "MATCH (m:Movie) WHERE m.year > 2000 RETURN m",
            "MATCH (m:Movie), (p:Person) WHERE m.year <> p.age RETURN m.title AS title, p.name AS name",
            "MATCH (m:Movie {year: 1999}) RETURN m",
            "MATCH (p:Person), (o:Order {personId: p.id}) RETURN o.total",
            "MATCH (p:Person) CREATE (a:Archive {id: p.id}) RETURN a",
            "MATCH (o:Order) DELETE o",
            "MATCH (a:A) MATCH (b:B) WHERE b.x = a.x RETURN a, b",
            "MATCH (m:Movie) WHERE m.title = 'Matrix' RETURN m",
            "CREATE (n:Person {name: 'Ana'})"
    })
    @DisplayName("Una consulta con todas sus variables declaradas no tiene errores")
    void consultaValida(String source) {
        assertEquals(List.of(), check(source));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en el WHERE")
    void variableNoDeclaradaEnWhere() {
        assertEquals(List.of("Undefined variable 'q' in WHERE"),
                check("MATCH (p:Person) WHERE q.age > 60 RETURN p"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en el RETURN")
    void variableNoDeclaradaEnReturn() {
        assertEquals(List.of("Undefined variable 'x' in RETURN"),
                check("MATCH (p:Person) RETURN x.name"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en el DELETE")
    void variableNoDeclaradaEnDelete() {
        assertEquals(List.of("Undefined variable 'q' in DELETE"),
                check("MATCH (p:Person) DELETE q"));
    }

    @Test
    @DisplayName("Detecta una variable usada en propiedades antes de ser declarada")
    void variableUsadaAntesDeDeclararse() {
        assertEquals(List.of("Undefined variable 'p' in properties of (o)"),
                check("MATCH (o:Order {personId: p.id}), (p:Person) RETURN o"));
    }

    @Test
    @DisplayName("Un nodo no puede usarse a si mismo en sus propiedades")
    void autoreferenciaEnPropiedades() {
        assertEquals(List.of("Undefined variable 'p' in properties of (p)"),
                check("MATCH (p:Person {id: p.id}) RETURN p"));
    }

    @Test
    @DisplayName("Un nodo sin variable se reporta con parentesis vacios")
    void nodoSinVariable() {
        assertEquals(List.of("Undefined variable 'x' in properties of ()"),
                check("MATCH (:Person {id: x.id})"));
    }

    @Test
    @DisplayName("Una variable del CREATE no se puede usar en el mismo CREATE antes de declararse")
    void createUsaVariableNoDeclarada() {
        assertEquals(List.of("Undefined variable 'z' in properties of (a)"),
                check("MATCH (p:Person) CREATE (a:Archive {id: z.id}) RETURN a"));
    }

    @Test
    @DisplayName("El alias del RETURN no cuenta como uso de variable")
    void aliasNoEsUso() {
        assertEquals(List.of(), check("MATCH (m:Movie) RETURN m.title AS nombreQueNoExiste"));
    }

    @Test
    @DisplayName("Un alias no declara una variable nueva")
    void aliasNoDeclara() {
        assertEquals(List.of("Undefined variable 'titulo' in RETURN"),
                check("MATCH (m:Movie) RETURN m.title AS titulo, titulo"));
    }

    @Test
    @DisplayName("Revisa ambos lados de una comparacion")
    void revisaAmbosLados() {
        assertEquals(List.of("Undefined variable 'a' in WHERE", "Undefined variable 'b' in WHERE"),
                check("MATCH (m:Movie) WHERE a.x = b.y RETURN m"));
    }

    @Test
    @DisplayName("Acumula todos los errores en el orden en que aparecen")
    void acumulaTodosLosErrores() {
        assertEquals(
                List.of("Undefined variable 'q' in WHERE", "Undefined variable 'q' in RETURN"),
                check("MATCH (p:Person), (o:Order {personId: p.id, status: \"cancelled\"}) WHERE q.age > 60 RETURN q AS name"));
    }

    @Test
    @DisplayName("Las variables de una revision no se filtran a la siguiente")
    void revisionesIndependientes() {
        check("MATCH (m:Movie) RETURN m");

        assertEquals(List.of("Undefined variable 'm' in RETURN"), check("MATCH (p:Person) RETURN m"));
    }
}
