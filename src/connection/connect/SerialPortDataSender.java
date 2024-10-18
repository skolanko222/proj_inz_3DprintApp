package connection.connect;

import com.fazecast.jSerialComm.*;
import connection.gcode.GcodeObject;
import connection.transmiter.BaseTransmHandler;

import java.util.logging.Logger;


public class SerialPortDataSender implements Runnable, SerialPortDataListener{
    private final SerialPort serialPort;
    private static final Logger logger = Logger.getLogger(SerialPortDataSender.class.getName());
    private final BaseTransmHandler transmHandler;
    private final Object lock = new Object();

    public SerialPortDataSender(SerialPort serialPort, BaseTransmHandler transmHandler) {
        if(serialPort == null){
            throw new NullPointerException("SerialPort is null");
        }
        this.serialPort = serialPort;
        this.transmHandler = transmHandler;
    }
    private void send() {
        GcodeObject data = transmHandler.dequeueCommand();
        logger.finest("[connection.connect.SerialPortDataSender] Sending data via serial port: " + data);
        serialPort.writeBytes(data.getCommand().getBytes(), data.getCommand().getBytes().length);
    }

    public void sendImid(String command) {
        logger.finest("[connection.connect.SerialPortDataSender] Sending now " + command);
        serialPort.writeBytes(command.getBytes(), command.getBytes().length);
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        byte[] newData = new byte[serialPort.bytesAvailable()];
        int numRead = serialPort.readBytes(newData, newData.length);
        handleReceivedData(newData, numRead);
    }

    private void handleReceivedData(byte[] buffer, int len) {
        GcodeObject response = transmHandler.dequeueResponse();
        String receivedData = new String(buffer, 0, len);
        response.setResponse(receivedData);
        System.out.println("Response: " + response.getResponse());
        if(response.isResponse())
            transmHandler.getResponseList().addElement(response.getCommand() + " -> " + response.getResponse());
        if(response.getCallback() != null) {
            System.out.println("Callback");
            response.getCallback().accept(receivedData);
            System.out.println("Callback end");
        }


        logger.info("[handleReceivedData] Read " + buffer.length + " bytes. \n" + response.toString());
    }

    @Override
    public void run() {
        while (true) {
            if(!transmHandler.isQueueEmpty() && transmHandler.isResponseQueueEmpty()){
                send();
            }
        }
    }
}
