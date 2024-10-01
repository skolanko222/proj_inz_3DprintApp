package printer;

import com.fazecast.jSerialComm.*;
import java.util.logging.*;


public class SerialPortReader implements SerialPortDataListener{
    private final SerialPort serialPort;
    private boolean transmStatus = false;
    private Logger logger = Logger.getLogger(SerialPortReader.class.getName());

    public SerialPortReader(SerialPort serialPort) {
        this.serialPort = serialPort;
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
        //        InputStream in = serialPort.getInputStream();
//        byte[] buffer = new byte[1024];
//        int len = 0;
//        try {
//            while (in.available() > 0) {
//                len = in.read(buffer);
//            }
//            handleReceivedData(buffer, len);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void handleReceivedData(byte[] buffer, int len) {
        String receivedData = new String(buffer, 0, len);
//        logger.info("[handleReceivedData] DATA END: \n");
        logger.fine("[handleReceivedData] Read " + buffer.length + " bytes.");
        logger.info("[handleReceivedData] DATA START: \n" + receivedData);
        if(receivedData.contains("ok")) {
            setTransmStatus(false);
            logger.info("[handleReceivedData] Received ok from printer, setting transmStatus to false.");
        }

    }
    public boolean isTransmStatus() {
        return transmStatus;
    }
    public void setTransmStatus(boolean transmStatus) {
        this.transmStatus = transmStatus;
    }
}

