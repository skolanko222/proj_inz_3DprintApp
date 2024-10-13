package connection.gcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GcodeFileReader implements AutoCloseable {
    private final BufferedReader reader;
    private final File file;
    private Point center = new Point(0, 0, 0);
    private Point max = new Point(0, 0, 0);
    private Point min = new Point(0, 0, 0);
    private ArrayList<Line> lines = new ArrayList<>();

    // Konstruktor przyjmujący obiekt File
    public GcodeFileReader(String path) throws IOException {
        this.file = new File(path);
        this.reader = new BufferedReader(new FileReader(file));
    }

    // Metoda do wczytywania kolejnej linii z pliku
    private String readNextLine() throws IOException {
        return reader.readLine();
    }

    // Zamknięcie czytnika
    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    public String[] parseNextLine() {
        String line = null;
        try {
            line = readNextLine();
            if (line != null ) {
                // Podział linii na słowa, separatorem jest spacja
                line = line.trim();
                String[] words = line.split(" ");
                return words;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public GcodeFileReader parseAllLines() {
        String[] words;
        Point lastPoint = new Point(0, 0, 0);
        Point currentPoint = new Point(0, 0, 0);
        while ((words = parseNextLine()) != null) {
            int length = words.length;
            if(length > 0 && words[0].equals("G1") || words[0].equals("G0")) {
                Float x = null, y = null, z = null;
                for (int i = 1; i < length; i++) {
                    String word = words[i];
                    if (word.startsWith("X")) {
                        x = Float.parseFloat(word.substring(1));
                    } else if (word.startsWith("Y")) {
                        y = Float.parseFloat(word.substring(1));
                    } else if (word.startsWith("Z")) {
                        z = Float.parseFloat(word.substring(1));
                    }
                }
                if (x == null) {
                    x = lastPoint.getX();
                }
                if (y == null) {
                    y = lastPoint.getY();
                }
                if (z == null) {
                    z = lastPoint.getZ();
                }

                currentPoint = new Point(x, y, z);
                System.out.println("Current point: " + currentPoint);
                Line line = new Line(lastPoint, currentPoint);
                lines.add(line);
                lastPoint = currentPoint;
            }
        }
        calculateAll();
        System.out.println("Center: " + center);
        System.out.println("Max: " + max);
        return this;
    }
    private void calculateAll() {
        for(Line line : getLines()) {
            calculateMinMax(line.getStart());
            calculateMinMax(line.getEnd());
        }
        calculateCenter();

    }

    private void calculateCenter() {

        for(Line line : getLines()) {
            center.setX((line.getStart().getX() + center.getX()) / 2);
            center.setY((line.getStart().getY() + center.getY()) / 2);
            center.setZ((line.getStart().getZ() + center.getZ()) / 2);
        }
    }

    private void calculateMinMax(Point point) {
        if (point.getX() > max.getX()) {
            max.setX(point.getX());
        }
        if (point.getY() > max.getY()) {
            max.setY(point.getY());
        }
        if (point.getZ() > max.getZ()) {
            max.setZ(point.getZ());
        }
        if (point.getX() < min.getX()) {
            min.setX(point.getX());
        }
        if (point.getY() < min.getY()) {
            min.setY(point.getY());
        }
        if (point.getZ() < min.getZ()) {
            min.setZ(point.getZ());
        }
    }

    public ArrayList<Line> getLines() {
        return lines;
    }
    public Point getCenter() {
        return center;
    }
    public Point getMax() {
        return max;
    }
    public Point getMin() {
        return min;
    }


    public static void main(String[] args) {
        try {
            GcodeFileReader gcodeReader = new GcodeFileReader("C:\\Users\\Szymon\\Desktop\\ta.gcode");

            // Czytanie pliku linijka po linijce
            ArrayList<Line> lines = gcodeReader.parseAllLines().getLines();
            System.out.println("Number of lines: " + lines.size());

            // Zamknięcie czytnika
            gcodeReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}