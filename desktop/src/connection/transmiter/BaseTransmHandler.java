package connection.transmiter;

import com.fazecast.jSerialComm.*;
import com.mycompany.gui_proj_inz.utils.MaxListSizeStringListModel;
import connection.ControlPrinter;
import gcode.GcodeObject;
import connection.PrinterSettings;
import connection.connect.SerialPortDataSender;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BaseTransmHandler implements DataTransmiterInterface {
    private static final Logger logger = Logger.getLogger(BaseTransmHandler.class.getName());

    // flag is true when command is sent and waits for "ok" from printer, changes to false when "ok" is received
    private SerialPort chosenPort;
    private SerialPortDataSender sender;
    private ControlPrinter printer;
    private ConcurrentLinkedDeque<GcodeObject> commandsQueue = new ConcurrentLinkedDeque<GcodeObject>();
    private ConcurrentLinkedDeque<GcodeObject> responsesQueue = new ConcurrentLinkedDeque<GcodeObject>();
    PrinterSettings settings;
    private MaxListSizeStringListModel responseList = new MaxListSizeStringListModel(50); // max 50 responses
    Thread senderThread;

    public int getQueueSize() {
        return commandsQueue.size();
    }
    public int getResponseQueueSize() {
        return responsesQueue.size();
    }
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
    @Override
    public void queueCommand(GcodeObject command) {
        sender.resume();
        logger.finest("[connection.connect.SerialPortDataSender] Command added to send queue: " + command);
        commandsQueue.add(command);
    }

    @Override
    public void sendCommandImidietly(GcodeObject command) {
        logger.finest("[connection.connect.SerialPortDataSender] Command added to send queue FRONT: " + command);
        if (sender.isPaused()) {
            sender.resume();
            commandsQueue.addFirst(command);
            sender.pause();
        }
        else
            commandsQueue.addFirst(command);
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


    public boolean isCommmandQueueEmpty() {
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
        if (chosenPort != null && chosenPort.isOpen()) {
            chosenPort.removeDataListener();
            chosenPort.closePort();
            if (senderThread != null && senderThread.isAlive()) {
                senderThread.interrupt();
                senderThread.join(1000); // Wait for the thread to terminate
            }
            Logger.getLogger(BaseTransmHandler.class.getName()).info("Port closed");
        }
    }

    public void pauseSending() {
        sender.pause();
    }

    public void resumeSending() {
        sender.resume();
    }

    public MaxListSizeStringListModel getResponseList() {
        return responseList;
    }

}
