package cr.ac.una.eif400.cyphail.compiler.codegen;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.AstNode;
import cr.ac.una.eif400.cyphail.compiler.lexer.Tokenizer;
import cr.ac.una.eif400.cyphail.compiler.parser.grammar.Grammar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertOk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas del generador de código. Los goals esperados se verificaron contra el engine
 * ({@code engine/engine.pl}) vía MQI: dan los mismos Rows que los ejemplos de
 * {@code ejemplos-cypher-prolog.md}.<br><br>
 *
 * <b>Sugerido por Claude (Anthropic)</b>, junto con {@link CodeGen}.
 *
 * @author Andrey Solano
 * @author Seb González
 * @version P2
 */
@DisplayName("Pruebas del generador de código")
public class CodeGenTest {

    private static String generate(String source) {
        AstNode ast = assertOk(Grammar.parse(assertOk(Tokenizer.tokenize(source)).value())).value();
        return CodeGen.generate(ast);
    }

    private static Stream<Arguments> ejemplosDelMd() {
        return Stream.of(
                Arguments.of("MATCH (n) RETURN n",
                        "cy_exec_query(cy_node(__N), ['n'], [__N], Rows)"),
                Arguments.of("MATCH (n:Persona) RETURN n",
                        "cy_exec_query((cy_node(__N), cy_node_label(__N, 'Persona')), ['n'], [__N], Rows)"),
                Arguments.of("MATCH (n:Persona) RETURN n.nombre, n.edad",
                        "cy_exec_query((cy_node(__N), cy_node_label(__N, 'Persona'), cy_node_prop(__N, 'nombre', __Nnombre), cy_node_prop(__N, 'edad', __Nedad)), ['n.nombre', 'n.edad'], [__Nnombre, __Nedad], Rows)"),
                Arguments.of("MATCH (n:Persona {nombre: 'Ana'}) RETURN n.edad",
                        "cy_exec_query((cy_node(__N), cy_node_label(__N, 'Persona'), cy_node_prop(__N, 'nombre', 'Ana'), cy_node_prop(__N, 'edad', __Nedad)), ['n.edad'], [__Nedad], Rows)"),
                Arguments.of("MATCH (n:Persona) WHERE n.edad > 30 RETURN n.nombre",
                        "cy_exec_query((cy_node(__N), cy_node_label(__N, 'Persona'), cy_node_prop(__N, 'edad', __Nedad), cy_greater_than(__Nedad, 30), cy_node_prop(__N, 'nombre', __Nnombre)), ['n.nombre'], [__Nnombre], Rows)"),
                Arguments.of("MATCH (n:Persona) WHERE n.nombre <> 'Ana' RETURN n",
                        "cy_exec_query((cy_node(__N), cy_node_label(__N, 'Persona'), cy_node_prop(__N, 'nombre', __Nnombre), cy_not_equal(__Nnombre, 'Ana')), ['n'], [__N], Rows)"),
                Arguments.of("MATCH (n:Persona) WHERE n.edad > 20 AND n.edad < 30 RETURN n",
                        "cy_exec_query((cy_node(__N), cy_node_label(__N, 'Persona'), cy_node_prop(__N, 'edad', __Nedad), (cy_greater_than(__Nedad, 20), cy_less_than(__Nedad, 30))), ['n'], [__N], Rows)"),
                Arguments.of("MATCH (n:Persona) WHERE n.edad < 20 OR n.edad > 30 RETURN n",
                        "cy_exec_query((cy_node(__N), cy_node_label(__N, 'Persona'), cy_node_prop(__N, 'edad', __Nedad), once((cy_less_than(__Nedad, 20) ; cy_greater_than(__Nedad, 30)))), ['n'], [__N], Rows)"),
                Arguments.of("MATCH (n:Persona) WHERE NOT n.edad > 30 RETURN n",
                        "cy_exec_query((cy_node(__N), cy_node_label(__N, 'Persona'), cy_node_prop(__N, 'edad', __Nedad), \\+ cy_greater_than(__Nedad, 30)), ['n'], [__N], Rows)"),
                Arguments.of("MATCH (a)-[r]->(b) RETURN a, b",
                        "cy_exec_query(cy_edge(__R, __A, __B), ['a', 'b'], [__A, __B], Rows)"),
                Arguments.of("MATCH (a)-[r:AMIGO_DE]->(b) RETURN a.nombre, b.nombre",
                        "cy_exec_query((cy_edge(__R, __A, __B), cy_edge_type(__R, 'AMIGO_DE'), cy_node_prop(__A, 'nombre', __Anombre), cy_node_prop(__B, 'nombre', __Bnombre)), ['a.nombre', 'b.nombre'], [__Anombre, __Bnombre], Rows)"),
                Arguments.of("MATCH (a)-[r:AMIGO_DE]->(b) WHERE r.desde >= 2010 RETURN a, b, r.desde",
                        "cy_exec_query((cy_edge(__R, __A, __B), cy_edge_type(__R, 'AMIGO_DE'), cy_edge_prop(__R, 'desde', __Rdesde), cy_greater_or_equal(__Rdesde, 2010)), ['a', 'b', 'r.desde'], [__A, __B, __Rdesde], Rows)"),
                Arguments.of("MATCH (a)<-[r]-(b) RETURN a, b",
                        "cy_exec_query(cy_edge(__R, __B, __A), ['a', 'b'], [__A, __B], Rows)"),
                Arguments.of("MATCH (a)-[r]-(b) RETURN a, b",
                        "cy_exec_query((cy_edge(__R, __A, __B) ; cy_edge(__R, __B, __A)), ['a', 'b'], [__A, __B], Rows)"),
                Arguments.of("RETURN 1",
                        "cy_exec_query(true, ['1'], [1], Rows)"));
    }

