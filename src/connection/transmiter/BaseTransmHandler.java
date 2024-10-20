package connection.transmiter;

import com.fazecast.jSerialComm.*;
import com.mycompany.gui_proj_inz.utils.MaxListSizeStringListModel;
import connection.ControlPrinter;
import connection.gcode.GcodeObject;
import connection.PrinterSettings;
import connection.connect.SerialPortDataSender;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BaseTransmHandler implements DataTransmiterInterface {
    private static final Logger logger = Logger.getLogger(BaseTransmHandler.class.getName());

    // flag is true when command is sent and waits for "ok" from printer, changes to false when "ok" is received
    private SerialPort chosenPort;
    private SerialPortDataSender sender;
    private ControlPrinter printer;
    private ConcurrentLinkedQueue<GcodeObject> commandsQueue = new ConcurrentLinkedQueue<GcodeObject>();
    private ConcurrentLinkedQueue<GcodeObject> responsesQueue = new ConcurrentLinkedQueue<GcodeObject>();
    PrinterSettings settings;
    private MaxListSizeStringListModel responseList = new MaxListSizeStringListModel(50);
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
    public BaseTransmHandler(PrinterSettings settings) throws Exception {
        chosenPort = settings.getSerialPort();
        sender = new SerialPortDataSender(chosenPort, this);
        senderThread = new Thread(sender);
        this.settings = settings;

        // choose the port to connect to
        logger.info("[BaseTransmHandler] Chosen port: " + chosenPort.getSystemPortName());
        connect();

        senderThread.start();
        //wait a second for the sender to start
        Thread.sleep(1000);
        queueCommand(GcodeObject.prepareCommand("M105", false, null));
        queueCommand(GcodeObject.prepareCommand("M105", false, null));

    }

    @Override
    public void queueCommand(GcodeObject command) {
        logger.finest("[connection.connect.SerialPortDataSender] Command added to send queue: " + command);
        commandsQueue.add(command);
    }

    @Override
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
    public boolean isResponseQueueEmpty() {
        return responsesQueue.isEmpty();
    }

    public void streamFile(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                GcodeObject command = GcodeObject.prepareCommand(line, false, null);
                queueCommand(command);
            }
        } catch (FileNotFoundException e) {
            logger.severe("[BaseTransmHandler] File not found: " + path);
        }
        catch (IOException e) {
            logger.severe("[BaseTransmHandler] IOException while reading file: " + path);
        }
    }
    public boolean isQueueEmpty() {
        return commandsQueue.isEmpty();
    }

    @Override
    public void connect() throws Exception {
        if (chosenPort == null) {
            throw new Exception("Serial port is null");
        }
        else if(chosenPort.isOpen()) {
            throw new Exception("Port is already open");
        }
        else {
            chosenPort.openPort();
            chosenPort.setComPortParameters(settings.getBaudRate(), 8, 1, 0);
            chosenPort.addDataListener(sender);
        }
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    public void disconnect() throws Exception {
        if(chosenPort != null && chosenPort.isOpen()) {
            chosenPort.removeDataListener();
            chosenPort.closePort();
            senderThread.interrupt();
            Logger.getLogger(BaseTransmHandler.class.getName()).info("Port closed");
        }
    }

    public MaxListSizeStringListModel getResponseList() {
        return responseList;
    }

}
