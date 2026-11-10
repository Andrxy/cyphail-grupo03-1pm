package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertFail;
import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.MULTIPLY;
import static cr.ac.una.eif400.cyphail.compiler.parser.grammar.GrammarTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de las clausulas SET, REMOVE y RETURN extendido en la gramatica del sprint P2.
 *
 * @author Alejandro Miranda
 * @version P2
 */
@DisplayName("Pruebas de SET, REMOVE y RETURN en la gramatica")
public class GrammarClausesTest {

    @Test
    @DisplayName("SET asigna una propiedad")
    void setPropiedad() {
        Query result = query("MATCH (p:Product {code: \"PROD-99\"}) SET p.precio = 19.99 RETURN p");

        assertEquals(List.of(new SetClause(List.of(new SetProperty(access("p", "precio"), num("19.99"))))),
                result.updates());
    }

    @Test
    @DisplayName("SET sobre la propiedad de una relacion")
    void setPropiedadDeRelacion() {
        Query result = query("""
                MATCH
                (u:Student {id: "4-666-000"})
                -[r:attends]->
                (c:Course {nrc: "50691"})
                SET r.grade = 9.5
                RETURN u.id, r.grade""");

        assertEquals(List.of(new SetClause(List.of(new SetProperty(access("r", "grade"), num("9.5"))))),
                result.updates());
    }

    @Test
    @DisplayName("SET puede reemplazar todas las propiedades")
    void setTodasLasPropiedades() {
        assertEquals(List.of(new SetClause(List.of(new SetAllProperties(id("n"), id("m"))))),
                query("MATCH (n), (m) SET n = m").updates());
    }

    @Test
    @DisplayName("SET puede agregar etiquetas")
    void setEtiquetas() {
        assertEquals(List.of(new SetClause(List.of(new SetLabels(id("n"), List.of(id("Admin"), id("User")))))),
                query("MATCH (n) SET n:Admin:User").updates());
    }

    @Test
    @DisplayName("SET acepta varios items separados por coma")
    void setVariosItems() {
        assertEquals(List.of(new SetClause(List.of(
                        new SetProperty(access("a", "x"), num("1")),
                        new SetProperty(access("a", "y"), num("2"))))),
                query("MATCH (a) SET a.x = 1, a.y = 2").updates());
    }

    @Test
    @DisplayName("REMOVE quita una propiedad")
    void removePropiedad() {
        Query result = query("MATCH (p:Product {code: \"PROD-99\"}) REMOVE p.discount RETURN p.id");

        assertEquals(List.of(new RemoveClause(List.of(new RemoveProperty(access("p", "discount"))))),
                result.updates());
    }

    @Test
    @DisplayName("REMOVE quita etiquetas")
    void removeEtiquetas() {
        assertEquals(List.of(new RemoveClause(List.of(new RemoveLabels(id("n"), List.of(id("Temp")))))),
                query("MATCH (n) REMOVE n:Temp").updates());
    }

    @Test
    @DisplayName("Un MATCH puede encadenar varias actualizaciones en orden")
    void variasActualizaciones() {
        List<UpdatingClause> updates = query("MATCH (a) SET a.x = 1 REMOVE a.y DELETE a").updates();

        assertEquals(3, updates.size());
        assertEquals(SetClause.class, updates.get(0).getClass());
        assertEquals(RemoveClause.class, updates.get(1).getClass());
        assertEquals(Delete.class, updates.get(2).getClass());
    }

    @Test
    @DisplayName("RETURN DISTINCT activa la marca distinct")
    void returnDistinct() {
        assertTrue(query("MATCH (m) RETURN DISTINCT m").ret().getFirst().distinct());
        assertFalse(query("MATCH (m) RETURN m").ret().getFirst().distinct());
    }

    @Test
    @DisplayName("RETURN * proyecta el asterisco")
    void returnAsterisco() {
        assertEquals(List.of(leaf(MULTIPLY, "*")), query("MATCH (m) RETURN *").ret().getFirst().projections());
    }

    @Test
    @DisplayName("RETURN acepta un alias con AS")
    void returnConAlias() {
        assertEquals(List.of(new AliasedProjection(access("m", "title"), id("t"))),
                query("MATCH (m) RETURN m.title AS t").ret().getFirst().projections());
    }

    @Test
    @DisplayName("RETURN guarda ORDER BY, SKIP y LIMIT en ese orden")
    void modificadores() {
        List<AstNode> expected = List.of(
                new Order(List.of(
                        new SortItem(access("m", "year"), true),
                        new SortItem(access("m", "title"), false))),
                new Skip(num("5")),
                new Limit(num("10")));

        assertEquals(expected,
                query("MATCH (m) RETURN m ORDER BY m.year DESCENDING, m.title SKIP 5 LIMIT 10")
                        .ret().getFirst().modifiers());
    }

    @Test
    @DisplayName("ORDER BY sin direccion es ascendente")
    void ordenAscendentePorDefecto() {
        assertEquals(List.of(new Order(List.of(new SortItem(access("m", "year"), false)))),
                query("MATCH (m) RETURN m ORDER BY m.year").ret().getFirst().modifiers());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "SET a.x = 1 MATCH (a) RETURN a",
            "MATCH (a) SET RETURN a",
            "MATCH (a) SET a.x RETURN a",
            "MATCH (a) REMOVE RETURN a",
            "MATCH (a) RETURN a ORDER BY",
            "MATCH (a) RETURN a LIMIT",
            "MATCH (a) RETURN"
    })
    @DisplayName("Las clausulas mal formadas se rechazan")
    void clausulasInvalidas(String source) {
        assertFail(parse(source));
    }
}
