package cr.ac.una.eif400.cyphail.common;

public class QueryResponse {
    private final boolean ok;
    private final String data;
    private final String message;

    private QueryResponse(boolean ok, String data, String message) {
        this.ok = ok;
        this.data = data;
        this.message = message;
    }

    public static QueryResponse ok(String data, String message) {
        return new QueryResponse(true, data, message);
    }

    public static QueryResponse error(String message) {
        return new QueryResponse(false, null, message);
    }

    public boolean isOk() {
        return ok;
    }

    public String getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}