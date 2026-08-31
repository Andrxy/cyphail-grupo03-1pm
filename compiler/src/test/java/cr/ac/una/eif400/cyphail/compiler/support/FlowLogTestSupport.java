package cr.ac.una.eif400.cyphail.compiler.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base para las pruebas que tocan el archivo de registro del flujo. Guarda el
 * contenido previo de cyphail-flow.log, deja el archivo limpio para la prueba y
 * lo restaura al terminar, de modo que las corridas no se contaminen entre si
 * ni se pierda el registro de la ultima sesion real.
 *
 * @author Grupo 03-1pm
 * @version P1.1
 */
public abstract class FlowLogTestSupport {

    private static final String LOG_FILE_NAME = "cyphail-flow.log";

    private String contenidoPrevio;

    @BeforeEach
    protected void guardarRegistroPrevio() throws IOException {
        Path archivo = archivoDeRegistro();

        contenidoPrevio = Files.exists(archivo) ? Files.readString(archivo) : null;
        Files.deleteIfExists(archivo);
    }

    @AfterEach
    protected void restaurarRegistroPrevio() throws IOException {
        Path archivo = archivoDeRegistro();

        if (contenidoPrevio == null) {
            Files.deleteIfExists(archivo);
        } else {
            Files.writeString(archivo, contenidoPrevio);
        }
    }

    /**
     * Lee lo que las pruebas hayan escrito en el registro del flujo.
     *
     * @return el contenido del archivo, o una cadena vacia si aun no existe
     */
    protected String registro() throws IOException {
        Path archivo = archivoDeRegistro();

        return Files.exists(archivo) ? Files.readString(archivo) : "";
    }

    /**
     * Ubica el registro donde lo deja {@link FlowLog}, resolviendo la raiz del
     * proyecto igual que esa clase: a partir de donde vive en el disco.
     *
     * @return la ruta del archivo de registro
     */
    protected static Path archivoDeRegistro() {
        return raizDelProyecto().resolve(LOG_FILE_NAME);
    }

    private static Path raizDelProyecto() {
        try {
            Path ubicacion = Path.of(FlowLog.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path raiz = ubicacion.getParent().getParent().getParent();

            return (raiz != null && Files.exists(raiz)) ? raiz : Path.of("").toAbsolutePath();
        } catch (URISyntaxException | NullPointerException e) {
            return Path.of("").toAbsolutePath();
        }
    }
}
