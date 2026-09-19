package cr.ac.una.eif400.compiler;

import cr.ac.una.eif400.cyphail.common.QueryResponse;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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


}
