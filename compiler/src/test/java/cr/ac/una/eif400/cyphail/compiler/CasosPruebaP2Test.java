package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.AstNode;
import cr.ac.una.eif400.cyphail.compiler.codegen.CodeGen;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.parser.grammar.Grammar;
import cr.ac.una.eif400.cyphail.compiler.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas con los casos de prueba unitarios del sprint P2 (documento "Casos de prueba del parser:
 * sprint de relaciones, create, delete separados") y sus validaciones.
 *
 * <p>Cada caso debe pasar el lexer, la gramática y la semántica, y además generar código.</p>
 *
 * @author Sebastián González
 *
 * <p>Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 *
 * @version P2
 */
@DisplayName("Casos de prueba unitarios del sprint P2")
public class CasosPruebaP2Test {

    private static final String CASO_1 = """
            MATCH (p:Person)-[r]->(q:Person)
            RETURN r""";

    private static final String CASO_2 = """
            MATCH (a:Persona)-[:CONOCE]->(b:Persona)
            RETURN a.nombre, b.nombre""";

    private static final String CASO_3_IN = """
            MATCH (e:Empresa)<-[r:TRABAJA_EN]-(p:Persona)
            RETURN p.nombre, e.nombre""";

    private static final String CASO_3_NONE = """
            MATCH (a:Persona)-[r:CONOCE]-(b:Persona)
            RETURN a.nombre, b.nombre""";

    private static final String CASO_4 = """
            MATCH (a:Persona)-[r:CONOCE {desde: 2015}]->(b:Persona)
            RETURN a.nombre, b.nombre, r.desde""";

    private static final String CASO_5 = """
            MATCH (a:Persona)-[r:CONOCE]->(b:Persona)
            WHERE r.desde > 2010 AND b.edad >= 30
            RETURN a.nombre, b.nombre, r.desde""";

    private static final String CASO_6 = """
            MATCH (p:Persona)-[t:TRABAJA_EN {cargo: 'Dev'}]->(e:Empresa)
            WHERE t.desde >= 2018 OR NOT e.pais = 'CR'
            RETURN p.nombre, e.nombre, t.cargo, t.desde""";

    private static final String CASO_7 = """
            MATCH (a:Persona {nombre: 'Ana'}), (b:Persona {nombre: 'Luis'})
            CREATE (a)-[:CONOCE {desde: 2020, nivel: 'amigo'}]->(b)""";

    private static final String CASO_8 = """
            CREATE (a:Persona {nombre: 'Marta'})-[r:TRABAJA_EN {cargo: 'Dev', activo: true}]->(e:Empresa {nombre: 'Acme'})
            RETURN a, r, e""";

    private static final String CASO_9 = """
            MATCH (a:Persona)-[r:CONOCE]->(b:Persona)
            WHERE r.desde < 2000
            DELETE r""";

    private static final String CASO_10 = """
            MATCH (p:Persona {activo: true})-[t:TRABAJA_EN {cargo: 'Dev'}]->(e:Empresa),
                  (q:Persona)-[s:SOCIO_DE]->(e),
                  (p)-[k:CONOCE]-(q)
            WHERE (t.desde >= 2018 OR s.porcentaje > 25.5) AND NOT k.desde = 2020
            RETURN p.nombre, q.nombre, e.nombre, t.cargo, s.porcentaje, k.desde""";

    private static final String CASO_8B = """
            MATCH (p:Persona)-[:TRABAJA_EN]->(e:Empresa),
                  (c:Persona)-[:TRABAJA_EN]->(e)
            WHERE p.nombre <> c.nombre
            RETURN p.nombre, c.nombre, e.nombre""";

    private static final String CASO_8C = """
            MATCH (a)-[r]->(b), (c)-[s]->(d)
            RETURN r, s""";

    /** Los 13 casos del documento. */
    static Stream<Arguments> todosLosCasos() {
        return Stream.of(
                Arguments.of("Caso 1", CASO_1),
                Arguments.of("Caso 2", CASO_2),
                Arguments.of("Caso 3 (<-)", CASO_3_IN),
                Arguments.of("Caso 3 (sin dirección)", CASO_3_NONE),
                Arguments.of("Caso 4", CASO_4),
                Arguments.of("Caso 5", CASO_5),
                Arguments.of("Caso 6", CASO_6),
                Arguments.of("Caso 7", CASO_7),
                Arguments.of("Caso 8", CASO_8),
                Arguments.of("Caso 9", CASO_9),
                Arguments.of("Caso 10", CASO_10),
                Arguments.of("Caso 8b", CASO_8B),
                Arguments.of("Caso 8c", CASO_8C));
    }

    private static AstNode parse(String source) {
        return assertOk(Grammar.parse(assertOk(Tokenizer.tokenize(source)).value())).value();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("todosLosCasos")
    @DisplayName("Cada caso se tokeniza, se parsea y pasa la semantica sin errores")
    void pasaLaSemantica(String nombre, String source) {
        assertEquals(List.of(), SemanticAnalyzer.check(parse(source)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("todosLosCasos")
    @DisplayName(".tree termina en Success para cada caso")
    void treeTerminaEnSuccess(String nombre, String source) {
        assertTrue(Compiler.getInstance().tree(source).endsWith("Success"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("todosLosCasos")
    @DisplayName("Cada caso genera codigo Prolog")
    void generaCodigo(String nombre, String source) {
        String code = assertDoesNotThrow(() -> CodeGen.generate(SemanticAnalyzer.analyze(parse(source))));

        assertTrue(code.startsWith(source.contains("CREATE ") || source.contains("DELETE ")
                ? "cy_exec_update(" : "cy_exec_query("), code);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {CASO_6, CASO_10})
    @DisplayName("El OR se evalua dentro de cy_where para propagar null (casos 6 y 10)")
    void orDentroDeCyWhere(String source) {
        String code = CodeGen.generate(SemanticAnalyzer.analyze(parse(source)));

        assertTrue(code.contains("cy_where("), code);
        assertTrue(code.contains("or(cy_"), code);
    }

    @Test
    @DisplayName("Caso 10: se generan las tres restricciones de unicidad de relaciones")
    void unicidadDeRelacionesEnCaso10() {
        String code = CodeGen.generate(SemanticAnalyzer.analyze(parse(CASO_10)));

        assertTrue(code.contains("cy_not_equal(__T, __S)"), code);
        assertTrue(code.contains("cy_not_equal(__T, __K)"), code);
        assertTrue(code.contains("cy_not_equal(__S, __K)"), code);
    }

    // ---------------------------------------------------------------------------------------
    // Validaciones del documento
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("Validacion: una variable reutilizada con otra etiqueta es error semantico")
    void etiquetaEnConflicto() {
        String source = """
                MATCH (e:Empresa)-[r]->(x), (e:Ciudad)-[s]->(y)
                RETURN e""";

        assertEquals(List.of("Labels of node variable 'e' can only be declared once"),
                SemanticAnalyzer.check(parse(source)));
    }

    @Test
    @DisplayName("Validacion: repetir la misma etiqueta al reutilizar la variable tambien es error")
    void etiquetaRepetida() {
        String source = """
                MATCH (p:Persona)-[r]->(e:Empresa), (q)-[s]->(e:Empresa)
                RETURN p""";

        assertEquals(List.of("Labels of node variable 'e' can only be declared once"),
                SemanticAnalyzer.check(parse(source)));
    }

    @Test
    @DisplayName("Validacion: reutilizar la variable sin etiquetas es valido (caso 8b)")
    void reutilizarSinEtiquetas() {
        assertEquals(List.of(), SemanticAnalyzer.check(parse(CASO_8B)));
    }

    @Test
    @DisplayName("Validacion: la misma relacion no se puede usar dos veces en un MATCH")
    void relacionRepetida() {
        String source = """
                MATCH (a)-[r]->(b), (c)-[r]->(d)
                RETURN r""";

        assertEquals(List.of("Relationship variable 'r' cannot be repeated in the same query"),
                SemanticAnalyzer.check(parse(source)));
    }

    @Test
    @DisplayName("Validacion: los errores llegan al usuario como Semantic error en .tree")
    void errorEnTree() {
        String tree = Compiler.getInstance().tree("MATCH (e:Empresa)-[r]->(x), (e:Ciudad)-[s]->(y) RETURN e");

        assertTrue(tree.endsWith("Semantic error: Labels of node variable 'e' can only be declared once"), tree);
    }
}
