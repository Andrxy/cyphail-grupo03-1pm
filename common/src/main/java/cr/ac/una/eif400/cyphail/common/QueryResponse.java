package cr.ac.una.eif400.cyphail.common;

public class QueryResponse {
    private final boolean ok;
    private final String[] headers;
    private final String[][] rows;
    private final String message;

    private QueryResponse(boolean ok, String[] headers, String[][] rows, String message) {
        this.ok = ok;
        this.headers = headers;
        this.rows = rows;
        this.message = message;
    }

    public static QueryResponse ok(String[] headers, String[][] rows, String message) {
        return new QueryResponse(true, headers, rows, message);
    }

    public static QueryResponse error(String message) {
        return new QueryResponse(false, null, null, message);
    }

    public boolean isOk() {
        return ok;
    }

    public String[] getHeaders() {
        return headers;
    }

    public String[][] getRows() {
        return rows;
    }

    public String getMessage() {
        return message;
    }
}
