package cr.ac.una.eif400.cyphail.compiler.codegen;

import cr.ac.una.eif400.cyphail.compiler.ast.AST;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pruebas de la generacion de codigo. En el sprint P1.1 la etapa devuelve una
 * cadena simulada, ya que la traduccion a prolog aun no esta implementada.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas del CodeGenerator")
public class CodeGeneratorTest {

    private final CodeGenerator codeGenerator = new CodeGenerator();

    @Test
    @DisplayName("Genera el codigo simulado del sprint")
    void generaElCodigoSimulado() {
        assertEquals("generated", codeGenerator.generate(new AST()));
    }

    @Test
    @DisplayName("Siempre devuelve codigo, nunca nulo")
    void siempreDevuelveCodigo() {
        assertNotNull(codeGenerator.generate(new AST()));
    }
}
