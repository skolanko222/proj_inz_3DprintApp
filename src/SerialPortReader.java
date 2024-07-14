import com.fazecast.jSerialComm.*;

import java.io.InputStream;
import java.util.logging.*;

public class SerialPortReader implements SerialPortDataListener{
    private final SerialPort serialPort;
    private BaseTransmHandler handler;
    public SerialPortReader(BaseTransmHandler handler, SerialPort serialPort) {
        this.handler = handler;
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
        System.out.println("Read " + numRead + " bytes.");
        System.out.println("Data: " + new String(newData));
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
        if(receivedData.contains("ok")) {
            handler.setTransmStatus(false);
            System.out.println("OK received");
        }
        else {
            System.out.println("Received: " + receivedData);
        }
    }
}

