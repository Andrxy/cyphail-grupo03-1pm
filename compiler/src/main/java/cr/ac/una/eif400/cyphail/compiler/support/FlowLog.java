package cr.ac.una.eif400.cyphail.compiler.support;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @Author Claude
 *
 * Utilidad simple para escribir el registro de simulacion del flujo de
 * compilacion (Tokenizer -> Parser -> SemanticAnalyser -> CodeGenerator)
 * a un archivo de texto plano, {@value #LOG_FILE}.
 *
 * <p>Quien arma el pipeline {@code Compiler.compile(String)}
 * construye un unico bloque de texto con todos los mensajes de una corrida
 * y lo entrega aqui para que se escriba de una sola vez, agregandolo al
 * final del archivo.
 *
 * <p><b>Nota:</b> esta clase fue generada con la asistencia de Claude
 * (Anthropic), a partir de una conversacion de como guardar
 * el registro del flujo de simulacion del proyecto para el Sprint 1.1.
 */
public final class FlowLog {

    /** Nombre del archivo donde se acumula el registro del flujo. */
    private static final String LOG_FILE = "cyphail-flow.log";

    private FlowLog() {
    }

    /**
     * Agrega un bloque de texto al final de {@value #LOG_FILE}.
     *
     * @param block texto a escribir, normalmente varias lineas ya armadas
     *              por quien llama (una por cada paso del pipeline)
     */
    public static void write(String block) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(block);
        } catch (IOException e) {
            System.err.println("No se pudo escribir el registro del flujo: " + e.getMessage());
        }
    }
}