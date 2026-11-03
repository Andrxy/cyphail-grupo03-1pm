package cr.ac.una.eif400.cyphail.compiler.semantic;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.AstNode;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.parser.grammar.Grammar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas de la semantica estatica de relaciones, SET, REMOVE, ORDER BY y expresiones del sprint P2.
 *
 * @version P2
 */
@DisplayName("Pruebas semanticas del sprint P2")
public class SemanticAnalyzerP2Test {

    private static List<String> check(String source) {
        AstNode ast = assertOk(Grammar.parse(assertOk(Tokenizer.tokenize(source)).value())).value();
        return SemanticAnalyzer.check(ast);
    }

    @Test
    @DisplayName("La variable de una relacion queda declarada")
    void variableDeRelacion() {
        assertEquals(List.of(),
                check("MATCH (a:Person)-[r:KNOWS]->(b:Person) WHERE r.since < 2000 DELETE r"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en las propiedades de una relacion")
    void propiedadesDeRelacion() {
        assertEquals(List.of("Undefined variable 'z' in properties of []"),
                check("MATCH (a)-[:KNOWS {since: z.year}]->(b) RETURN a"));
    }

    @Test
    @DisplayName("Una variable de relacion no se puede repetir")
    void relacionRepetida() {
        assertEquals(List.of("Relationship variable 'r' cannot be repeated in the same query"),
                check("MATCH (a)-[r]->(b), (c)-[r]->(b) RETURN a"));
    }

    @Test
    @DisplayName("Una variable de nodo no se puede usar como relacion")
    void nodoComoRelacion() {
        assertEquals(List.of("Variable 'a' cannot be used as both a node and a relationship"),
                check("MATCH (a:N), (b)-[a]->(c) RETURN b"));
    }

    @Test
    @DisplayName("Una variable de relacion tampoco se puede usar despues como nodo")
    void relacionComoNodo() {
        assertEquals(List.of("Variable 'a' cannot be used as both a node and a relationship"),
                check("MATCH (b)-[a]->(c), (a) RETURN b"));
    }

    @Test
    @DisplayName("Una variable de nodo si se puede repetir para unir patrones")
    void nodoRepetidoEsUnJoin() {
        assertEquals(List.of(), check("MATCH (p)-[:T]->(e), (c)-[:T]->(e) RETURN p"));
    }

    @Test
    @DisplayName("Las relaciones sin variable no chocan entre si")
    void relacionesSinVariable() {
        assertEquals(List.of(), check("MATCH (a)-[:T]->(b), (b)-[:T]->(a) RETURN a"));
    }

    @Test
    @DisplayName("Se acumulan todos los errores de la consulta")
    void acumulaLosErrores() {
        assertEquals(2, check("MATCH (a)-[r]->(b), (c)-[r]->(b), (d)-[a]->(e) RETURN a").size());
    }

    @Test
    @DisplayName("Un CREATE con relacion declara sus variables para el RETURN")
    void createDeclaraLasVariables() {
        assertEquals(List.of(),
                check("CREATE (a:Persona {nombre: 'Marta'})-[r:TRABAJA_EN {rol: 'Dev'}]->(e:Empresa {nombre: 'UCR'}) RETURN a, r, e"));
    }

    @Test
    @DisplayName("Un CREATE sobre nodos de un MATCH no necesita declararlos de nuevo")
    void createSobreNodosExistentes() {
        assertEquals(List.of(), check("MATCH (a:Autor), (b:Libro) CREATE (a)-[:ESCRIBIO]->(b)"));
    }

    @Test
    @DisplayName("Una relacion del CREATE necesita exactamente un tipo y una direccion")
    void createSinTipoNiDireccion() {
        assertEquals(List.of(
                        "A relationship created with CREATE must have exactly one type",
                        "A relationship created with CREATE must have a direction"),
                check("MATCH (a), (b) CREATE (a)-[r]-(b)"));
    }

    @Test
    @DisplayName("Las etiquetas solo se cambian en variables de nodo")
    void etiquetasEnRelacion() {
        assertEquals(List.of(
                        "Labels can only be changed with SET on node variables, 'r' is a relationship",
                        "Labels can only be changed with REMOVE on node variables, 'r' is a relationship"),
                check("MATCH (a)-[r]->(b) SET r:X REMOVE r:Y"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en SET")
    void setNoDeclarada() {
        assertEquals(List.of("Undefined variable 'q' in SET target"),
                check("MATCH (p:Person) SET q.age = 1"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en el valor de SET")
    void setValorNoDeclarado() {
        assertEquals(List.of("Undefined variable 'q' in SET value"),
                check("MATCH (p:Person) SET p.age = q.age"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en las etiquetas de SET")
    void setEtiquetasNoDeclaradas() {
        assertEquals(List.of("Undefined variable 'q' in SET labels"),
                check("MATCH (p:Person) SET q:Admin"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en REMOVE")
    void removeNoDeclarada() {
        assertEquals(List.of("Undefined variable 'q' in REMOVE property"),
                check("MATCH (p:Person) REMOVE q.age"));
    }

    @Test
    @DisplayName("SET y REMOVE sobre variables declaradas son validos")
    void setYRemoveValidos() {
        assertEquals(List.of(), check("MATCH (p:Product {code: \"PROD-99\"}) SET p.precio = 19.99 REMOVE p.discount RETURN p.id"));
    }

    @Test
    @DisplayName("Un alias del RETURN se puede usar en ORDER BY")
    void aliasEnOrderBy() {
        assertEquals(List.of(), check("MATCH (m:Movie) RETURN m.year AS y ORDER BY y"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en ORDER BY")
    void orderByNoDeclarada() {
        assertEquals(List.of("Undefined variable 'q' in ORDER BY"),
                check("MATCH (m:Movie) RETURN m ORDER BY q"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada dentro de un AND del WHERE")
    void andConVariableNoDeclarada() {
        assertEquals(List.of("Undefined variable 'b' in WHERE"),
                check("MATCH (a) WHERE a.x > 1 AND b.y = 2 RETURN a"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en los argumentos de una funcion")
    void funcionConVariableNoDeclarada() {
        assertEquals(List.of("Undefined variable 'q' in RETURN"),
                check("MATCH (a) RETURN type(q)"));
    }

    @Test
    @DisplayName("Detecta una variable no declarada en LIMIT")
    void limitConVariableNoDeclarada() {
        assertEquals(List.of("Undefined variable 'n' in LIMIT"),
                check("MATCH (a) RETURN a LIMIT n"));
    }
}