    private static Stream<Arguments> otrosCasos() {
        return Stream.of(
                Arguments.of("MATCH (a)-[:AMIGO_DE]->(b), (b)-[:AMIGO_DE]->(a) WHERE a.edad > 18 RETURN a.nombre, b.nombre",
                        "cy_exec_query((cy_edge(__1, __A, __B), cy_edge_type(__1, 'AMIGO_DE'), cy_edge(__2, __B, __A), cy_edge_type(__2, 'AMIGO_DE'), cy_not_equal(__1, __2), cy_node_prop(__A, 'edad', __Aedad), cy_greater_than(__Aedad, 18), cy_node_prop(__A, 'nombre', __Anombre), cy_node_prop(__B, 'nombre', __Bnombre)), ['a.nombre', 'b.nombre'], [__Anombre, __Bnombre], Rows)"),
                Arguments.of("MATCH (a)-[r]->(b) RETURN *",
                        "cy_exec_query(cy_edge(__R, __A, __B), ['a', 'b', 'r'], [__A, __B, __R], Rows)"),
                Arguments.of("MATCH (n:Persona) RETURN n.nombre AS nombre, n.edad + 1 AS siguiente, -n.edad, n.edad % 7",
                        "cy_exec_query((cy_node(__N), cy_node_label(__N, 'Persona'), cy_node_prop(__N, 'nombre', __Nnombre), cy_node_prop(__N, 'edad', __Nedad), __1 is __Nedad + 1, __2 is -(__Nedad), __3 is __Nedad mod 7), ['nombre', 'siguiente', '-n.edad', 'n.edad % 7'], [__Nnombre, __1, __2, __3], Rows)"),
                Arguments.of("MATCH (n) WHERE n.edad IS NULL RETURN n.nombre",
                        "cy_exec_query((cy_node(__N), \\+ cy_node_prop(__N, 'edad', _), cy_node_prop(__N, 'nombre', __Nnombre)), ['n.nombre'], [__Nnombre], Rows)"),
                Arguments.of("MATCH (a)-[r]->(b) RETURN a.nombre, type(r), b.nombre",
                        "cy_exec_query((cy_edge(__R, __A, __B), cy_node_prop(__A, 'nombre', __Anombre), cy_edge_type(__R, __Type), cy_node_prop(__B, 'nombre', __Bnombre)), ['a.nombre', 'type(r)', 'b.nombre'], [__Anombre, __Type, __Bnombre], Rows)"),
                Arguments.of("MATCH (a)-[r:AMIGO_DE|TRABAJA_EN]->(b) WHERE r.desde < 2010 OR b:Empresa RETURN a, r, b",
                        "cy_exec_query((cy_edge(__R, __A, __B), (cy_edge_type(__R, 'AMIGO_DE') ; cy_edge_type(__R, 'TRABAJA_EN')), cy_edge_prop(__R, 'desde', __Rdesde), once((cy_less_than(__Rdesde, 2010) ; cy_node_label(__B, 'Empresa')))), ['a', 'r', 'b'], [__A, __R, __B], Rows)"),
                Arguments.of("MATCH (a), (A) WHERE a.nombre = 'Ana' AND A.nombre = \"Luis\" RETURN a, A",
                        "cy_exec_query((cy_node(__A), cy_node(__1), cy_node_prop(__A, 'nombre', __Anombre), cy_node_prop(__1, 'nombre', __2), (cy_equal(__Anombre, 'Ana'), cy_equal(__2, 'Luis'))), ['a', 'A'], [__A, __1], Rows)"),
                Arguments.of("MATCH (n {nombre: \"O'Brien\"}) RETURN n",
                        "cy_exec_query((cy_node(__N), cy_node_prop(__N, 'nombre', 'O\\'Brien')), ['n'], [__N], Rows)"),
                Arguments.of("MATCH (a)-[:AMIGO_DE*1..3]->(b) RETURN a, b",
                        "cy_exec_query(cy_path(__A, __B, _, [edge_direction(right), bounds(1, 3), edge_types(['AMIGO_DE'])]), ['a', 'b'], [__A, __B], Rows)"),
                Arguments.of("MATCH (a)<-[:AMIGO_DE*..2]-(b) RETURN a.nombre",
                        "cy_exec_query((cy_path(__A, __B, _, [edge_direction(left), bounds(1, 2), edge_types(['AMIGO_DE'])]), cy_node_prop(__A, 'nombre', __Anombre)), ['a.nombre'], [__Anombre], Rows)"),
                Arguments.of("MATCH (a {nombre: 'Ana'})-[*2 {desde: 2015}]-(b) RETURN b",
                        "cy_exec_query((cy_path(__A, __B, _, [edge_direction(none), bounds(2, 2), edge_props(['desde'-2015])]), cy_node_prop(__A, 'nombre', 'Ana')), ['b'], [__B], Rows)"),
                Arguments.of("MATCH (a)-[*]->(b) RETURN a",
                        "cy_exec_query(cy_path(__A, __B, _, [edge_direction(right), bounds(1, infinite)]), ['a'], [__A], Rows)"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("ejemplosDelMd")
    @DisplayName("Transpila los ejemplos 1 a 15 del documento de ejemplos")
    void ejemplosDelDocumento(String source, String expected) {
        assertEquals(expected, generate(source));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("otrosCasos")
    @DisplayName("Transpila relaciones anónimas, RETURN *, alias, aritmética, IS NULL, type() y choques de nombres")
    void otrosCasos(String source, String expected) {
        assertEquals(expected, generate(source));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "CREATE (n:Persona {nombre: 'Ana'})",
            "MATCH (n) RETURN DISTINCT n",
            "MATCH (n) RETURN n ORDER BY n.edad",
            "MATCH (a)-[r*1..3]->(b) RETURN a",
            "MATCH (n) RETURN size(n)",
            "MATCH (n) WHERE n.a.b = 1 RETURN n"
    })
    @DisplayName("Rechaza lo que el engine todavía no resuelve")
    void noSoportado(String source) {
        assertThrows(UnsupportedOperationException.class, () -> generate(source));
    }
}