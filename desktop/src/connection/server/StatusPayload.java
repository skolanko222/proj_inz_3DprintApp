package connection.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusPayload {
    private String printerId;
    private String status;

    // Default constructor
    public StatusPayload() {
    }
    @JsonCreator
    public StatusPayload(@JsonProperty("printerId") String printerId,
                         @JsonProperty("status") String status) {
        this.printerId = printerId;
        this.status = status;
    }

    // Getters and setters
    public String getPrinterId() {
        return printerId;
    }

    public void setPrinterId(String printerId) {
        this.printerId = printerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StatusPayload{" +
                "printerId='" + printerId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}