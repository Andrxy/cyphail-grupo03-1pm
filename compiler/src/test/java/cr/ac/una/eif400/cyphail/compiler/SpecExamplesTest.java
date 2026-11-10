package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.AstNode;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenString;
import cr.ac.una.eif400.cyphail.compiler.parser.Statements;
import cr.ac.una.eif400.cyphail.compiler.parser.grammar.Grammar;
import cr.ac.una.eif400.cyphail.compiler.parser.result.Ok;
import cr.ac.una.eif400.cyphail.compiler.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del front-end (lexer, gramatica y semantica) con los ejemplos del SPEC del sprint P2.
 *
 * @author Alejandro Miranda
 * @version P2
 */
@DisplayName("Pruebas con los ejemplos del SPEC de P2")
public class SpecExamplesTest {

    private static final String TRABAJA_EN = """
            MATCH (p:Persona)-[:TRABAJA_EN]->(e:Empresa),
                  (c:Persona)-[:TRABAJA_EN]->(e)
            WHERE p.nombre <> c.nombre
            RETURN p.nombre, c.nombre, e.nombre""";

    private static final String ESCRIBIO = """
            MATCH (a:Autor {nombre: 'Bill Gates'}),
                  (b:Libro {titulo: 'Fake Windows Millenium'})
            CREATE (a)-[:ESCRIBIO]->(b)""";

    private static final String MARTA = """
            CREATE (a:Persona {nombre: 'Marta'})
                -[r:TRABAJA_EN {rol: 'Dev'}]->(e:Empresa
                                            {nombre: 'UCR'})
            RETURN a, r, e""";

    private static final String BORRAR = """
            MATCH (a:Person:Female)-[r:KNOWS]->(b:Person:Male)
            WHERE r.since < 2000
            DELETE r""";

    private static final String PRECIO = """
            MATCH (p:Product {code: "PROD-99"})
            SET p.precio = 19.99
            RETURN p""";

    private static final String DESCUENTO = """
            MATCH (p:Product {code: "PROD-99"})
            REMOVE p.discount
            RETURN p.id""";

    private static final String NOTA = """
            MATCH
            (u:Student {id: "4-666-000"})
            -[r:attends]->
            (c:Course {nrc: "50691"})
            SET r.grade = 9.5
            RETURN u.id, r.grade""";

    private static final String AMIGOS = """
            MATCH (a) -[:amigo]-> (b),
                  (b) -[:amigo]-> (a)
            WHERE a.age > 18 AND a.age = b.age
            RETURN a.name, b.name""";

    static Stream<String> ejemplos() {
        return Stream.of(TRABAJA_EN, ESCRIBIO, MARTA, BORRAR, PRECIO, DESCUENTO, NOTA, AMIGOS);
    }

    private static AstNode parse(List<TokenString> tokens) {
        return assertOk(Grammar.parse(tokens)).value();
    }

    @ParameterizedTest(name = "Ejemplo {index}")
    @MethodSource("ejemplos")
    @DisplayName("Cada ejemplo del SPEC se tokeniza, se parsea y pasa la semantica")
    void ejemploValido(String source) {
        AstNode ast = parse(assertOk(Tokenizer.tokenize(source)).value());

        assertEquals(List.of(), SemanticAnalyzer.check(ast));
    }

    @ParameterizedTest(name = "Ejemplo {index}")
    @MethodSource("ejemplos")
    @DisplayName(".tree termina en Success para cada ejemplo del SPEC")
    void treeTerminaEnSuccess(String source) {
        assertTrue(Compiler.getInstance().tree(source).endsWith("Success"));
    }

    @Test
    @DisplayName("Un script con todos los ejemplos separados por punto y coma se parte en 8 sentencias validas")
    void scriptCompleto() {
        String script = String.join(";\n// separador\n", ejemplos().toList()) + ";";

        List<List<TokenString>> statements = Statements.split(assertOk(Tokenizer.tokenize(script)).value());

        assertEquals(8, statements.size());
        statements.forEach(tokens -> assertEquals(List.of(), SemanticAnalyzer.check(parse(tokens))));
    }

    @Test
    @DisplayName("La gramatica no acepta el punto y coma: lo resuelve Statements")
    void puntoYComaLoResuelveStatements() {
        List<TokenString> tokens = assertOk(Tokenizer.tokenize("MATCH (a) RETURN a;")).value();

        assertFalse(Grammar.parse(tokens) instanceof Ok);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "MATCH (a)-[r]->(b), (c)-[r]->(b) RETURN a",
            "MATCH (a:N), (b)-[a]->(c) RETURN b"
    })
    @DisplayName("Las reglas semanticas de relaciones del SPEC se rechazan al compilar")
    void reglasSemanticas(String source) {
        QueryResponse response = Compiler.getInstance().compile(source);

        assertFalse(response.isOk());
        assertTrue(response.getMessage().contains("Semantic error"));
    }

    @Test
    @DisplayName("Una variable que empieza con dos guiones bajos se rechaza al compilar")
    void variableReservada() {
        QueryResponse response = Compiler.getInstance().compile("MATCH (__person) RETURN __person");

        assertFalse(response.isOk());
        assertTrue(response.getMessage().contains("Lexical Error"));
    }
}
