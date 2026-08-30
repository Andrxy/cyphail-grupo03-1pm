package cr.ac.una.eif400.broker.fake;

/**
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

    public static boolean exists(String name) {
        for (String[] graph : availableGraphs) {
            if (graph[0].equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
