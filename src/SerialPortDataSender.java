import com.fazecast.jSerialComm.*;
import java.util.logging.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SerialPortDataSender implements Runnable{
    private SerialPort serialPort;
    private SerialPortReader serialPortReader;
    private ConcurrentLinkedQueue<String> commandsQueue;
    private static final Logger logger = Logger.getLogger(SerialPortReader.class.getName());

    public SerialPortDataSender(SerialPort serialPort, SerialPortReader reader) {
        this.serialPort = serialPort;
        this.serialPortReader = reader;
        this.commandsQueue = new ConcurrentLinkedQueue<>();
    }
    private void send(String data) {
//        serialPortReader.setTransmStatus(true);
        serialPort.writeBytes(data.getBytes(), data.getBytes().length);
//        waitForDataToBeSent(); // blokowanie wątku do czasu aż dane zostaną wysłane, aby nie przepełnić bufora w drukarce
//        serialPortReader.setTransmStatus(false);

    }
    private void waitForDataToBeSent() {
        while (serialPortReader.isTransmStatus()) {
            //loguj co sekundę logger.finest("[SerialPortDataSender] Waiting for data to be sent...");
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void queueCommand(String command) {
        logger.finest("[SerialPortDataSender] Command added to queue: " + command);
        commandsQueue.add(command);
    }

    @Override
    public void run() {
        while (true) {
            if (!commandsQueue.isEmpty()) {
                logger.finest("[SerialPortDataSender - run] Sending command: " + commandsQueue.peek());
                send(commandsQueue.poll());
            }
        }
    }
}
