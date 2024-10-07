package connection;

import javax.security.auth.callback.Callback;
import java.util.concurrent.Callable;

public class GcodeObject {
    private String command = null;
    private boolean isResponse = false;
    private String response = null;
    private Callback callback = null;


    public GcodeObject(String command, boolean isResponse) {
        this.command = command;
        this.isResponse = isResponse;
    }

    public GcodeObject(String command, boolean isResponse, Callback callback) {
        this.command = command;
        this.isResponse = isResponse;
        this.callback = callback;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isResponse() {
        return isResponse;
    }

    public void setResponse(boolean isResponse) {
        this.isResponse = isResponse;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "GcodeObject{" +
                "command='" + command + '\'' +
                ", isResponse=" + isResponse +
                ", response='" + response + '\'' +
                '}';
    }
}
