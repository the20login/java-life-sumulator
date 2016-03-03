package com.company.life_simulator.world.quadtree;

/**
 * Axis-aligned area
 */
public class Rectangle {
    private final double x1, y1, x2, y2;

    /**
     * Creates new instance
     *
     * @param x1 top left x coordinate
     * @param y1 top left y coordinate
     * @param x2 bottom right x coordinate
     * @param y2 bottom right y coordinate
     */
    public Rectangle(double x1, double y1, double x2, double y2) {
        assert x1 <= x2;
        assert y1 <= y2;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Creates new instance
     *
     * @param point1 top left point
     * @param point2 bottom right point
     */
    public Rectangle(Point point1, Point point2) {
        this(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    /**
     * Creates new instance
     *
     * @param point  top left point
     * @param width  area width
     * @param height area height
     */
    public Rectangle(Point point, double width, double height) {
        this(point.getX(), point.getY(), point.getX() + width, point.getY() + height);
    }

    public Point getTopLeft() {
        return new Point(x1, y1);
    }

    public Point getTopRight() {
        return new Point(x2, y1);
    }

    public Point getBottomLeft() {
        return new Point(x1, y2);
    }

    public Point getBottomRight() {
        return new Point(x2, y2);
    }

    public Point getCenter() {
        return new Point(x1 + getWidth() / 2, y1 + getHeight() / 2);
    }

    public double getWidth() {
        return x2 - x1;
    }

    public double getHeight() {
        return y2 - y1;
    }

    public boolean contains(Point point) {
        return x1 <= point.getX() && x2 >= point.getX()
                && y1 <= point.getY() && y2 >= point.getY();
    }

    public boolean isIntersect(Rectangle rect) {
        return x1 <= rect.x2 && x2 >= rect.x1
                && y1 <= rect.y2 && y2 >= rect.y1;
    }

    public boolean isIntersect(Point point, double radius) {
        Point center = getCenter();
        double circleDistance_x = Math.abs(center.getX() - point.getX());
        double circleDistance_y = Math.abs(center.getY() - point.getY());

        if (circleDistance_x > (getWidth() / 2 + radius)) {
            return false;
        }
        if (circleDistance_y > (getHeight() / 2 + radius)) {
            return false;
        }

        if (circleDistance_x <= (getWidth() / 2)) {
            return true;
        }
        if (circleDistance_y <= (getHeight() / 2)) {
            return true;
        }

        double cornerDistance_sq = Math.pow(circleDistance_x - getWidth()/2, 2) +
                Math.pow(circleDistance_y - getHeight()/2, 2);

        return (cornerDistance_sq <= Math.pow(radius, 2));
    }

    public Quadrant pointInQuadrant(Point point) {
        assert contains(point);
        Point center = getCenter();
        if (point.getX() < center.getX()) {
            return point.getY() < center.getY() ? Quadrant.NW : Quadrant.SW;
        } else {
            return point.getY() < center.getY() ? Quadrant.NE : Quadrant.SE;
        }
    }

    public boolean fitInArea(Rectangle rectangle)
    {
        return x1 >= rectangle.x1 && x2 <= rectangle.x2
                && y1 >= rectangle.y1 && y2 <= rectangle.y2;
    }

    public boolean fitInArea(Point point, double radius)
    {
        double squareRadius = radius * radius;
        return getTopLeft().squareDistance(point) <= squareRadius
                && getTopRight().squareDistance(point) <= squareRadius
                && getBottomLeft().squareDistance(point) <= squareRadius
                && getBottomRight().squareDistance(point) <= squareRadius;
    }

    @Override
    public String toString() {
        return String.format("[%f, %f | %f, %f]", x1, y1, x2, y2);
    }
}
