package connection.connect;

import connection.GcodeObject;

public interface ConnectionInterface {

    boolean openPort() throws Exception;
    void closePort() throws Exception;
    public boolean isOpen();

    void send(GcodeObject data);

}
