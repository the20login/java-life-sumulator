package com.company.life_simulator.world.quadtree;

import java.util.Objects;

public class Vector {
    private final double x;
    private final double y;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Point from, Point to)
    {
        this.x = to.getX() - from.getX();
        this.y = to.getY() - from.getY();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public static Vector getUnitVector(double angle)
    {
        return new Vector(Math.cos(angle*Math.PI*2), Math.sin(angle*Math.PI*2));
    }

    public double length() {
        return Math.sqrt(x*x + y*y);
    }

    public double squareLength() {
        return (x*x + y*y);
    }

    public Vector scale(double scaleFactor) {
        return Vector.scale(this, scaleFactor);
    }

    public static Vector scale(final Vector vector, double scalingFactor) {
        return new Vector(vector.x * scalingFactor, vector.y * scalingFactor);
    }

    public double angle() {
        double angle = Math.atan2(y, x)/(Math.PI*2);
        if (angle < 0)
            angle += 1;
        return angle;
    }

    public Vector rotate(double newAngle)
    {
        double length = this.length();

        return new Vector(length * Math.cos(newAngle*Math.PI*2), length * Math.sin(newAngle*Math.PI*2));
    }

    public boolean isZeroVector()
    {
        return x== 0 && y == 0;
    }

    public Vector plus(Vector other)
    {
        return new Vector(this.x + other.x, this.y + other.y);
    }

    @Override
    public String toString() {
        return String.format("{%s, %s}", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector vector = (Vector) o;
        return Double.compare(vector.x, x) == 0 &&
                Double.compare(vector.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
