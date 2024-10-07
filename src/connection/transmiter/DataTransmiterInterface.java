package connection.transmiter;

import connection.GcodeObject;
import printer.PrinterSettings;

import java.util.Collection;

interface DataTransmiterInterface {
    void connect() throws Exception;
    boolean isConnected();
    void disconnect() throws Exception;
    Collection<GcodeObject> getCommandQueue();
    void queueCommand(GcodeObject command);
    GcodeObject dequeueCommand();
}
