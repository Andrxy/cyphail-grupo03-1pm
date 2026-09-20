package cr.ac.una.eif400.broker.model;

public record QueryEntry(String query, String[] headers, String[][] rows) {
}