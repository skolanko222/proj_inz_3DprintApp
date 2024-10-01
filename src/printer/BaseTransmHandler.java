package printer;

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
            logger.setLevel(java.util.logging.Level.ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public BaseTransmHandler(SerialPort port, Integer baudRate) {
        chosenPort = port;
        reader = new SerialPortReader(chosenPort);
        sender = new SerialPortDataSender(chosenPort, reader);
        senderThread = new Thread(sender);

        // choose the port to connect to
        logger.info("[BaseTransmHandler] Chosen port: " + chosenPort.getSystemPortName());
        chosenPort.openPort();
        chosenPort.setComPortParameters(baudRate, 8, 1, 0);
        chosenPort.addDataListener(reader);
        senderThread.start();
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
}
