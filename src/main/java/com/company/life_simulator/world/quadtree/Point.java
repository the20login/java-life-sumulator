package com.company.life_simulator.world.quadtree;

import java.util.Objects;

public class Point implements Comparable<Point>{
    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean at(int x, int y) {
        return this.x == x && this.y == y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point position = (Point) o;
        return x == position.x &&
                y == position.y;
    }

    public double squareDistance(Point pos)
    {
        return Math.pow(x - pos.getX(), 2) + Math.pow(y - pos.getY(), 2);
    }

    public double distance(Point pos)
    {
        return Math.sqrt(Math.pow(x - pos.getX(), 2) + Math.pow(y - pos.getY(), 2));
    }

    public Point delta(double dx, double dy)
    {
        return new Point(x + dx, y + dy);
    }

    public Point delta(Vector vector)
    {
        return delta(vector.getX(), vector.getY());
    }

    public Point multiply(double multiplier)
    {
        return new Point(x * multiplier, y * multiplier);
    }

    public boolean withinCircle(Point point, double radius)
    {
        return this.squareDistance(point) <= radius * radius;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", x, y);
    }

    @Override
    public int compareTo(Point o) {
        int xCompare = Double.compare(this.x, o.x);
        if (xCompare != 0)
            return xCompare;
        return Double.compare(this.y, o.y);
    }
}
