import com.fazecast.jSerialComm.*;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BaseTransmHandler {
    private static final Logger logger = Logger.getLogger(SerialPortReader.class.getName());

    // flag is true when command is sent and waits for "ok" from printer, changes to false when "ok" is received
    private boolean transmStatus = false;
    private SerialPort chosenPort;
    private SerialPortReader reader;
    SerialPortDataSender sender;
    Thread senderThread;

    public BaseTransmHandler(SerialPort chosenPort) {
        this.chosenPort = chosenPort;
        reader = new SerialPortReader(this, chosenPort);
        sender = new SerialPortDataSender(this, chosenPort);
        senderThread = new Thread(sender);
    }
    static {
        try {
            FileHandler fileHandler = new FileHandler("PrinterConnection.log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            System.out.println(port.getSystemPortName());
        }
        // choose the port to connect to
        SerialPort chosenPort = ports[0];
        chosenPort.openPort();
        chosenPort.setComPortParameters(9600, 8, 1, 0);
        BaseTransmHandler handler = new BaseTransmHandler(chosenPort);
        chosenPort.addDataListener(handler.reader);
        handler.senderThread.start();

        // stwórz nowy wątek, który otworzy konsole do wpisywania komend i wysyłania ich do drukarki, stwórz klasę anonimową
        Thread consoleThread = new Thread(() -> {
            while (true) {
                String command = System.console().readLine();
                handler.sender.commandsQueue.add(command + "\n");
            }
        });
        consoleThread.start();

        //co sekundę wysyłamy zapytanie o status drukarki
//        while (true) {
//            handler.sender.commandsQueue.add("M105\n");
//            System.out.println(handler.sender.commandsQueue.size() + " commands in queue");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
    public boolean isTransmStatus() {
        return transmStatus;
    }
    public void setTransmStatus(boolean transmStatus) {
        this.transmStatus = transmStatus;
    }
}
