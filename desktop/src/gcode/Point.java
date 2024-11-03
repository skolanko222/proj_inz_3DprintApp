package gcode;

import java.util.Objects;

public class Point {
    private float x;
    private float y;
    private float z;
    private boolean isEndOfIdleMove = true;


    public Point(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point(Float x, Float y, Float z) {
        this.x = Objects.requireNonNullElse(y, 0F);
        this.y = Objects.requireNonNullElse(z, 0F);
        this.z = Objects.requireNonNullElse(z, 0F);
    }


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return  "x=" + x +
                ", y=" + y +
                ", z=" + z;

    }

    public boolean isEndOfIdleMove() {
        return isEndOfIdleMove;
    }

    public void setEndOfIdleMove(boolean endOfIdleMove) {
        isEndOfIdleMove = endOfIdleMove;
    }

    public Point copy() {
        return new Point(x, y, z);
    }
}
