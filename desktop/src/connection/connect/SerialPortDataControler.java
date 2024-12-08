package connection.connect;

import com.fazecast.jSerialComm.*;
import connection.transmiter.DataTransmiterInterface;
import gcode.GcodeObject;

import java.util.logging.Logger;


public class SerialPortDataControler implements Runnable, SerialPortDataListener {
    private final SerialPort serialPort;
    private final DataTransmiterInterface transmHandler;
    private static final Logger logger = Logger.getLogger(SerialPortDataControler.class.getName());
    private final Object lock = new Object();
    private boolean pause = false;

    public SerialPortDataControler(SerialPort serialPort, DataTransmiterInterface transmHandler) {
        if(serialPort == null){
            throw new NullPointerException("SerialPort is null");
        }
        this.serialPort = serialPort;
        this.transmHandler = transmHandler;
    }
    private void send() {
        GcodeObject data = transmHandler.dequeueCommand();
        logger.finest("Sending data via serial port: " + data);
        serialPort.writeBytes(data.getCommand().getBytes(), data.getCommand().getBytes().length);
        if(transmHandler.isCommmandQueueEmpty())
            pause();
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            byte[] newData = new byte[serialPort.bytesAvailable()];
            int numRead = serialPort.readBytes(newData, newData.length);
            handleReceivedData(newData, numRead); // Obsługa odebranych danych
        }
    }

    private void handleReceivedData(byte[] buffer, int len) {
        GcodeObject response = transmHandler.dequeueResponse(); // komenda do której przypisujemy odpowiedź
        String receivedData = new String(buffer, 0, len);
        response.setResponse(receivedData); // parowanie odpowiedzi drukarki z komendą pobraną z kolejki
        if(response.isResponse())
            // lista odpowiedzi - wyświetlanie w GUI
            transmHandler.getResponseList().addElement(response.getCommand() + " -> " + response.getResponse());
            logger.info(" Read " + buffer.length + " bytes. \n" + response);
        // Wywołanie callbacka (np aktualizacja temperatury w GUI)
        if(response.getCallback() != null) {
            response.getCallback().accept(receivedData);
        }
    }

    public void pause() {
        logger.finest("Sender paused");
        synchronized (lock) {
            pause = true;
        }
    }

    public void resume() {
        logger.finest("Sender resumed");
        synchronized (lock) {
            pause = false;
            lock.notify();  // Powiadomienie, że wątek może kontynuować pracę
        }
    }

    public boolean isPaused() {
        return pause;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if(!transmHandler.isCommmandQueueEmpty() && transmHandler.isResponseQueueEmpty()){
                send();
            }
            synchronized (lock) {
                while (pause) {
                    try {
                        lock.wait();  // Wątek czeka, aż zostanie powiadomiony
                    } catch (InterruptedException e) {
                        logger.info("Sender thread interrupted");
                    }
                }
            }
        }
    }
}
