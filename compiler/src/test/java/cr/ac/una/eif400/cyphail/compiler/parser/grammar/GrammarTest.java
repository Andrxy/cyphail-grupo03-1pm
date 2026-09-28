package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType;
import cr.ac.una.eif400.cyphail.compiler.parser.input.InputTokens;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertFail;
import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pruebas de la gramatica")
public class GrammarTest {

    private static Result<InputTokens, AstNode, String> parse(String source) {
        return Grammar.parse(assertOk(Tokenizer.tokenize(source)).value());
    }

    private static Query query(String source) {
        return assertInstanceOf(Query.class, assertOk(parse(source)).value());
    }

    private static Leaf leaf(TokenType type, String value) {
        return new Leaf(new TokenString(type, value));
    }

    private static Leaf id(String name) {
        return leaf(IDENTIFIER, name);
    }

    private static Leaf num(String value) {
        return leaf(NUMBER, value);
    }

    private static PropertyAccess access(String variable, String key) {
        return new PropertyAccess(id(variable), id(key));
    }

    private static Pattern node(String variable, String label) {
        return new Pattern(List.of(id(variable)), List.of(id(label)), List.of());
    }

    private static Match match(AstNode... patterns) {
        return new Match(List.of(patterns), List.of());
    }

    private static Return returning(AstNode... projections) {
        return new Return(List.of(projections));
    }

    @Test
    @DisplayName("Construye el AST de un MATCH con RETURN")
    void matchConReturn() {
        Query expected = new Query(
                List.of(match(node("m", "Movie"))),
                List.of(),
                List.of(returning(new SimpleProjection(access("m", "title")))));

        assertEquals(expected, query("MATCH (m:Movie) RETURN m.title"));
    }

    @Test
    @DisplayName("Un patron de nodo puede no tener variable")
    void patronSinVariable() {
        Pattern pattern = (Pattern) query("MATCH (:Movie)").matches().getFirst().patterns().getFirst();

        assertEquals(new Pattern(List.of(), List.of(id("Movie")), List.of()), pattern);
    }

    @Test
    @DisplayName("Un patron de nodo puede no tener etiquetas")
    void patronSinEtiquetas() {
        Pattern pattern = (Pattern) query("MATCH (n) RETURN n").matches().getFirst().patterns().getFirst();

        assertEquals(new Pattern(List.of(id("n")), List.of(), List.of()), pattern);
    }

    @Test
    @DisplayName("Un patron de nodo acepta varias etiquetas y propiedades")
    void patronConEtiquetasYPropiedades() {
        Pattern expected = new Pattern(
                List.of(id("a")),
                List.of(id("Person"), id("Employee")),
                List.of(
                        new Property(id("id"), num("1")),
                        new Property(id("name"), leaf(STRING_LITERAL, "'Ana'"))));

        Pattern pattern = (Pattern) query("MATCH (a:Person:Employee {id: 1, name: 'Ana'}) RETURN a")
                .matches().getFirst().patterns().getFirst();

        assertEquals(expected, pattern);
    }

    @Test
    @DisplayName("Un mapa de propiedades vacio no produce propiedades")
    void propiedadesVacias() {
        Pattern pattern = (Pattern) query("MATCH (n:Movie {}) RETURN n").matches().getFirst().patterns().getFirst();

        assertEquals(List.of(), pattern.properties());
    }

    @Test
    @DisplayName("El valor de una propiedad puede ser un acceso a otra variable")
    void propiedadConAcceso() {
        Pattern pattern = (Pattern) query("MATCH (p:Person), (o:Order {personId: p.id}) RETURN o")
                .matches().getFirst().patterns().get(1);

        assertEquals(List.of(new Property(id("personId"), access("p", "id"))), pattern.properties());
    }

    @Test
    @DisplayName("Un MATCH acepta varios patrones separados por coma")
    void variosPatrones() {
        Match expected = match(node("m", "Movie"), node("p", "Person"));

        assertEquals(expected, query("MATCH (m:Movie), (p:Person) RETURN m").matches().getFirst());
    }

    @Test
    @DisplayName("Un patron entre parentesis extra es el mismo patron")
    void patronEntreParentesis() {
        assertEquals(query("MATCH (m:Movie) RETURN m"), query("MATCH ((m:Movie)) RETURN m"));
    }

    @Test
    @DisplayName("Una consulta puede tener varios MATCH seguidos")
    void variosMatch() {
        Query result = query("MATCH (a:A) MATCH (b:B) RETURN a, b");

        assertEquals(List.of(match(node("a", "A")), match(node("b", "B"))), result.matches());
    }

