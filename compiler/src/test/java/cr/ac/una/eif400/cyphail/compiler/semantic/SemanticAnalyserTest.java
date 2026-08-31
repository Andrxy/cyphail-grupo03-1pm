package cr.ac.una.eif400.cyphail.compiler.semantic;

import cr.ac.una.eif400.cyphail.compiler.ast.AST;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Pruebas del analisis semantico. En el sprint P1.1 la etapa no rechaza arboles,
 * asi que se verifica que no interrumpa el pipeline.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas del SemanticAnalyser")
public class SemanticAnalyserTest {

    private final SemanticAnalyser semanticAnalyser = new SemanticAnalyser();

    @Test
    @DisplayName("Acepta un AST sin reportar errores")
    void aceptaUnAst() {
        assertDoesNotThrow(() -> semanticAnalyser.analyse(new AST()));
    }

    @Test
    @DisplayName("Analizar dos veces el mismo AST tampoco falla")
    void aceptaElMismoAstDosVeces() {
        AST ast = new AST();

        assertDoesNotThrow(() -> {
            semanticAnalyser.analyse(ast);
            semanticAnalyser.analyse(ast);
        });
    }
}
