package cr.ac.una.eif400.broker.mqi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Cliente del protocolo MQI (Machine Query Interface) de SWI-Prolog sobre un socket TCP.
 *
 * <p>Cada mensaje, en los dos sentidos, va precedido por su tamaño en <b>bytes UTF-8</b>:</p>
 * <pre>
 * &lt;bytes&gt;.\n&lt;mensaje&gt;
 * </pre>
 * <p>Lo primero que se envía es la contraseña; después, cada consulta como
 * {@code run((Goal), -1).}, donde {@code -1} significa sin tiempo límite.</p>
 *
 * <p>Cerrar el cliente solo cierra la conexión: el proceso {@code swipl} sigue vivo. De
 * terminarlo se encarga {@link PrologEngine}.</p>
 *
 * @author Sebastián González
 *
 * <p>Sigue el diseño del {@code MqiClient} del demo {@code MQI_ResponseDemo} del curso (el
 * mismo framing, autenticación y {@code run}). A diferencia del demo, abre su propio socket y
 * acepta {@code \r\n} y heartbeats de forma explícita, por los fallos encontrados al probarlo
 * en Windows.
 * Herramienta de asistencia: Claude (Anthropic), declarado en el README, Sprint P2.</p>
 *
 * @version P2
 */
public class MqiClient implements AutoCloseable {

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    /**
     * Abre la conexión con el servidor MQI.
     *
     * @param host dirección del servidor, normalmente {@code 127.0.0.1}
     * @param port puerto que imprimió {@code swipl} al arrancar
     * @throws IOException si no se puede conectar
     */
    public MqiClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedInputStream(socket.getInputStream());
        this.out = new BufferedOutputStream(socket.getOutputStream());
    }

    /**
     * Se autentica con la contraseña que imprimió {@code swipl} al arrancar.
     *
     * @param password la contraseña
     * @throws IOException si la conexión falla o el servidor rechaza la contraseña
     */
    public void authenticate(String password) throws IOException {
        send(password);
        if (!(MqiResponseParser.parse(receive()) instanceof PrologResult.Success)) {
            throw new IOException("MQI rejected the password");
        }
    }

    /**
     * Ejecuta un goal y devuelve todas sus soluciones.
     *
     * @param goal el goal de Prolog, sin el punto final
     * @return el resultado del goal
     * @throws IOException si la conexión se cae
     */
    public PrologResult query(String goal) throws IOException {
        send("run((" + goal + "), -1)");
        return MqiResponseParser.parse(receive());
    }

    /**
     * Envía un mensaje con su encabezado de tamaño. Agrega el {@code .\n} final que pide MQI.
     *
     * @param message el mensaje sin el punto final
     * @throws IOException si la conexión se cae
     */
    private void send(String message) throws IOException {
        byte[] body = (message + ".\n").getBytes(StandardCharsets.UTF_8);
        out.write((body.length + ".\n").getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    /**
     * Lee un mensaje completo: primero el tamaño y luego esa cantidad de bytes.
     *
     * <p>Es tolerante con dos detalles del servidor MQI, igual que el cliente oficial de Python
     * ({@code swiplserver}):</p>
     * <ul>
     *     <li>Mientras un goal largo corre, MQI envía puntos sueltos ({@code .}) como
     *     <i>heartbeat</i> antes del tamaño. Se ignoran.</li>
     *     <li>En Windows, SWI-Prolog escribe los saltos de línea como {@code \r\n}, también en
     *     el encabezado ({@code 203.\r\n}). Por eso el {@code \r} se acepta antes del {@code \n}.</li>
     * </ul>
     *
     * <p>El tamaño ya cuenta los bytes tal como viajan (con los {@code \r} incluidos en
     * Windows), así que el cuerpo se lee exactamente con esa cantidad de bytes. Los {@code \r}
     * que quedan en el JSON son espacio en blanco y Gson los ignora.</p>
     *
     * @return el contenido del mensaje
     * @throws IOException si la conexión se cae o el encabezado no es un número
     */
    private String receive() throws IOException {
        StringBuilder header = new StringBuilder();
        for (int c = read(); c != '\n'; c = read()) {
            if (Character.isDigit(c)) {
                header.append((char) c);
            } else if (c != '.' && c != '\r') {
                throw new IOException("Invalid MQI message header: " + header + (char) c);
            }
        }
        if (header.isEmpty()) {
            throw new IOException("Invalid MQI message header: no size");
        }

        int size = Integer.parseInt(header.toString());
        byte[] body = in.readNBytes(size);
        if (body.length < size) {
            throw new EOFException("MQI connection closed");
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    /**
     * Lee un byte, fallando si la conexión se cerró.
     *
     * @return el byte leído
     * @throws IOException si la conexión se cerró
     */
    private int read() throws IOException {
        int c = in.read();
        if (c == -1) {
            throw new EOFException("MQI connection closed");
        }
        return c;
    }

    /**
     * Le avisa al servidor que se cierra la conexión y cierra el socket. No termina {@code swipl}.
     */
    @Override
    public void close() {
        try {
            send("close");
        } catch (IOException ignored) {
            // la conexión ya estaba caída: solo queda cerrar el socket
        }
        try {
            socket.close();
        } catch (IOException ignored) {
            // nada más que hacer
        }
    }
}
