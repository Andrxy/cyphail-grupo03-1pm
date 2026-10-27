package cr.ac.una.eif400.broker.mqi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas del protocolo de MqiClient contra un servidor MQI falso, sin necesitar swipl.
 *
 * <p>El servidor falso responde como lo hace SWI-Prolog en Linux ({@code \n}) y en Windows
 * ({@code \r\n}, con el tamaño contando los {@code \r}), y también con los puntos de
 * <i>heartbeat</i> que MQI envía mientras un goal tarda.</p>
 *
 * <p>Cada test tiene un tiempo máximo: si el cliente leyera mal el tamaño se quedaría
 * esperando bytes que nunca llegan, y el test debe fallar en vez de colgar el build.</p>
 *
 * @author Sebastián González
 *
 * <p>Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 */
@DisplayName("Pruebas del MqiClient con un servidor falso")
@Timeout(value = 10, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class MqiClientTest {

    private static final String PASSWORD_OK = """
            {
              "args": [[[{"args": ["c", "g"], "functor": "threads"}]]],
              "functor": "true"
            }
            """;

    private static final String ANSWER = """
            {
              "args": [[[{"args": ["X", "San José Ñandú"], "functor": "="}]]],
              "functor": "true"
            }
            """;

    private ServerSocket server;
    private Thread serverThread;

    @AfterEach
    void cerrarServidor() throws IOException, InterruptedException {
        server.close();
        serverThread.join(2000);
    }

    /**
     * Levanta un servidor que lee cada mensaje del cliente y contesta con las respuestas dadas,
     * escritas tal como las enviaría MQI.
     *
     * @param windows    si los saltos de línea se escriben como {@code \r\n}
     * @param heartbeats cuántos puntos enviar antes de cada respuesta
     * @param answers    las respuestas, en orden
     * @return el puerto donde escucha
     */
    private int servidor(boolean windows, int heartbeats, String... answers) throws IOException {
        server = new ServerSocket(0);
        serverThread = new Thread(() -> {
            try (Socket socket = server.accept()) {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                for (String answer : answers) {
                    leerMensaje(in);
                    String newline = windows ? "\r\n" : "\n";
                    String text = answer.replace("\n", newline);
                    byte[] body = text.getBytes(StandardCharsets.UTF_8);
                    String header = ".".repeat(heartbeats) + body.length + "." + newline;
                    out.write(header.getBytes(StandardCharsets.UTF_8));
                    out.write(body);
                    out.flush();
                }
            } catch (IOException ignored) {
                // el test terminó y cerró el servidor
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        return server.getLocalPort();
    }

    /**
     * Lee un mensaje del cliente con el formato {@code <bytes>.\n<mensaje>}.
     */
    private static void leerMensaje(InputStream in) throws IOException {
        StringBuilder header = new StringBuilder();
        for (int c = in.read(); c != '\n'; c = in.read()) {
            if (c == -1) {
                throw new IOException("cliente desconectado");
            }
            if (c != '.') {
                header.append((char) c);
            }
        }
        in.readNBytes(Integer.parseInt(header.toString()));
    }

    @Test
    @DisplayName("Autentica y consulta con saltos de linea \\n (Linux)")
    void formatoLinux() throws IOException {
        try (MqiClient client = new MqiClient("127.0.0.1", servidor(false, 0, PASSWORD_OK, ANSWER))) {
            client.authenticate("clave");
            PrologResult.Success success = assertInstanceOf(PrologResult.Success.class, client.query("X = 1"));
            assertEquals(List.of(Map.of("X", "San José Ñandú")), success.solutions());
        }
    }

    @Test
    @DisplayName("Autentica y consulta con saltos de linea \\r\\n (Windows)")
    void formatoWindows() throws IOException {
        try (MqiClient client = new MqiClient("127.0.0.1", servidor(true, 0, PASSWORD_OK, ANSWER))) {
            client.authenticate("clave");
            PrologResult.Success success = assertInstanceOf(PrologResult.Success.class, client.query("X = 1"));
            assertEquals(List.of(Map.of("X", "San José Ñandú")), success.solutions());
        }
    }

    @Test
    @DisplayName("Ignora los puntos de heartbeat antes del tamano")
    void heartbeats() throws IOException {
        try (MqiClient client = new MqiClient("127.0.0.1", servidor(true, 3, PASSWORD_OK, ANSWER))) {
            client.authenticate("clave");
            assertInstanceOf(PrologResult.Success.class, client.query("X = 1"));
        }
    }

    @Test
    @DisplayName("Varias respuestas seguidas no se mezclan")
    void variasRespuestas() throws IOException {
        try (MqiClient client = new MqiClient("127.0.0.1",
                servidor(true, 0, PASSWORD_OK, "\"false\"\n", ANSWER, "\"false\"\n"))) {
            client.authenticate("clave");
            assertInstanceOf(PrologResult.Failure.class, client.query("fail"));
            assertInstanceOf(PrologResult.Success.class, client.query("X = 1"));
            assertInstanceOf(PrologResult.Failure.class, client.query("fail"));
        }
    }

    @Test
    @DisplayName("Rechaza una contrasena incorrecta")
    void contrasenaIncorrecta() throws IOException {
        int port = servidor(false, 0, "{\"args\": [\"bad_password\"], \"functor\": \"exception\"}\n");
        try (MqiClient client = new MqiClient("127.0.0.1", port)) {
            assertThrows(IOException.class, () -> client.authenticate("mala"));
        }
    }
}
