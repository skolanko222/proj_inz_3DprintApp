package connection.connect;

import connection.gcode.GcodeObject;

public interface ConnectionInterface {

    boolean openPort() throws Exception;
    void closePort() throws Exception;
    public boolean isOpen();

    void send(GcodeObject data);

}
