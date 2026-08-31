package cr.ac.una.eif400.broker.fake;

/**
 * Proveedor que gestiona los grafos quemados para las pruebas
 *
 * <p>Contiene definiciones de cabeceras, estructuras de datos estáticas y métodos de validación
 * implementados temporalmente para efectos del primer sprint P1.1</p>
 *
 * @author Wilson Umaña
 * Version 1.0
 */

public class FakeGraphs {

    public static String[] headers = {"Graph", "Description"};

    public static String[][] availableGraphs = {
            {"Amigos", "Social Network"},
            {"Tasks", "Task and resources"},
            {"teams",   "Soccer Teams"},
            {"planets", "Planets in Solar System"}
    };

    /**
     * Verifica si un grafo específico existe dentro de los datos quemados
     *
     * @param name nombre del grafo a buscar
     * @return trues i el grafo se encuentra registrado, si no false
     */
    public static boolean exists(String name) {
        for (String[] graph : availableGraphs) {
            if (graph[0].equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
