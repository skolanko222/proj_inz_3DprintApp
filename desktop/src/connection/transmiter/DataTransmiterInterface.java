package connection.transmiter;

import com.mycompany.gui_proj_inz.utils.MaxListSizeStringListModel;
import gcode.GcodeObject;

/**
 * Interface for transmitting data to printer. This interface doesn't specify how data is transmitted, only that it can be transmitted.
 * It is used to abstract the way of transmitting data to printer using different methods (e.g. serial port, network) with help of queues.
 */
public interface DataTransmiterInterface {
    void connect() throws Exception; // connect to printer
    boolean isConnected(); // check if connected to printer
    void disconnect() throws Exception; // disconnect from printer
    void queueCommand(GcodeObject command); // add command to queue
    void sendCommandImidietly(GcodeObject command); // send command immediately (before other commands in queue)
    GcodeObject dequeueCommand(); // get command from queue
    GcodeObject dequeueResponse(); // get response from queue
    boolean isCommmandQueueEmpty(); // check if command queue is empty
    boolean isResponseQueueEmpty(); // check if response queue is empty
    MaxListSizeStringListModel getResponseList(); // get list of responses
}
