package cr.ac.una.eif400.broker.mqi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Proceso de SWI-Prolog con el engine cargado y la conexión MQI ya autenticada.
 *
 * <p>Ciclo de vida:</p>
 * <ol>
 *     <li>{@link #start} lanza {@code swipl -l engine.pl mqi --write_connection_values=true}.</li>
 *     <li>{@code swipl} imprime el puerto en la primera línea y la contraseña en la segunda.</li>
 *     <li>Se abre el socket (con reintentos cortos) y se autentica con un {@link MqiClient}.</li>
 *     <li>{@link #run} ejecuta goals mientras el proceso siga vivo.</li>
 *     <li>{@link #close} cierra la conexión <b>y</b> termina el proceso, para que no quede un
 *     {@code swipl} huérfano. Se puede llamar varias veces.</li>
 * </ol>
 *
 * @author Sebastián González
 *
 * <p>Escrita con la asistencia de Claude (Anthropic) a partir del demo {@code MQI_ResponseDemo}
 * del curso, y revisada por el equipo. Corresponde al prompt del Plan #3 (Broker MQI)
 * declarado en el README.</p>
 *
 * @version P2
 */
public class PrologEngine implements AutoCloseable {

    /** Tiempo máximo para que {@code swipl} imprima el puerto y la contraseña. */
    private static final long STARTUP_TIMEOUT_SECONDS = 10;

    /** Intentos de conexión: {@code swipl} imprime el puerto un instante antes de escuchar. */
    private static final int CONNECT_ATTEMPTS = 40;
    private static final long CONNECT_RETRY_MILLIS = 50;

    private final Process process;
    private final MqiClient client;
    private boolean closed = false;

    private PrologEngine(Process process, MqiClient client) {
        this.process = process;
        this.client = client;
    }

    /**
     * Lanza {@code swipl} con el engine, se conecta y se autentica.
     *
     * @param enginePath ruta del {@code engine.pl}
     * @return el engine listo para recibir goals
     * @throws IOException si {@code swipl} no existe, no arranca a tiempo o la conexión falla
     */
    public static PrologEngine start(Path enginePath) throws IOException {
        if (!Files.isRegularFile(enginePath)) {
            throw new IOException("engine not found: " + enginePath.toAbsolutePath());
        }

        Process process = new ProcessBuilder(
                "swipl", "-l", enginePath.toString(), "mqi", "--write_connection_values=true")
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();

        try {
            process.getOutputStream().close();   // swipl no lee de la entrada estándar
            BlockingQueue<String> lines = drain(process);
            int port = Integer.parseInt(nextLine(lines, process).strip());
            String password = nextLine(lines, process).strip();

            MqiClient client = connect(port, process);
            client.authenticate(password);
            return new PrologEngine(process, client);
        } catch (IOException | RuntimeException e) {
            process.destroyForcibly();
            throw e instanceof IOException io ? io : new IOException("swipl did not start", e);
        }
    }

    /**
     * Lee la salida estándar de {@code swipl} en un hilo aparte y la deja en una cola.
     *
     * <p>Así se puede esperar el puerto y la contraseña con tiempo límite, y la salida se sigue
     * consumiendo después, para que {@code swipl} nunca se bloquee escribiendo.</p>
     *
     * @param process el proceso de {@code swipl}
     * @return la cola donde van llegando las líneas
     */
    private static BlockingQueue<String> drain(Process process) {
        BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        Thread reader = new Thread(() -> {
            try (BufferedReader out = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                out.lines().forEach(lines::add);
            } catch (IOException | UncheckedIOException ignored) {
                // el proceso terminó: no hay más salida
            }
        }, "cyphail-swipl-stdout");
        reader.setDaemon(true);
        reader.start();
        return lines;
    }

    /**
     * Espera la siguiente línea de {@code swipl} sin quedarse colgado si el proceso falla.
     *
     * <p>Revisa cada 100 ms si llegó la línea o si el proceso ya terminó (por ejemplo, un
     * {@code engine.pl} con errores o un SWI-Prolog sin MQI), para fallar de inmediato en vez
     * de esperar todo el tiempo límite.</p>
     *
     * @param lines   la cola que llena {@link #drain}
     * @param process el proceso, para detectar que murió
     * @return la línea leída
     * @throws IOException si se acaba el tiempo o el proceso termina antes
     */
    private static String nextLine(BlockingQueue<String> lines, Process process) throws IOException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(STARTUP_TIMEOUT_SECONDS);
        try {
            while (System.nanoTime() < deadline) {
                String line = lines.poll(100, TimeUnit.MILLISECONDS);
                if (line != null) {
                    return line;
                }
                if (!process.isAlive() && lines.isEmpty()) {
                    throw new IOException("swipl exited with code " + process.exitValue());
                }
            }
            throw new IOException("swipl did not print its connection values in time");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while waiting for swipl", e);
        }
    }

    /**
     * Abre el socket, reintentando unos milisegundos si {@code swipl} todavía no escucha.
     *
     * @param port    el puerto que imprimió {@code swipl}
     * @param process el proceso, para no reintentar si ya murió
     * @return el cliente conectado
     * @throws IOException si no se logra conectar
     */
    private static MqiClient connect(int port, Process process) throws IOException {
        for (int attempt = 1; ; attempt++) {
            try {
                return new MqiClient("127.0.0.1", port);
            } catch (ConnectException e) {
                if (attempt == CONNECT_ATTEMPTS || !process.isAlive()) {
                    throw e;
                }
                try {
                    Thread.sleep(CONNECT_RETRY_MILLIS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IOException("interrupted while connecting to swipl", interrupted);
                }
            }
        }
    }

    /**
     * Ejecuta un goal en el engine.
     *
     * @param goal el goal de Prolog, sin el punto final
     * @return el resultado del goal
     * @throws IOException si la conexión con {@code swipl} se cayó
     */
    public synchronized PrologResult run(String goal) throws IOException {
        if (closed) {
            throw new IOException("the Prolog engine is closed");
        }
        return client.query(goal);
    }

    /**
     * Indica si el engine todavía puede recibir goals.
     *
     * @return {@code true} si no se cerró y el proceso sigue vivo
     */
    public synchronized boolean isAlive() {
        return !closed && process.isAlive();
    }

    /**
     * Cierra la conexión y termina el proceso {@code swipl}. Llamarlo más de una vez no hace nada.
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        client.close();
        process.destroy();
        try {
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }
}
