package cr.ac.una.eif400.cyphail.broker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Timeout(20)
class GraphPersistenceTest {

    @TempDir
    Path stateDirectory;

    @BeforeAll
    static void requiresSwipl() {
        assumeTrue(BrokerEngineTest.swiplInstalado());
    }

    @Test
    void restoresCreateSetAndDeleteAfterRestart() throws IOException, InterruptedException {
        execute("set_current_graph(amigos), "
                + "cy_exec_update(cy_create_node(['PersistTest'], ['value'-42], _), [], [], _)");

        String id = execute("node_prop(amigos, Id, value, 42), write(Id)");

        assertEquals("42", execute("node_prop(amigos, " + id + ", value, Value), write(Value)"));

        execute("set_current_graph(amigos), cy_exec_update(cy_set_node_prop("
                + id + ", value, 99), [], [], _)");

        assertEquals("99", execute("node_prop(amigos, " + id + ", value, Value), write(Value)"));

        execute("set_current_graph(amigos), cy_exec_update(cy_delete_node("
                + id + "), [], [], _)");

        assertEquals("gone", execute("\\+ node(amigos, " + id + "), write(gone)"));
        assertTrue(stateDirectory.resolve("amigos.pl").toFile().isFile());
    }

    @Test
    void rollsBackFailedUpdate() throws IOException, InterruptedException {
        assertEquals("rolled_back", execute("set_current_graph(amigos), "
                + "catch(cy_exec_update((cy_create_node(['FailedTest'], [], _), "
                + "throw(cy_error(failed))), [], [], _), _, true), "
                + "(node_label(amigos, _, 'FailedTest') -> write(leaked) ; write(rolled_back))"));
    }

    @Test
    void restoresRelationshipAndPropertiesAfterRestart() throws IOException, InterruptedException {
        execute("set_current_graph(amigos), "
                + "cy_exec_update(cy_create_edge(ana, luis, 'PersistEdge', ['since'-2026], _), [], [], _)");

        String id = execute("edge_type(amigos, Id, 'PersistEdge'), "
                + "edge_prop(amigos, Id, since, 2026), write(Id)");

        execute("set_current_graph(amigos), cy_exec_update(cy_delete_edge(" + id + "), [], [], _)");

        assertEquals("gone", execute("\\+ edge(amigos, " + id + ", _, _), write(gone)"));
    }

    private String execute(String goal) throws IOException, InterruptedException {
        Path engine = Path.of(System.getProperty("cyphail.home"), "engine", "engine.pl");
        ProcessBuilder builder = new ProcessBuilder("swipl", "-q", "-s", engine.toString(),
                "-g", goal, "-t", "halt").redirectErrorStream(true);
        builder.environment().put("CYPHAIL_STATE_DIR", stateDirectory.toString());
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();
        assertEquals(0, process.waitFor(), output);
        return output;
    }
}
