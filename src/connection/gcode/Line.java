package connection.gcode;

public class Line {
    private Point start;
    private Point end;

    //
    private boolean isIdleMove = false;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public boolean checkIfTheSameZ() {
        return start.getZ() == end.getZ();
    }

    public boolean isIdleMove() {
        return isIdleMove;
    }

    public void setIdleMove(boolean idleMove) {
        isIdleMove = idleMove;
    }

    @Override
    public String toString() {
        return "Line{" +
                "start=" + start +
                ",\n end=" + end +
                '}';
    }
}
