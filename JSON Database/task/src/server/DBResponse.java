package server;

public class DBResponse {

    private final String response;
    private Object value;
    private Exception reason;

    DBResponse(String response) {
        this.response = response;
    }

    DBResponse(String response, Object value) {
        this.response = response;
        this.value = value;
    }

    DBResponse(String response, Exception reason) {
        this.response = response;
        this.reason = reason;
    }

    public String getResponse() {
        return response;
    }

    public Exception getReason() {
        return reason;
    }

    public Object getValue() {
        return value;
    }

}
