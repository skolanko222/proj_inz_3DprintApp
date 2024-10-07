package connection.transmiter;

import com.fazecast.jSerialComm.*;
import connection.GcodeObject;
import connection.connect.SerialPortDataSender;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BaseTransmHandler {
    private static final Logger logger = Logger.getLogger(BaseTransmHandler.class.getName());

    // flag is true when command is sent and waits for "ok" from printer, changes to false when "ok" is received
    private SerialPort chosenPort;
    private SerialPortDataSender sender;
    private ConcurrentLinkedQueue<GcodeObject> commandsQueue = new ConcurrentLinkedQueue<GcodeObject>();
    private ConcurrentLinkedQueue<GcodeObject> responsesQueue = new ConcurrentLinkedQueue<GcodeObject>();


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
        sender = new SerialPortDataSender(chosenPort, this);
        senderThread = new Thread(sender);

        // choose the port to connect to
        logger.info("[BaseTransmHandler] Chosen port: " + chosenPort.getSystemPortName());
        chosenPort.openPort();
        chosenPort.setComPortParameters(baudRate, 8, 1, 0);
        chosenPort.addDataListener(sender);
        senderThread.start();
    }

    public void queueCommand(GcodeObject command) {
        logger.finest("[connection.connect.SerialPortDataSender] Command added to send queue: " + command);
        commandsQueue.add(command);
    }

    public GcodeObject dequeueCommand() {
        GcodeObject command = commandsQueue.poll();
        queueResponse(command);
        return command;
    }

    public void queueResponse(GcodeObject command) {
        logger.finest("Command added to response queue: " + command);
        responsesQueue.add(command);
    }

    public GcodeObject dequeueResponse() {
        logger.finest("response for: " + responsesQueue.peek());
        return responsesQueue.poll();
    }

    public void streamFile(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    GcodeObject command = prepareCommand(line, false);
                } catch ( IllegalArgumentException e) {
                    logger.warning("[BaseTransmHandler] " + e.getMessage());
                    continue;
                }
                GcodeObject command = prepareCommand(line, false);
                /// TODO
                queueCommand(command);
            }
        } catch (FileNotFoundException e) {
            logger.severe("[BaseTransmHandler] File not found: " + path);
        }
        catch (IOException e) {
            logger.severe("[BaseTransmHandler] IOException while reading file: " + path);
        }
    }
    private GcodeObject prepareCommand(String command, boolean isResponse) throws IllegalArgumentException {
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
            System.out.println("Command: " + command);
        }
        return new GcodeObject(command, isResponse);

    }
    public boolean isQueueEmpty() {
        return commandsQueue.isEmpty();
    }

    public void disconnect() throws InterruptedException {
        chosenPort.closePort();
        senderThread.interrupt();
    }
    public void sendImid(String command) {
        sender.sendImid(command);
    }

    public static void main(String[] args) {
        //make console log finest level
        logger.setLevel(java.util.logging.Level.FINEST);

        SerialPort serialPort = SerialPort.getCommPort("COM3");
        BaseTransmHandler handler = new BaseTransmHandler(serialPort, 115200);
        GcodeObject temp = new GcodeObject("M105\n",true);
        handler.queueCommand(temp);
        //wait seconds
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("hej" +
                temp.getResponse());
        try {
            handler.disconnect();
        } catch (InterruptedException e) {
           System.out.println("Interrupted");
        }

    }
}
