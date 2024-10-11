package connection.transmiter;

import connection.gcode.GcodeObject;

public interface DataTransmiterInterface {
    void connect() throws Exception;
    boolean isConnected();
    void disconnect() throws Exception;
    void queueCommand(GcodeObject command);
    GcodeObject dequeueCommand();
}