    @Test
    @DisplayName("El WHERE se guarda como una comparacion dentro del MATCH")
    void whereConComparacion() {
        Where expected = new Where(new Comparison(access("m", "year"), leaf(GREATER_THAN, ">"), num("2000")));

        Match result = query("MATCH (m:Movie) WHERE m.year > 2000 RETURN m").matches().getFirst();

        assertEquals(List.of(expected), result.where());
    }

    @ParameterizedTest(name = "{0} es {1}")
    @CsvSource({
            "=, EQUALS", "<>, NOTEQUALS", "<, LESS_THAN", ">, GREATER_THAN",
            "<=, LESS_THAN_OR_EQUAL", ">=, GREATER_THAN_OR_EQUAL"
    })
    @DisplayName("El WHERE acepta todos los operadores de comparacion")
    void whereConCadaOperador(String operator, TokenType type) {
        Where where = query("MATCH (m:Movie) WHERE m.year " + operator + " p.age RETURN m")
                .matches().getFirst().where().getFirst();

        assertEquals(new Comparison(access("m", "year"), leaf(type, operator), access("p", "age")), where.condition());
    }

    @Test
    @DisplayName("Una expresion sin operador queda como la expresion simple")
    void whereSinComparacion() {
        Where where = query("MATCH (m:Movie) WHERE m.active RETURN m").matches().getFirst().where().getFirst();

        assertEquals(access("m", "active"), where.condition());
    }

    @Test
    @DisplayName("El RETURN acepta varias proyecciones con y sin alias")
    void returnConAlias() {
        Return expected = returning(
                new SimpleProjection(access("m", "title")),
                new AliasedProjection(access("m", "year"), id("year")),
                new SimpleProjection(id("m")));

        assertEquals(List.of(expected), query("MATCH (m:Movie) RETURN m.title, m.year AS year, m").ret());
    }

    @Test
    @DisplayName("El RETURN es opcional")
    void sinReturn() {
        assertEquals(List.of(), query("MATCH (m:Movie)").ret());
    }

    @Test
    @DisplayName("El CREATE se guarda en las clausulas de actualizacion")
    void createDespuesDeMatch() {
        Query result = query("MATCH (p:Person) CREATE (c:Certificate {year: 2026}) RETURN p");

        Create expected = new Create(List.of(new Pattern(
                List.of(id("c")), List.of(id("Certificate")), List.of(new Property(id("year"), num("2026"))))));
        assertEquals(List.of(expected), result.updates());
    }

    @Test
    @DisplayName("Un CREATE puede ir sin MATCH antes")
    void createSinMatch() {
        Query result = query("CREATE (n:Person {name: 'Ana'})");

        assertEquals(List.of(), result.matches());
        assertEquals(1, result.updates().size());
        assertInstanceOf(Create.class, result.updates().getFirst());
    }

    @Test
    @DisplayName("El DELETE acepta varias expresiones separadas por coma")
    void deleteConVariasExpresiones() {
        Query result = query("MATCH (a:A), (b:B) DELETE a, b");

        assertEquals(List.of(new Delete(List.of(id("a"), id("b")))), result.updates());
    }

    @Test
    @DisplayName("El DETACH DELETE produce el mismo nodo que DELETE")
    void detachDelete() {
        assertEquals(query("MATCH (n:A) DELETE n"), query("MATCH (n:A) DETACH DELETE n"));
    }

    @Test
    @DisplayName("Las actualizaciones se guardan en el orden en que aparecen")
    void variasActualizaciones() {
        List<AstNode> updates = query("MATCH (o:Order) CREATE (a:Archive) DELETE o RETURN a").updates();

        assertInstanceOf(Create.class, updates.get(0));
        assertInstanceOf(Delete.class, updates.get(1));
    }

    @ParameterizedTest(name = "\"{0}\" es invalida")
    @ValueSource(strings = {
            "MATCH (m:Movie RETURN m",
            "MATCH m RETURN m",
            "MATCH (m:Movie) RETURN",
            "MATCH (m:Movie) RETURN m,",
            "MATCH (m:Movie) WHERE RETURN m",
            "MATCH (m:Movie) WHERE m.year > 1 > 2 RETURN m",
            "RETURN m MATCH (m:Movie)",
            "MATCH (m:Movie) RETURN m CREATE (n:Movie)",
            "MATCH (m:) RETURN m",
            "MATCH (m {year}) RETURN m",
            "MATCH (m) DELETE",
            "MATCH (a)-[:KNOWS]->(b) RETURN a"
    })
    @DisplayName("Rechaza consultas mal formadas")
    void rechazaConsultasMalFormadas(String source) {
        assertFail(parse(source));
    }

    @Test
    @DisplayName("Si sobran tokens al final indica cual encontro")
    void tokensSobrantes() {
        var failure = assertFail(parse("MATCH (m:Movie) RETURN m )"));

        assertTrue(failure.reason().contains("Expecting end of input. Found RPAREN"), failure.reason());
    }
}
