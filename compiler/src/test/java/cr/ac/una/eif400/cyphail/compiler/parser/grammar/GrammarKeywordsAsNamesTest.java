package cr.ac.una.eif400.cyphail.compiler.parser.grammar;

import cr.ac.una.eif400.cyphail.compiler.ast.node_specializations.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static cr.ac.una.eif400.cyphail.compiler.ResultAssertions.assertFail;
import static cr.ac.una.eif400.cyphail.compiler.lexer.token.TokenType.ORDER;
import static cr.ac.una.eif400.cyphail.compiler.parser.grammar.GrammarTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas de las palabras clave escritas en minuscula y de su uso como nombre de etiqueta, tipo o propiedad.
 *
 * @version P2
 */
@DisplayName("Pruebas de palabras clave usadas como nombres")
public class GrammarKeywordsAsNamesTest {

    @Test
    @DisplayName("Una consulta en minusculas produce el mismo AST que en mayusculas")
    void minusculasIgualQueMayusculas() {
        assertEquals(query("MATCH (a:Person) WHERE a.age > 18 RETURN a.name"),
                query("match (a:Person) where a.age > 18 return a.name"));
    }

    @Test
    @DisplayName("Order sirve como etiqueta de un nodo")
    void palabraClaveComoEtiqueta() {
        Pattern pattern = (Pattern) firstPattern("MATCH (o:Order) RETURN o");

        assertEquals(List.of(leaf(ORDER, "Order")), pattern.labels());
    }

    @Test
    @DisplayName("Una palabra clave sirve como tipo de relacion")
    void palabraClaveComoTipoDeRelacion() {
        Path path = (Path) firstPattern("MATCH (a)-[:SET]->(b) RETURN a");

        assertEquals(1, path.relationship().types().size());
    }

    @Test
    @DisplayName("Una palabra clave sirve como clave de propiedad")
    void palabraClaveComoClaveDePropiedad() {
        query("MATCH (o:Item) WHERE o.order = 1 RETURN o.limit");
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "MATCH (order) RETURN order",
            "MATCH (a) RETURN a AS limit",
            "MATCH (set) RETURN set"
    })
    @DisplayName("Una palabra clave no sirve como variable")
    void palabraClaveComoVariable(String source) {
        assertFail(parse(source));
    }
}
