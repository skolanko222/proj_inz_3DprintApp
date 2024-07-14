import com.fazecast.jSerialComm.*;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SerialPortDataSender implements Runnable{
    private SerialPort serialPort;
    private BaseTransmHandler handler;
    public ConcurrentLinkedQueue<String> commandsQueue;

    public SerialPortDataSender(BaseTransmHandler handler, SerialPort serialPort) {
        this.handler = handler;
        this.serialPort = serialPort;
        this.commandsQueue = new ConcurrentLinkedQueue<>();
    }
    private void send(String data) {
//        handler.setTransmStatus(true);
        serialPort.writeBytes(data.getBytes(), data.getBytes().length);
//        waitForDataToBeSent(); // blokowanie wątku do czasu aż dane zostaną wysłane, aby nie przepełnić bufora w drukarce
//        handler.setTransmStatus(false);

    }
    private void waitForDataToBeSent() {
        while (handler.isTransmStatus()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            if (!commandsQueue.isEmpty()) {
                System.out.println("Sending command: " + commandsQueue.peek());
                send(commandsQueue.poll());
            }
        }
    }
}
