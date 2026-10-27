package cr.ac.una.eif400.cyphail.cli.repl.actions;

import cr.ac.una.eif400.broker.Broker;
import cr.ac.una.eif400.cyphail.cli.repl.Repl;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoadCommandTest extends CommandTestSupport {
    private static final String PERSONAS = "MATCH (p:Persona) RETURN p.nombre, p.edad";
    private static final String TAREAS = "MATCH (t:Tarea) RETURN t.titulo, t.estado";

    @TempDir
    Path directory;

    @Test
    void ejecutaDosConsultasConocidasYPresentaSusTablasEnOrden() throws Exception {
        Path script = script("\uFEFF" + PERSONAS + ";\n" + TAREAS);

        assertEquals(0, ejecutar(".load", script.toString()));
        assertTrue(salida().contains("Ana"), salida());
        assertTrue(salida().contains("Escribir README"), salida());
        assertTrue(salida().indexOf("p.nombre") < salida().indexOf("t.titulo"), salida());
        assertFalse(salida().contains("Error"), salida());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\"", "'"})
    void admiteRutasConEspaciosYComillas(String quote) throws Exception {
        Path script = script(PERSONAS + ";");

        assertEquals(0, ejecutar(".load", quote + script + quote));
        assertTrue(salida().contains("p.nombre"), salida());
    }

    @Test
    void recibeLaRutaEntreComillasDesdeElBucleRealDelRepl() throws Exception {
        Path script = script(TAREAS + ";");
        InputStream original = System.in;
        try {
            String input = ".load \"" + script + "\"\n.exit\n";
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            repl.open();
        } finally {
            System.setIn(original);
        }

        assertTrue(salida().contains("Escribir README"), salida());
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
    @ValueSource(strings = {"RETURN 1", "RETURN 'sin cerrar;", "/* sin cerrar", "RETURN \"sin cerrar;"})
    void informaLosErroresDelScript(String tail) throws Exception {
        Path script = script(PERSONAS + ";\n" + tail);

        assertEquals(1, ejecutar(".load", script.toString()));
        assertTrue(salida().toLowerCase().contains("error"), salida());
    }

    @Test
    void detieneElScriptEnLaPrimeraConsultaQueFalla() throws Exception {
        Path script = script(PERSONAS + ";\nMATCH (n) RETURN q;\n" + TAREAS + ";");

        assertEquals(1, ejecutar(".load", script.toString()));
        assertTrue(salida().contains("Ana"), salida());
        assertTrue(salida().contains("Undefined variable 'q'"), salida());
        assertFalse(salida().contains("Escribir README"), salida());
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
