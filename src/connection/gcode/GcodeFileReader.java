package connection.gcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class GcodeFileReader implements AutoCloseable {
    private BufferedReader reader;
    private final File file;
    private Point center = new Point(0, 0, 0);
    private Point max = new Point(0, 0, 0);
    private Point min = new Point(0, 0, 0);
    private ArrayList<Line> lines = new ArrayList<>();
    private int [] layerStarts; //index of first line of each layer
    private boolean awaitLayerStart = false;
    private int layerCounter = 0;

    public Integer getLayerAmount() {
        return layerAmount;
    }
    private Integer layerAmount = null;


    public GcodeFileReader(File fileOb) throws IOException {
        this.file = new File(fileOb.getAbsolutePath());
        this.reader = new BufferedReader(new FileReader(file));
    }

    public String readNextLine() throws IOException {
        return reader.readLine();
    }

    public void reset() throws IOException {
        reader.close();
        reader = new BufferedReader(new FileReader(file));
    }

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
        Point lastPoint = null;
        Point currentPoint = null;
        while ((words = parseNextLine()) != null) {
            int length = words.length;

            //find layer count, check until it is found
            if(layerAmount == null) {
                findLayerCount(words);
            }
            //find layer number
            findLayerNumber(words);

            currentPoint = readPoint(words, lastPoint);
            if(lastPoint == null) {
                lastPoint = currentPoint;
                continue;
            }
            Line line = new Line(lastPoint, currentPoint);
            lines.add(line);

            lastPoint = currentPoint;
    }
        if(layerAmount == null) {
            throw new RuntimeException("Layer count not found");
        }
        calculateAll();
        System.out.println("Center: " + center);
        System.out.println("Max: " + max);
        System.out.println("Min: " + min);
        return this;
    }

    private Point readPoint(String[] words, Point lastPoint) {
        if(lastPoint == null) {
            lastPoint = new Point(0, 0, 0);
            System.out.println("Last point is null");
        }
        int length = words.length;
        Point currentPoint = lastPoint.copy();
        if(length > 0 && words[0].equals("G1") || words[0].equals("G0")) {
            if (awaitLayerStart) { // layer starts with G1/G0 command
                layerStarts[layerCounter - 1] = lines.size();
                awaitLayerStart = false;
            }
            Float x = null, y = null, z = null;
                for (int i = 1; i < length; i++) {
                    String word = words[i];
                    if (word.startsWith("X")) {
                        x = Float.parseFloat(word.substring(1));
                    } else if (word.startsWith("Y")) {
                        y = Float.parseFloat(word.substring(1));
                    } else if (word.startsWith("Z")) {
                        z = Float.parseFloat(word.substring(1));
                    } else if (word.startsWith("E")) {
                        currentPoint.setEndOfIdleMove(false);
                    }
                }

            currentPoint.setX(Objects.requireNonNullElseGet(x, lastPoint::getX));
            currentPoint.setY(Objects.requireNonNullElseGet(y, lastPoint::getY));
            currentPoint.setZ(Objects.requireNonNullElseGet(z, lastPoint::getZ));
        }
        return currentPoint;
    }

    private void calculateAll() {
        for(Line line : getLines().subList(1, getLines().size())) {
            calculateMinMax(line.getStart());
            calculateMinMax(line.getEnd());
        }
        calculateCenter();

    }

    private void calculateCenter() {
        center.setX((max.getX() + min.getX()) / 2);
        center.setY((max.getY() + min.getY()) / 2);
        center.setZ((max.getZ() + min.getZ()) / 2);  
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
    private void findLayerCount(String[] line) {
        if(line.length > 0 && line[0].startsWith(";LAYER_COUNT:")) {
            layerAmount = Integer.parseInt(line[0].split(":")[1]);
            System.out.println("Layer count: " + layerAmount);
            layerStarts = new int[layerAmount];
        }

    }
    private void findLayerNumber(String[] line) {
        if(line.length > 0 && line[0].startsWith(";LAYER:")) {
            awaitLayerStart = true;
            layerCounter++;
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
    public int getLayerIndex(int layer) throws RuntimeException {
        if(layerStarts == null) {
            throw new RuntimeException("Layer starts not found");
        }
        else if(layer < 0 || layer >= layerStarts.length) {
            throw new RuntimeException("Layer index out of bounds");
        }
        else
            return layerStarts[layer];
    }

}