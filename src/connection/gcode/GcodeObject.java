package connection.gcode;

import java.util.function.Consumer;


public class GcodeObject {
    private String command = null;
    private boolean isResponse = false;
    private String response = null;
    private Consumer<String> callback = null;

    private GcodeObject(String command, boolean isResponse, Consumer<String> callback) {
        this.command = command;
        this.isResponse = isResponse;
        this.callback = callback;
    }

    public static GcodeObject prepareCommand(String command, boolean isResponse, Consumer<String> cal) throws IllegalArgumentException {
        // check if starts with ;
        if (command.startsWith(";")) {
            throw new IllegalArgumentException("Tried to send comment.");
        }
        // delete comments
        if (command.contains(";")) {
            command = command.substring(0, command.indexOf(";"));
        }
        // make uppercase
        command = command.toUpperCase();
        //check if ends with newline
        if (!command.endsWith("\n")) {
            command += "\n";
            System.out.println("Command: " + command);
        }
        return new GcodeObject(command, isResponse, cal);

    }

    public String getCommand() {return command;}

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isResponse() {
        return isResponse;
    }

    public void setResponse(boolean isResponse) {
        this.isResponse = isResponse;
    }

    public String getResponse() {return response;}

    public void setResponse(String response) {
        this.response = response;
    }

    public Consumer<String> getCallback() {
        return callback;
    }

    @Override
    public String toString() {
        return "GcodeObject{" +
                "command='" + command + '\'' +
                ", isResponse=" + isResponse +
                ", response='" + response + '\'' +
                ", callback=" + callback +
                '}';
    }
}
