package printer;

import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.google.gson.Gson;
import com.fazecast.jSerialComm.SerialPort;
import com.google.gson.stream.JsonWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class PrinterSettings{
    private String profileName = "Default";
    public static String DEFAULT_SETTINGS = "{\"baudRate\":9600,\"xMin\":0,\"xMax\":200,\"yMin\":0,\"yMax\":220,\"zMin\":0,\"zMax\":150,\"speed\":30,\"maxTempExt\":260,\"defaultTempExt\":100,\"maxTempBed\":80,\"defaultTempBed\":50,\"checkTempInterval\":5}";
    public static String DEFAULT_PROFILE_PATH = "C:\\Users\\Szymon\\Documents\\ProjektyStudia\\proj_inz_3DprintApp\\profiles\\";

    private Integer baudRate;
    private transient SerialPort serialPort;
    private String portName;

    private Integer xMin = 0;
    private Integer xMax = 200;
    private Integer yMin = 0;
    private Integer yMax = 220;
    private Integer zMin = 0;
    private Integer zMax = 150;
    private Integer speed = 30;

    private Integer maxTempExt = 260;
    private Integer defaultTempExt = 100;
    private Integer maxTempBed = 80;
    private Integer defaultTempBed = 50;
    private Integer checkTempInterval = 5;

    // zapisywanie ustawień do pliku
    public void saveProfileToFile(String path) {
        if(profileName.isEmpty())
            throw new IllegalArgumentException("Profile name cannot be empty");
        String json = this.serializeProfile();
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(path + "\\" + profileName + ".json"));
            writer.jsonValue(json);
            writer.close();
        } catch (IOException e) {
            Logger.getGlobal().severe("Error while saving profile to file");
        }
    }

    // odczytywanie ustawień z pliku
    static public PrinterSettings readProfileFromFile(String path, String profileName) {
        try {
            // read the whole file content
            String text = Files.readString(Paths.get(path + profileName + ".json"), StandardCharsets.US_ASCII);
            // check if the content is correct
            return PrinterSettings.deserializeProfile(text);
        } catch (Exception e){
            Logger.getGlobal().severe("File not found");
        }

        return null;
    }

    // serializacja ustawień z obiektu do JSON string
    // TODO zamienić na prywatne metody, w testach użyć refleksji
    public String serializeProfile() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // deserializacja ustawień z JSON string do obiektu
    public static PrinterSettings deserializeProfile(String json) {
        Gson gson = new Gson();
        System.out.println(json);
        return gson.fromJson(json, PrinterSettings.class);
    }

    // Konstruktor
    public PrinterSettings(Integer baudRate, SerialPort serialPort)  {
        this.baudRate = baudRate;
        setSerialPort(serialPort);
//        deserializeProfile(DEFAULT_SETTINGS);
    }

    // Gettery i settery
    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
        if(serialPort == null)
            this.portName = null;
        else
            this.portName = serialPort.getSystemPortName();
    }
    public void setSerialPort(String serialPortName) {
        try {
            this.serialPort = SerialPort.getCommPort(serialPortName);
            this.portName = serialPortName;
        } catch (SerialPortInvalidPortException e){
            Logger.getGlobal().severe("Port" + serialPortName + " not found");
        }
    }
    public String getPortName() { return portName; }

    public Integer getxMin() {
        return xMin;
    }

    public void setxMin(Integer xMin) {
        this.xMin = xMin;
    }

    public Integer getxMax() {
        return xMax;
    }

    public void setxMax(Integer xMax) {
        this.xMax = xMax;
    }

    public Integer getyMin() {
        return yMin;
    }

    public void setyMin(Integer yMin) {
        this.yMin = yMin;
    }

    public Integer getyMax() {
        return yMax;
    }

    public void setyMax(Integer yMax) {
        this.yMax = yMax;
    }

    public Integer getzMin() {
        return zMin;
    }

    public void setzMin(Integer zMin) {
        this.zMin = zMin;
    }

    public Integer getzMax() {
        return zMax;
    }

    public void setzMax(Integer zMax) {
        this.zMax = zMax;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getMaxTempExt() {
        return maxTempExt;
    }

    public void setMaxTempExt(Integer maxTempExt) {
        this.maxTempExt = maxTempExt;
    }

    public Integer getDefaultTempExt() {
        return defaultTempExt;
    }

    public void setDefaultTempExt(Integer defaultTempExt) {
        this.defaultTempExt = defaultTempExt;
    }

    public Integer getMaxTempBed() {
        return maxTempBed;
    }

    public void setMaxTempBed(Integer maxTempBed) {
        this.maxTempBed = maxTempBed;
    }

    public Integer getDefaultTempBed() {
        return defaultTempBed;
    }

    public void setDefaultTempBed(Integer defaultTempBed) {
        this.defaultTempBed = defaultTempBed;
    }

    public Integer getCheckTempInterval() {
        return checkTempInterval;
    }

    public void setCheckTempInterval(Integer checkTempInterval) {
        this.checkTempInterval = checkTempInterval;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    // Metoda toString

    @Override
    public String toString() {
        return "PrinterSettings{" +
                ", profileName='" + profileName +
                ", baudRate=" + baudRate +
                ", serialPort=" + serialPort +
                ", xMin=" + xMin +
                ", xMax=" + xMax +
                ", yMin=" + yMin +
                ", yMax=" + yMax +
                ", zMin=" + zMin +
                ", zMax=" + zMax +
                ", speed=" + speed +
                ", maxTempExt=" + maxTempExt +
                ", defaultTempExt=" + defaultTempExt +
                ", maxTempBed=" + maxTempBed +
                ", defaultTempBed=" + defaultTempBed +
                ", checkTempInterval=" + checkTempInterval +
                '}';
    }

    // Metoda equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrinterSettings that = (PrinterSettings) o;
        return baudRate.equals(that.baudRate) &&
                serialPort.equals(that.serialPort) &&
                xMin.equals(that.xMin) &&
                xMax.equals(that.xMax) &&
                yMin.equals(that.yMin) &&
                yMax.equals(that.yMax) &&
                zMin.equals(that.zMin) &&
                zMax.equals(that.zMax) &&
                speed.equals(that.speed) &&
                maxTempExt.equals(that.maxTempExt) &&
                defaultTempExt.equals(that.defaultTempExt) &&
                maxTempBed.equals(that.maxTempBed) &&
                defaultTempBed.equals(that.defaultTempBed) &&
                checkTempInterval.equals(that.checkTempInterval);
    }

    // Metoda hashCode
    @Override
    public int hashCode() {
        int result = baudRate.hashCode();
        result = 31 * result + serialPort.hashCode();
        result = 31 * result + xMin.hashCode();
        result = 31 * result + xMax.hashCode();
        result = 31 * result + yMin.hashCode();
        result = 31 * result + yMax.hashCode();
        result = 31 * result + zMin.hashCode();
        result = 31 * result + zMax.hashCode();
        result = 31 * result + speed.hashCode();
        result = 31 * result + maxTempExt.hashCode();
        result = 31 * result + defaultTempExt.hashCode();
        result = 31 * result + maxTempBed.hashCode();
        result = 31 * result + defaultTempBed.hashCode();
        result = 31 * result + checkTempInterval.hashCode();
        return result;
    }



}
