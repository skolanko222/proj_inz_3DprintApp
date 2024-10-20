package connection;

import connection.gcode.GcodeObject;
import connection.transmiter.DataTransmiterInterface;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ControlPrinter {
    private final DataTransmiterInterface dataTransmiter;
    private final PrinterSettings printerSettings;
    //xyz coordinates
    private Integer x = null;
    private Integer y = null;
    private Integer z = null;

    //temperature
    private Float extruderTemp = null;
    private Consumer<Float> extruderTempLabel;
    private Float bedTemp = null;
    private Consumer<Float> bedTempLabel;
    private Thread temperatureThread;


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
                Logger.getLogger(ControlPrinter.class.getName()).severe("Error while connecting to printer");
            }
        }

        int updateTemperatureInterval = printerSettings.getCheckTempInterval();
        temperatureThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(updateTemperatureInterval * 1000);
                    updateTemperature();
                } catch (InterruptedException e) {
                    Logger.getLogger(ControlPrinter.class.getName()).info("Temperature thread interrupted");
                }
            }
        });
    }
    public void startTemperatureThread(Consumer<Float> extruderTempConsumer, Consumer<Float> bedTempConsumer) {
        extruderTempLabel = extruderTempConsumer;
        bedTempLabel = bedTempConsumer;
        temperatureThread.start();
    }
    public void stopTemperatureThread() {
        temperatureThread.interrupt();
    }

    public void setDesiredExtrTemp(float extruderTemp) {
        sendCommand(GcodeObject.prepareCommand("M104 S" + extruderTemp + "\n", false, null));
    }
    public void setDesiredBedTemp(float bedTemp) {
        sendCommand(GcodeObject.prepareCommand("M140 S" + bedTemp + "\n", false, null));
    }
    public void setFanSpeed(int speed) {
        sendCommand(GcodeObject.prepareCommand("M106 S" + speed + "\n", false, null));
    }



    private void updateTemperature() {
        sendCommand(GcodeObject.prepareCommand("M105\n", false, (a) -> {
            String[] split = a.split(" ");
            for (String s : split) {
                try{
                    System.out.println(s);
                    if (s.startsWith("T")) {
                        extruderTemp = Float.parseFloat(s.substring(2));
                        extruderTempLabel.accept(extruderTemp);
                    } else if (s.startsWith("B")) {
                        bedTemp = Float.parseFloat(s.substring(2));
                        bedTempLabel.accept(bedTemp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
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
        sendCommand(GcodeObject.prepareCommand("G28\n", true, (a) -> {
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
