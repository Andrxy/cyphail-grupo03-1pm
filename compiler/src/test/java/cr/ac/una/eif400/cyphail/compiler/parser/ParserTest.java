package cr.ac.una.eif400.cyphail.compiler.parser;

import cr.ac.una.eif400.cyphail.compiler.ast.AST;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Pruebas del parser. En el sprint P1.1 solo se valida que entregue un AST
 * utilizable para las etapas siguientes del pipeline.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
@DisplayName("Pruebas del Parser")
public class ParserTest {

    private final Parser parser = new Parser();

    @Test
    @DisplayName("Devuelve un AST para una secuencia de tokens")
    void devuelveUnAst() {
        assertNotNull(parser.parse("MATCH (p:Persona) RETURN p.nombre"));
    }

    @Test
    @DisplayName("Devuelve un AST aunque no haya tokens")
    void devuelveUnAstSinTokens() {
        assertNotNull(parser.parse(""));
    }

    @Test
    @DisplayName("Cada parseo construye un AST nuevo")
    void cadaParseoConstruyeUnAstNuevo() {
        AST primero = parser.parse("MATCH (t:Tarea) RETURN t.titulo");
        AST segundo = parser.parse("MATCH (t:Tarea) RETURN t.titulo");

        assertNotSame(primero, segundo, "No se reutiliza el arbol de la corrida anterior");
    }
}
