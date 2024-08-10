import com.fazecast.jSerialComm.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BaseTransmHandler {
    private static final Logger logger = Logger.getLogger(SerialPortReader.class.getName());

    // flag is true when command is sent and waits for "ok" from printer, changes to false when "ok" is received
    private SerialPort chosenPort;
    private SerialPortReader reader;
    private SerialPortDataSender sender;
    Thread senderThread;
    static {
        try {
            FileHandler fileHandler = new FileHandler("PrinterConnection.log");
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public BaseTransmHandler(SerialPort chosenPort) {
        this.chosenPort = chosenPort;
        reader = new SerialPortReader(chosenPort);
        sender = new SerialPortDataSender(chosenPort, reader);
        senderThread = new Thread(sender);
    }


    public void sendCommand(String command) {
        logger.info("[BaseTransmHandler] Sending command: " + command);

        sender.queueCommand(prepareCommand(command));
    }
    public void streamFile(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String command = prepareCommand(line);
                } catch ( IllegalArgumentException e) {
                    logger.warning("[BaseTransmHandler] " + e.getMessage());
                    continue;
                }
                String command = prepareCommand(line);
                sendCommand(line);
            }
        } catch (FileNotFoundException e) {
            logger.severe("[BaseTransmHandler] File not found: " + path);
        }
        catch (IOException e) {
            logger.severe("[BaseTransmHandler] IOException while reading file: " + path);
        }
    }
    private String prepareCommand(String command) throws IllegalArgumentException {
        // check if starts with ;
        if (command.startsWith(";")) {
            throw new IllegalArgumentException("Tried to send comment.");
        }
        // delete comments
        if (command.contains(";")) {
            command = command.substring(0, command.indexOf(";"));
        }
        // make uppercase
        command = command.toUpperCase();
        //check if ends with newline
        if (!command.endsWith("\n")) {
            command += "\n";
        }
        return command;

    }
    public static void main(String[] args) {
        SerialPort[] ports = SerialPort.getCommPorts();

        // choose the port to connect to
        SerialPort chosenPort = ports[0];
        logger.info("[BaseTransmHandler] Chosen port: " + chosenPort.getSystemPortName());
        chosenPort.openPort();
        chosenPort.setComPortParameters(9600, 8, 1, 0);
        BaseTransmHandler handler = new BaseTransmHandler(chosenPort);
        chosenPort.addDataListener(handler.reader);
        handler.senderThread.start();

        handler.streamFile("C:\\Users\\Szymon\\Desktop\\SCP_3DBenchy1.gcode");
//        handler.sendCommand("G1 Y10");
        // stwórz nowy wątek, który otworzy konsole do wpisywania komend i wysyłania ich do drukarki, stwórz klasę anonimową
        Thread consoleThread = new Thread(() -> {
            while (true) {
                String command = System.console().readLine();
                handler.sendCommand(command);
            }
        });
        consoleThread.start();

//        co sekundę wysyłamy zapytanie o status drukarki
//        while (true) {
//            try {
//                Thread.sleep(1000);
//                handler.sendCommand("M105");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
