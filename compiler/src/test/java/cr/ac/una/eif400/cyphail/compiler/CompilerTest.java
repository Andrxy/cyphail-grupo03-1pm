package cr.ac.una.eif400.cyphail.compiler;

import cr.ac.una.eif400.cyphail.common.QueryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Andrey Solano
 */
public class CompilerTest {
    private Compiler compiler;

    @BeforeEach
    void setUp() {
        compiler = Compiler.getInstance();
    }

    @Test
    @DisplayName("Debe retornar siempre la misma instancia")
    void testGetInstance() {
        Compiler instance1 = Compiler.getInstance();
        Compiler instance2 = Compiler.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2, "Compiler debe implementar correctamente el patrón Singleton");
    }

    @Test
    @DisplayName("Debe procesar exitosamente una consulta fuente válida a través del pipeline")
    void testCompileValidQuery() {
        String validSource = "MATCH (p:Persona) RETURN p.nombre, p.edad";

        QueryResponse response = compiler.compile(validSource);

        assertNotNull(response, "La respuesta de la compilación no debe ser nula");
    }

    @Test
    @DisplayName("Un error lexico no llega al broker")
    void compileConErrorLexico() {
        QueryResponse response = compiler.compile("MATCH (p:Persona) RETURN p @");

        assertFalse(response.isOk());
        assertTrue(response.getMessage().startsWith("Lexical Error"), response.getMessage());
        assertNull(response.getHeaders());
        assertNull(response.getRows());
    }

    @Test
    @DisplayName("Un error sintactico se reporta con el prefijo Syntax error")
    void compileConErrorSintactico() {
        QueryResponse response = compiler.compile("MATCH (p:Persona RETURN p");

        assertFalse(response.isOk());
        assertTrue(response.getMessage().startsWith("Syntax error: "), response.getMessage());
    }

    @Test
    @DisplayName("Un error semantico se reporta con el prefijo Semantic error")
    void compileConErrorSemantico() {
        QueryResponse response = compiler.compile("MATCH (p:Persona) RETURN q.nombre");

        assertFalse(response.isOk());
        assertEquals("Semantic error: Undefined variable 'q' in RETURN", response.getMessage());
    }

    @Test
    @DisplayName("Varios errores semanticos se unen con punto y coma")
    void compileConVariosErroresSemanticos() {
        QueryResponse response = compiler.compile("MATCH (p:Persona) WHERE a.x > 1 RETURN b");

        assertEquals("Semantic error: Undefined variable 'a' in WHERE; Undefined variable 'b' in RETURN",
                response.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "CREATE (n:Persona)",
            "MATCH (n) RETURN DISTINCT n",
            "MATCH (n) RETURN size(n)"
    })
    void devuelveUnErrorSiCodegenNoSoportaLaConsulta(String source) {
        QueryResponse response = compiler.compile(source);

        assertFalse(response.isOk());
        assertTrue(response.getMessage().startsWith("Code generation error:"), response.getMessage());
        assertTrue(response.getMessage().contains("Not supported yet"), response.getMessage());
        assertNull(response.getHeaders());
        assertNull(response.getRows());
    }

    @Test
    @DisplayName("El tree de una consulta valida muestra el arbol y termina en Success")
    void treeDeConsultaValida() {
        String tree = compiler.tree("MATCH (m:Movie) RETURN m.title");

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
                  Updates: []
                  Return: {
                    Distinct: false
                    Projection: {
                      Items: [
                        (. m title)
                      ]
                      Modifiers: []
                    }
                  }
                }
                Success""", tree);
    }

    @Test
    @DisplayName("El tree muestra el arbol aunque haya error semantico")
    void treeConErrorSemantico() {
        String tree = compiler.tree("MATCH (m:Movie) RETURN q");

        assertTrue(tree.startsWith("Query {"), tree);
        assertTrue(tree.endsWith("\nSemantic error: Undefined variable 'q' in RETURN"), tree);
    }

    @Test
    @DisplayName("El tree con error sintactico muestra solo el error")
    void treeConErrorSintactico() {
        String tree = compiler.tree("MATCH (m:Movie RETURN m");

        assertTrue(tree.startsWith("Syntax error: "), tree);
        assertFalse(tree.contains("Query {"), tree);
    }

    @Test
    @DisplayName("El tree con error lexico muestra solo el error")
    void treeConErrorLexico() {
        assertEquals("Lexical Error: Unexpected character at index 15", compiler.tree("MATCH (m:Movie) # RETURN m"));
    }

    @ParameterizedTest(name = "{0} es valido")
    @ValueSource(strings = {"c01", "c02", "c03", "c04", "c05", "c06", "c07", "c08", "c09"})
    @DisplayName("Los casos validos de resources producen su arbol sin errores")
    void casosValidos(String caso) throws IOException {
        String tree = compiler.tree(leerCaso(caso));

        assertTrue(tree.endsWith("\nSuccess"), tree);
    }

    @ParameterizedTest(name = "{0} tiene error semantico")
    @ValueSource(strings = {"c10", "c11"})
    @DisplayName("Los casos con variables no declaradas producen error semantico")
    void casosConErrorSemantico(String caso) throws IOException {
        String tree = compiler.tree(leerCaso(caso));

        assertTrue(tree.contains("\nSemantic error: Undefined variable"), tree);
    }

    private static String leerCaso(String caso) throws IOException {
        try (InputStream stream = CompilerTest.class.getResourceAsStream("/casos/" + caso + ".cyphail")) {
            assertNotNull(stream, "No existe el caso " + caso);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
