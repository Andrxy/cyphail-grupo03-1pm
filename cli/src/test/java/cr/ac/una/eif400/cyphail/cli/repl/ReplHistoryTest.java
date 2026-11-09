package cr.ac.una.eif400.cyphail.cli.repl;

import cr.ac.una.eif400.cyphail.broker.Broker;
import cr.ac.una.eif400.cyphail.compiler.Compiler;
import org.jline.keymap.KeyMap;
import org.jline.reader.LineReader;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(10)
class ReplHistoryTest {
    private String up;
    private String down;
    private PipedOutputStream keyboard;
    private Terminal terminal;
    private LineReader reader;

    @BeforeEach
    void prepararTerminal() throws IOException {
        PipedInputStream input = new PipedInputStream();
        keyboard = new PipedOutputStream(input);
        terminal = TerminalBuilder.builder()
                .system(false)
                .type("xterm")
                .streams(input, new ByteArrayOutputStream())
                .build();
        terminal.setSize(new Size(120, 40));
        up = KeyMap.key(terminal, Capability.key_up);
        down = KeyMap.key(terminal, Capability.key_down);
        Repl repl = new Repl(Compiler.getInstance(), Broker.getInstance());
        reader = repl.buildLineReader(terminal);
    }

    @AfterEach
    void cerrarTerminal() throws IOException {
        try {
            if (terminal != null) terminal.close();
        } finally {
            if (keyboard != null) keyboard.close();
        }
    }

    @Test
    void recuperaElBloqueConLineasVaciasYEspacios() throws IOException {
        String query = "MATCH (p:Persona)\n  RETURN p.nombre\n\n;";
        assertEquals(query, escribir(query.replace("\n", "\r") + "\r"));
        assertEquals(query, reader.getHistory().get(0));
        assertEquals(query, escribir(up + "\r"));
    }

    @Test
    void lasFlechasRecorrenLasLineasDelBloqueRecuperado() throws IOException {
        assertEquals(".about", escribir(".about\r"));
        assertEquals("RETURN 1\n\n;", escribir("RETURN 1\r\r;\r"));

        // Recupera el bloque, sube hasta RETURN, edita y baja de nuevo hasta ';'.
        String keys = up + up + up + "\005 + 1" + down + down + "\r";
        assertEquals("RETURN 1 + 1\n\n;", escribir(keys));
    }

    @Test
    void conservaLosComentariosDeLineaAlRecuperarLaConsulta() throws IOException {
        String query = "RETURN 1 // comentario\n;";
        assertEquals(query, escribir("RETURN 1 // comentario\r;\r"));
        assertEquals(query, escribir(up + "\r"));
    }

    private String escribir(String keys) throws IOException {
        reader.runMacro(keys);
        return reader.readLine("cyphail>>> ");
    }
}
