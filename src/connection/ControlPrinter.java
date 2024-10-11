package connection;

import connection.gcode.GcodeObject;
import connection.transmiter.DataTransmiterInterface;

public class ControlPrinter {
    private final DataTransmiterInterface dataTransmiter;
    private final PrinterSettings printerSettings;
    //xyz coordinates
    private Integer x = null;
    private Integer y = null;
    private Integer z = null;


    public enum PrinterAxis {
        X,
        Y,
        Z
    }

    public ControlPrinter(DataTransmiterInterface dataTransmiter, PrinterSettings printerSettings) {
        if (dataTransmiter == null || printerSettings == null) {
            throw new IllegalArgumentException("dataTransmiter cannot be null");
        }
        this.printerSettings = printerSettings;
        this.dataTransmiter = dataTransmiter;
        if (!dataTransmiter.isConnected()) {
            try {
                dataTransmiter.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void moveAxisRelatively(PrinterAxis axis, int distance) {
        if (distance == 0) {
            return;
        }
        sendCommand(GcodeObject.prepareCommand("G91", false, null));
        String command = "G1 " + axis.name() + distance + " F" + printerSettings.getSpeed() + "\n";
        sendCommand(GcodeObject.prepareCommand(command, true, null));
        sendCommand(GcodeObject.prepareCommand("G90", false, null));
        sendCommand(GcodeObject.prepareCommand("M105", true, null));
    }

    public void homeAxis(PrinterAxis axis) {
        String command = "G28 " + axis.name() + "\n";
        sendCommand(GcodeObject.prepareCommand(command, true, null));
    }

    public void homeAllAxis() {
        sendCommand(GcodeObject.prepareCommand("G28\n", true, (_) -> {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            System.out.println("Homed all axis");
        }));
    }

    public void releaseMotors() {
        sendCommand(GcodeObject.prepareCommand("M84\n", true, null));
    }

    private void sendCommand(GcodeObject command) {
        dataTransmiter.queueCommand(command);
    }



}
