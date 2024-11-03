package connection.transmiter;

import gcode.GcodeObject;

public interface DataTransmiterInterface {
    void connect() throws Exception;
    boolean isConnected();
    void disconnect() throws Exception;
    void queueCommand(GcodeObject command);
    void sendCommandImidietly(GcodeObject command);
    GcodeObject dequeueCommand();
}
