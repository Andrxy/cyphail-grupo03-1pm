package cr.ac.una.eif400.broker.fake;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

final class DataFileLocator {

    private DataFileLocator() {
    }

    static Path resolveDataFile(String fileName) {
        String cyphailHome = System.getProperty("cyphail.home");
        if (cyphailHome == null) {
            cyphailHome = System.getenv("CYPHAIL_HOME");
        }
        if (cyphailHome != null) {
            return Paths.get(cyphailHome, "broker", "data", fileName);
        }
        return buscarRaizDelProyecto().resolve("broker").resolve("data").resolve(fileName);
    }

    private static Path buscarRaizDelProyecto() {
        Path actual = Path.of("").toAbsolutePath();
        while (actual != null) {
            Path pom = actual.resolve("pom.xml");
            if (Files.exists(pom) && esPomRaiz(pom)) {
                return actual;
            }
            actual = actual.getParent();
        }
        throw new RuntimeException("No se encontro la raiz del proyecto");
    }

    private static boolean esPomRaiz(Path pom) {
        try {
            String contenido = Files.readString(pom);
            return contenido.contains("<packaging>pom</packaging>");
        } catch (IOException e) {
            return false;
        }
    }
}