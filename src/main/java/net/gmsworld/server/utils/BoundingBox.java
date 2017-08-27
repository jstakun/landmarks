package net.gmsworld.server.utils;

public class BoundingBox {
    public double north;
    public double south;
    public double east;
    public double west;

    @Override
    public String toString() {
        return "north: " + north + ",south: " + south + ",east: " + east + ",west: " + west;
    }
}