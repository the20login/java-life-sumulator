package com.company.life_simulator.world.quadtree;

import java.util.stream.Stream;

public class Node<T> {

    private final Rectangle rect;
    private final Node<T> opt_parent;
    private Point point;
    private NodeType nodetype = NodeType.EMPTY;
    private T value;
    private Node<T> nwNode;
    private Node<T> neNode;
    private Node<T> swNode;
    private Node<T> seNode;

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

        this.nwNode = new Node<>(new Rectangle(point, halfWidth, halfHeight), this);
        this.neNode = new Node<>(new Rectangle(point.delta(halfWidth, 0), halfWidth, halfHeight), this);
        this.swNode = new Node<>(new Rectangle(point.delta(0, halfHeight), halfWidth, halfHeight), this);
        this.seNode = new Node<>(new Rectangle(point.delta(halfWidth, halfHeight), halfWidth, halfHeight), this);
    }

    public void clear()
    {
        nodetype = NodeType.EMPTY;
        this.nwNode = null;
        this.neNode = null;
        this.swNode = null;
        this.seNode = null;
        value = null;
    }

    public Node<T> getNwNode() {
        return nwNode;
    }

    public Node<T> getNeNode() {
        return neNode;
    }

    public Node<T> getSwNode() {
        return swNode;
    }

    public Node<T> getSeNode() {
        return seNode;
    }

    public T getValue() {
        return value;
    }

    public Node<T> getQuadrantNode(Point point)
    {
        Point middle = rect.getCenter();
        if (point.getX() < middle.getX()) {
            return point.getY() < middle.getY() ? nwNode : swNode;
        } else {
            return point.getY() < middle.getY() ? neNode : seNode;
        }
    }

    public Stream<Node<T>> getChildNodes()
    {
        return Stream.of(neNode, seNode, swNode, nwNode);
    }
}
