package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.Relationship.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertFail;
import static cr.ac.una.eif400.cyphail.compiler.parser.grammar.GrammarTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Pruebas de los patrones de relaciones en la gramatica del sprint P2.
 *
 * @version P2
 */
@DisplayName("Pruebas de las relaciones en la gramatica")
public class GrammarRelationshipsTest {

    private static Relationship relationshipOf(String source) {
        return assertInstanceOf(Path.class, firstPattern(source)).relationship();
    }

    private static List<Leaf> bound(String value) {
        return value == null ? List.of() : List.of(num(value));
    }

    @Test
    @DisplayName("Una relacion saliente con variable y tipo une dos nodos en un Path")
    void relacionSaliente() {
        Path expected = new Path(
                new Pattern(List.of(id("a")), List.of(id("Person")), List.of()),
                new Relationship(List.of(id("r")), List.of(id("KNOWS")), List.of(), List.of(), Direction.RIGHT),
                new Pattern(List.of(id("b")), List.of(id("Person")), List.of()));

        assertEquals(expected, firstPattern("MATCH (a:Person)-[r:KNOWS]->(b:Person) RETURN r"));
    }

    @ParameterizedTest(name = "{0} es {1}")
    @CsvSource(delimiter = '|', value = {
            "MATCH (a)-[:KNOWS]->(b) RETURN a | RIGHT",
            "MATCH (a)-[:KNOWS]-(b) RETURN a | NONE",
            "MATCH (a)<-[:KNOWS]-(b) RETURN a | LEFT",
            "MATCH (a)-->(b) RETURN a | RIGHT",
            "MATCH (a)--(b) RETURN a | NONE",
            "MATCH (a)<--(b) RETURN a | LEFT"
    })
    @DisplayName("La direccion sale de las flechas escritas")
    void direccion(String source, Direction direction) {
        assertEquals(direction, relationshipOf(source).direction());
    }

    @Test
    @DisplayName("Una relacion sin corchetes no tiene variable, tipos ni propiedades")
    void sinDetalle() {
        assertEquals(new Relationship(List.of(), List.of(), List.of(), List.of(), Direction.RIGHT),
                relationshipOf("MATCH (a)-->(b) RETURN a"));
    }

    @Test
    @DisplayName("Una relacion con corchetes vacios tampoco tiene detalle")
    void corchetesVacios() {
        assertEquals(new Relationship(List.of(), List.of(), List.of(), List.of(), Direction.NONE),
                relationshipOf("MATCH (a)-[]-(b) RETURN a"));
    }

    @Test
    @DisplayName("Una relacion acepta varios tipos alternativos")
    void tiposAlternativos() {
        assertEquals(List.of(id("KNOWS"), id("LIKES")),
                relationshipOf("MATCH (a)-[:KNOWS|LIKES]->(b) RETURN a").types());
    }

    @Test
    @DisplayName("Una relacion acepta propiedades")
    void propiedades() {
        assertEquals(List.of(new Property(id("since"), num("1999"))),
                relationshipOf("MATCH (a)-[r:KNOWS {since: 1999}]->(b) RETURN a").properties());
    }

    @Test
    @DisplayName("Una relacion con espacios entre los simbolos se lee igual")
    void conEspacios() {
        assertEquals(relationshipOf("MATCH (a)-[:amigo]->(b) RETURN a"),
                relationshipOf("MATCH (a) -[ :amigo ]-> (b) RETURN a"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {"*||", "*2|2|2", "*1..3|1|3", "*..3||3", "*2..|2|"})
    @DisplayName("El rango de longitud guarda su minimo y su maximo")
    void rango(String range, String min, String max) {
        List<AstNode> expected = List.of(new RelRange(bound(min), bound(max)));

        assertEquals(expected, relationshipOf("MATCH (a)-[:KNOWS" + range + "]->(b) RETURN a").range());
    }

    @Test
    @DisplayName("Un CREATE acepta una relacion con propiedades en la relacion y en los nodos")
    void createConRelacion() {
        Query result = query("""
                CREATE (a:Persona {nombre: 'Marta'})
                    -[r:TRABAJA_EN {rol: 'Dev'}]->(e:Empresa
                                                {nombre: 'UCR'})
                RETURN a, r, e""");

        Create create = assertInstanceOf(Create.class, result.updates().getFirst());
        Path path = assertInstanceOf(Path.class, create.patterns().getFirst());
        assertEquals(new Relationship(List.of(id("r")), List.of(id("TRABAJA_EN")), List.of(),
                List.of(new Property(id("rol"), text("'Dev'"))), Direction.RIGHT), path.relationship());
    }

    @Test
    @DisplayName("Un CREATE puede unir dos nodos ya ligados por un MATCH")
    void createEntreNodosExistentes() {
        Query result = query("""
                MATCH (a:Autor {nombre: 'Bill Gates'}),
                      (b:Libro {titulo: 'Fake Windows Millenium'})
                CREATE (a)-[:ESCRIBIO]->(b)""");

        Create create = assertInstanceOf(Create.class, result.updates().getFirst());
        Path path = assertInstanceOf(Path.class, create.patterns().getFirst());
        assertEquals(List.of(id("ESCRIBIO")), path.relationship().types());
        assertEquals(2, result.matches().getFirst().patterns().size());
    }

    @Test
    @DisplayName("Dos relaciones en un MATCH pueden compartir un nodo")
    void relacionesQueComparten() {
        List<AstNode> patterns =
                query("MATCH (p)-[:T]->(e), (c)-[:T]->(e) RETURN p").matches().getFirst().patterns();

        assertEquals(2, patterns.size());
        assertInstanceOf(Path.class, patterns.get(0));
        assertInstanceOf(Path.class, patterns.get(1));
    }

    @Test
    @DisplayName("Se puede borrar una relacion por su variable")
    void borrarRelacion() {
        Query result = query("MATCH (a:Person:Female)-[r:KNOWS]->(b:Person:Male) WHERE r.since < 2000 DELETE r");

        assertEquals(List.of(new Delete(false, List.of(id("r")))), result.updates());
    }

    @Test
    @DisplayName("DETACH DELETE activa la marca detach")
    void detachDelete() {
        Query result = query("MATCH (a)-[r]->(b) DETACH DELETE a");

        assertEquals(List.of(new Delete(true, List.of(id("a")))), result.updates());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "MATCH (a)<-[:K]->(b) RETURN a",
            "MATCH (a)-[:K]-> RETURN a",
            "MATCH (a)-[:K](b) RETURN a",
            "MATCH (a)-[:K->(b) RETURN a",
            "MATCH (a)-[:]->(b) RETURN a",
            "MATCH (a)-[:K|]->(b) RETURN a",
            "MATCH (a)-[:K {since: }]->(b) RETURN a"
    })
    @DisplayName("Las relaciones mal formadas se rechazan")
    void relacionesInvalidas(String source) {
        assertFail(parse(source));
    }
}
