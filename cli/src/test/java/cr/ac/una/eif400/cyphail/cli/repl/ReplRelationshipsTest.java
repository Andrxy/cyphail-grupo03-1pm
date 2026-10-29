package cr.ac.una.eif400.cyphail.cli.repl;

import cr.ac.una.eif400.cyphail.broker.Broker;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del comando {@code .tree} del REPL con relaciones escritas con espacios.
 *
 * @version P2
 */
@DisplayName("Pruebas del Repl con relaciones")
public class ReplRelationshipsTest {

    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream captured;

    private Repl repl;

    @BeforeEach
    void prepararRepl() {
        originalOut = System.out;
        originalIn = System.in;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));

        Broker.getInstance().setCurrentGraph(null);
        repl = new Repl(Compiler.getInstance(), Broker.getInstance());
    }

    @AfterEach
    void restaurarEntorno() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        Broker.getInstance().setCurrentGraph(null);
    }

    @Test
    @DisplayName(".tree acepta una relacion escrita con espacios")
    void treeConRelacionConEspacios() {
        escribir(".tree MATCH (a) -[:amigo]-> (b) RETURN a", ".exit");
        repl.open();

        assertTrue(salida().contains("Relationship: {"), "Dibuja la relacion");
        assertTrue(salida().contains("direction: RIGHT"), "Muestra la direccion");
        assertTrue(salida().contains("Success"), "La consulta es valida");
        assertFalse(salida().contains("Unknown option"), "Picocli no toma la flecha como opcion");
    }

    @Test
    @DisplayName(".tree distingue la direccion entrante")
    void treeConDireccionEntrante() {
        escribir(".tree MATCH (a)<-[r:KNOWS]-(b) RETURN r", ".exit");
        repl.open();

        assertTrue(salida().contains("direction: LEFT"));
    }

    @Test
    @DisplayName(".tree conserva los espacios dobles dentro de un string")
    void treeConservaLosEspaciosDeUnString() {
        escribir(".tree MATCH (a {nombre: 'Bill  Gates'}) RETURN a", ".exit");
        repl.open();

        assertTrue(salida().contains("'Bill  Gates'"));
    }

    @Test
    @DisplayName(".tree reporta un error semantico de relacion repetida")
    void treeConRelacionRepetida() {
        escribir(".tree MATCH (a)-[r]->(b), (c)-[r]->(b) RETURN a", ".exit");
        repl.open();

        assertTrue(salida().contains("Semantic error: Relationship variable 'r' cannot be repeated"));
    }

    @Test
    @DisplayName(".tree muestra SET con una propiedad de relacion")
    void treeConSet() {
        escribir(".tree MATCH (u)-[r:attends]->(c) SET r.grade = 9.5 RETURN r.grade", ".exit");
        repl.open();

        assertTrue(salida().contains("Set: {"));
        assertTrue(salida().contains("(= (. r grade) 9.5)"));
        assertTrue(salida().contains("Success"));
    }

    @Test
    @DisplayName("Los comandos de una palabra siguen funcionando")
    void comandosDeUnaPalabra() {
        escribir(".use Amigos", ".about", ".exit");
        repl.open();

        assertTrue(salida().contains("cyphail (Amigos) >>> "));
        assertTrue(salida().contains("What is cyphail?"));
    }

    private void escribir(String... lineas) {
        String entrada = String.join(System.lineSeparator(), lineas) + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
    }

    private String salida() {
        return captured.toString();
    }
}
