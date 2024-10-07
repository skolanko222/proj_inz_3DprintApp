package connection.connect;

import com.fazecast.jSerialComm.*;
import connection.GcodeObject;
import connection.transmiter.BaseTransmHandler;

import java.util.logging.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SerialPortDataSender implements Runnable, SerialPortDataListener{
    private SerialPort serialPort;
    private static final Logger logger = Logger.getLogger(SerialPortDataSender.class.getName());
    BaseTransmHandler transmHandler;

    public SerialPortDataSender(SerialPort serialPort, BaseTransmHandler transmHandler) {
        if(serialPort == null){
            throw new NullPointerException("SerialPort is null");
        }
        this.serialPort = serialPort;
        if(!serialPort.isOpen()){
            serialPort.openPort();
        }
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
        System.out.println("Connected");
        handleReceivedData(newData, numRead);
    }

    private void handleReceivedData(byte[] buffer, int len) {
        GcodeObject response = transmHandler.dequeueResponse();
        String receivedData = new String(buffer, 0, len);
        System.out.println("halo" + receivedData);
        logger.fine("[handleReceivedData] Read " + buffer.length + " bytes.");
        logger.fine("[handleReceivedData] DATA START: \n" + receivedData);
        response.setResponse(receivedData);

        if(receivedData.contains("ok")) {
            logger.fine("[handleReceivedData] Received ok from printer.");
        }

    }

    @Override
    public void run() {
        while (true) {
            if (!transmHandler.isQueueEmpty()) {
                send();
            }
        }
    }
}
