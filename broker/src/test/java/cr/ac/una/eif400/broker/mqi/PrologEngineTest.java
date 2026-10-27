package cr.ac.una.eif400.broker.mqi;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pruebas de integración de PrologEngine: lanzan un swipl real con el engine del proyecto.
 * Si swipl no está instalado se omiten en lugar de fallar.
 *
 * @author Sebastián González
 */
@DisplayName("Pruebas del PrologEngine (necesitan swipl)")
@Timeout(value = 20, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class PrologEngineTest {

    @BeforeAll
    static void requiereSwipl() {
        assumeTrue(swiplInstalado(), "swipl no está en el PATH");
    }

    static boolean swiplInstalado() {
        try {
            Process process = new ProcessBuilder("swipl", "--version").start();
            return process.waitFor() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    static Path engine() {
        String home = System.getProperty("cyphail.home", "..");
        return Paths.get(home, "engine", "engine.pl");
    }

    @Test
    @DisplayName("Arranca, ejecuta un goal y devuelve todas sus soluciones")
    void ejecutaUnGoal() throws IOException {
        try (PrologEngine engine = PrologEngine.start(engine())) {
            PrologResult.Success success = assertInstanceOf(PrologResult.Success.class,
                    engine.run("member(X, [a, b])"));
            assertEquals(List.of(Map.of("X", "a"), Map.of("X", "b")), success.solutions());
        }
    }

    @Test
    @DisplayName("Un goal que falla da Failure y la sesion sigue viva")
    void goalQueFalla() throws IOException {
        try (PrologEngine engine = PrologEngine.start(engine())) {
            assertInstanceOf(PrologResult.Failure.class, engine.run("fail"));
            assertInstanceOf(PrologResult.Success.class, engine.run("true"));
        }
    }

    @Test
    @DisplayName("Un error de sintaxis da PrologError")
    void errorDeSintaxis() throws IOException {
        try (PrologEngine engine = PrologEngine.start(engine())) {
            assertInstanceOf(PrologResult.PrologError.class, engine.run("MATCH (p) RETURN p"));
        }
    }

    @Test
    @DisplayName("Los textos con tildes llegan completos")
    void tildes() throws IOException {
        try (PrologEngine engine = PrologEngine.start(engine())) {
            PrologResult.Success success = (PrologResult.Success) engine.run("X = 'San José Ñandú'");
            assertEquals("San José Ñandú", success.solutions().getFirst().get("X"));
        }
    }

    @Test
    @DisplayName("close termina el proceso y se puede llamar dos veces")
    void closeTerminaElProceso() throws IOException {
        PrologEngine engine = PrologEngine.start(engine());
        assertTrue(engine.isAlive());

        engine.close();
        engine.close();

        assertFalse(engine.isAlive());
        assertThrows(IOException.class, () -> engine.run("true"));
    }

    @Test
    @DisplayName("Un engine que no existe da IOException, no se queda colgado")
    void engineInexistente() {
        assertThrows(IOException.class, () -> PrologEngine.start(Paths.get("no", "existe.pl")));
    }
}
