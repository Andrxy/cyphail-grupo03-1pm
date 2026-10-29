package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.cyphail.broker.Broker;
import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LoadCommandTest extends CommandTestSupport {
    private static final String PRIMERA = "RETURN 'primera' AS valor";
    private static final String SEGUNDA = "RETURN 'segunda' AS otro";

    @TempDir
    Path directory;

    @Test
    void ejecutaDosConsultasYPresentaSusTablasEnOrden() throws Exception {
        requiereSwipl();
        Path script = script("\uFEFF" + PRIMERA + ";\n" + SEGUNDA);

        assertEquals(0, ejecutar(".load", script.toString()));
        assertTrue(salida().contains("primera"), salida());
        assertTrue(salida().contains("segunda"), salida());
        assertTrue(salida().indexOf("valor") < salida().indexOf("otro"), salida());
        assertFalse(salida().contains("Error"), salida());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\"", "'"})
    void admiteRutasConEspaciosYComillas(String quote) throws Exception {
        requiereSwipl();
        Path script = script(PRIMERA + ";");

        assertEquals(0, ejecutar(".load", quote + script + quote));
        assertTrue(salida().contains("primera"), salida());
    }

    @Test
    void recibeLaRutaEntreComillasDesdeElBucleRealDelRepl() throws Exception {
        requiereSwipl();
        Path script = script(SEGUNDA + ";");
        InputStream original = System.in;
        try {
            String input = ".load \"" + script + "\"\n.exit\n";
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            repl.open();
        } finally {
            System.setIn(original);
        }

        assertTrue(salida().contains("segunda"), salida());
        assertFalse(salida().contains("Error"), salida());
        assertTrue(salida().contains("Bye!"));
    }

    @Test
    void entregaElScriptCompletoAlReplSinModificarStringsNiComentarios() throws Exception {
        List<String> queries = capturarConsultas();
        String first = "/* ; */ CREATE (n {name: 'Ana; María'})";
        String second = "MATCH (n) // ;\nRETURN n";
        Path script = script(first + "; " + second + ";");

        assertEquals(0, ejecutar(".load", script.toString()));
        assertEquals(List.of(first + "; " + second + ";"), queries);
    }

    @ParameterizedTest
    @ValueSource(strings = {"RETURN", "RETURN 'sin cerrar;", "/* sin cerrar", "RETURN \"sin cerrar;"})
    void informaLosErroresDelScript(String tail) throws Exception {
        requiereSwipl();
        Path script = script(PRIMERA + ";\n" + tail);

        assertEquals(1, ejecutar(".load", script.toString()));
        assertTrue(salida().toLowerCase().contains("error"), salida());
    }

    @Test
    void detieneElScriptEnLaPrimeraConsultaQueFalla() throws Exception {
        requiereSwipl();
        Path script = script(PRIMERA + ";\nMATCH (n) RETURN q;\n" + SEGUNDA + ";");

        assertEquals(1, ejecutar(".load", script.toString()));
        assertTrue(salida().contains("primera"), salida());
        assertTrue(salida().contains("Undefined variable 'q'"), salida());
        assertFalse(salida().contains("segunda"), salida());
        assertEquals(0, ejecutar(".about"));
        assertTrue(salida().contains("What is cyphail?"));
    }

    @Test
    void informaSiElArchivoNoExiste() {
        assertEquals(1, ejecutar(".load", directory.resolve("missing.cyphail").toString()));
        assertTrue(salida().contains("Cannot read script"), salida());
        assertEquals(0, ejecutar(".about"));
        assertTrue(salida().contains("What is cyphail?"));
    }

    @Test
    void informaSiLaRutaEsUnDirectorio() {
        assertEquals(1, ejecutar(".load", directory.toString()));
        assertTrue(salida().contains("Cannot read script"), salida());
    }

    @Test
    void exigeUnArgumentoDeRuta() {
        assertEquals(1, ejecutar(".load"));
        assertTrue(salida().contains("Missing path"), salida());
    }

    @Test
    void aceptaUnArchivoVacioOConSoloComentarios() throws Exception {
        Path script = script("\uFEFF// comentario ;\n/* otro ; */\n;;;");

        assertEquals(0, ejecutar(".load", script.toString()));
        assertTrue(salida().isBlank(), salida());
    }

    @Test
    void informaSiLaRutaNoEsValida() {
        assertEquals(1, ejecutar(".load", "invalid\0path.cyphail"));
        assertTrue(salida().contains("Cannot read script"), salida());
    }

    /** Solo las pruebas que ejecutan consultas requieren el proceso externo. */
    private void requiereSwipl() throws InterruptedException {
        try {
            assumeTrue(new ProcessBuilder("swipl", "--version").start().waitFor() == 0,
                    "swipl no está disponible");
        } catch (IOException error) {
            assumeTrue(false, "swipl no está en el PATH");
        }
    }

    private Path script(String content) throws Exception {
        return Files.writeString(directory.resolve("consultas con espacios.cyphail"),
                content, StandardCharsets.UTF_8);
    }

    private List<String> capturarConsultas() {
        List<String> queries = new ArrayList<>();
        repl = new Repl(Compiler.getInstance(), Broker.getInstance()) {
            @Override
            public boolean executeQuery(String sourceCode) {
                queries.add(sourceCode);
                return true;
            }
        };
        commandLine = new CommandLine(repl);
        return queries;
    }
}
