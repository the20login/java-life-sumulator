package com.company.life_simulator.world.quadtree;

import java.util.EnumMap;

public class Node<T> {

    private Rectangle rect;
    private Node<T> opt_parent;
    private Point point;
    private NodeType nodetype = NodeType.EMPTY;
    private EnumMap<Quadrant, Node<T>> childNodes = new EnumMap<>(Quadrant.class);
    private T value;

    /**
     * Constructs a new quad tree node.
     *
     * @param {double} x X-coordiate of node.
     * @param {double} y Y-coordinate of node.
     * @param {double} w Width of node.
     * @param {double} h Height of node.
     * @param {Node}   opt_parent Optional parent node.
     * @constructor
     */
    public Node(Rectangle rect, Node<T> opt_parent) {
        this.rect = rect;
        this.opt_parent = opt_parent;
    }

    public Rectangle getRectangle() {
        return rect;
    }

    public Node<T> getParent() {
        return opt_parent;
    }

    public void setParent(Node<T> opt_parent) {
        this.opt_parent = opt_parent;
    }

    public void setPoint(Point point, T payload) {
        clear();
        nodetype = NodeType.LEAF;
        this.point = point;
        this.value = payload;
    }

    public Point getPoint() {
        return this.point;
    }

    public void setNodeType(NodeType nodetype) {
        this.nodetype = nodetype;
    }

    public NodeType getNodeType() {
        return this.nodetype;
    }

    public void clearAndSplit()
    {
        clear();
        nodetype = NodeType.POINTER;

        Point point = rect.getTopLeft();
        double halfWidth = rect.getWidth() / 2;
        double halfHeight = rect.getHeight() / 2;

        childNodes.put(Quadrant.NW, new Node<>(new Rectangle(point, halfWidth, halfHeight), this));
        childNodes.put(Quadrant.NE, new Node<>(new Rectangle(point.delta(halfWidth, 0), halfWidth, halfHeight), this));
        childNodes.put(Quadrant.SW, new Node<>(new Rectangle(point.delta(0, halfHeight), halfWidth, halfHeight), this));
        childNodes.put(Quadrant.SE, new Node<>(new Rectangle(point.delta(halfWidth, halfHeight), halfWidth, halfHeight), this));
    }

    public void clear()
    {
        nodetype = NodeType.EMPTY;
        childNodes.clear();
        value = null;
    }

    public T getValue() {
        return value;
    }

    public Node<T> getQuadrantNode(Point point)
    {
        Point middle = rect.getCenter();
        if (point.getX() < middle.getX()) {
            return point.getY() < middle.getY() ? childNodes.get(Quadrant.NW) : childNodes.get(Quadrant.SW);
        } else {
            return point.getY() < middle.getY() ? childNodes.get(Quadrant.NE) : childNodes.get(Quadrant.SE);
        }
    }

    public Node<T> getChildNode(Quadrant quadrant) {
        return childNodes.get(quadrant);
    }
}
