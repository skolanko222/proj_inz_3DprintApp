package connection;

import connection.gcode.GcodeFileReader;
import connection.gcode.GcodeObject;
import connection.transmiter.DataTransmiterInterface;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ControlPrinter {
    private final DataTransmiterInterface dataTransmiter;
    private final PrinterSettings printerSettings;
    //xyz coordinates
    private Float x = null;
    private Float y = null;
    private Float z = null;

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
            while (!Thread.currentThread().isInterrupted()) {
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
        if (temperatureThread != null && temperatureThread.isAlive()) {
            temperatureThread.interrupt();
            try {
                temperatureThread.join(1000); // Wait for the thread to terminate
            } catch (InterruptedException e) {
                Logger.getLogger(ControlPrinter.class.getName()).severe("Failed to stop temperature thread");
            }
        }
    }

    public void setDesiredExtrTemp(float extruderTemp) {
        sendCommandImmediately(GcodeObject.prepareCommand("M104 S" + extruderTemp + "\n", false, null));
    }
    public void setDesiredBedTemp(float bedTemp) {
        sendCommandImmediately(GcodeObject.prepareCommand("M140 S" + bedTemp + "\n", false, null));
    }
    public void setFanSpeed(int speed) {
        if(speed < 0 || speed > 100) {
            throw new IllegalArgumentException("Speed must be between 0 and 100");
        }
        //convert to 0-255
        speed = (int) (speed * 2.55);
        sendCommandImmediately(GcodeObject.prepareCommand("M106 S" + speed + "\n", false, null));
    }

    private void updateTemperature() {
        sendCommandImmediately(GcodeObject.prepareCommand("M105\n", false, (a) -> {
            String[] split = a.split(" ");
            for (String s : split) {
                try{
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
        queueComman(GcodeObject.prepareCommand("G91", false, null));
        String command = "G1 " + axis.name() + distance + " F" + printerSettings.getSpeed() + "\n";
        queueComman(GcodeObject.prepareCommand(command, true, null));
        queueComman(GcodeObject.prepareCommand("G90", false, null));
        queueComman(GcodeObject.prepareCommand("M105", true, null));
    }

    public void homeAxis(PrinterAxis axis) {
        String command = "G28 " + axis.name() + "\n";
        queueComman(GcodeObject.prepareCommand(command, true, null));
    }

    public void homeAllAxis() {
        queueComman(GcodeObject.prepareCommand("G28\n", true, (a) -> {
            this.x = 0F;
            this.y = 0F;
            this.z = 0F;
        }));
    }

    public void releaseMotors() {
        queueComman(GcodeObject.prepareCommand("M84\n", true, null));
    }


    public void streamFile(GcodeFileReader gcodeFileReader) {
        try {
            gcodeFileReader.reset();
            String line = gcodeFileReader.readNextLine();
            while (line != null) {
                String finalLine = line;
                queueComman(GcodeObject.prepareCommand(line, false, null));
                line = gcodeFileReader.readNextLine();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void queueComman(GcodeObject command) {
        dataTransmiter.queueCommand(command);
    }
    private void sendCommandImmediately(GcodeObject command) {
        dataTransmiter.sendCommandImidietly(command);
    }



}
