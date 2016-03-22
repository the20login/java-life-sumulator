package com.company.life_simulator.world.quadtree;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data structure: A point Quad Tree for representing 2D data. Each
 * region has the same ratio as the bounds for the tree.
 * <p/>
 * The implementation currently requires pre-determined bounds for data as it
 * can not rebalance itself to that degree.
 */
public class QuadTree<T> {


    private final Node<T> root;
    private int count = 0;

    /**
     * Creates new instance of tree
     *
     * @param rectangle determine area, covered by tree
     */
    public QuadTree(Rectangle rectangle) {
        this.root = new Node<>(rectangle, null);
    }

    public void put(Point point, T value) {

        if (!root.getRectangle().contains(point)) {
            throw new QuadTreeException("Out of bounds: " + point);
        }
        if (this.insert(root, point, value)) {
            this.count++;
        }
    }

    /**
     * @return Area, covered by tree
     */
    public Rectangle getSize()
    {
        return root.getRectangle();
    }

    /**
     * Gets the value of the point
     * <br>
     * Note: this method require absolute precision({@link Point#equals Point.equals}), use {@link #searchWithin} to search with custom precision
     *
     * @param point coordinates
     * @return Optional value at point
     */
    public Optional<T> get(Point point) {
        Optional<Node<T>> optional = this.find(this.root, point);
        return optional.isPresent() ? Optional.of(optional.get().getValue()) : Optional.empty();
    }

    /**
     * Removes element from tree. Tree will rebalance itself after removing.
     * <br>
     * Note: this method require absolute precision({@link Point#equals Point.equals})
     *
     * @param point coordinates
     * @return Optional removed element, empty if there is no element at provided coordinates
     */
    public Optional<T> remove(Point point) {
        Optional<Node<T>> optional = this.find(this.root, point);
        if (optional.isPresent()) {
            Node<T> node = optional.get();
            T value = node.getValue();
            node.setPoint(null, null);
            node.setNodeType(NodeType.EMPTY);
            this.balance(node);
            this.count--;
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Checks if there is an element at this point
     *
     * @param point coordinates
     * @return True if tree contains element at this point, otherwise false.
     */
    public boolean contains(Point point) {
        return this.get(point).isPresent();
    }

    /**
     * @return Whether the tree is empty.
     */
    public boolean isEmpty() {
        return this.root.getNodeType() == NodeType.EMPTY;
    }

    /**
     * @return The number of elements in the tree.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Removes all elements from the tree.
     */
    public void clear() {
        root.clear();
        this.count = 0;
    }

    /**
     * Returns all elements of the tree
     * @return Stream of tree elements
     */
    public Stream<T> getValues() {
        Stream.Builder<Node<T>> streamBuilder = Stream.builder();
        this.traverse(this.root, streamBuilder::add);
        return streamBuilder.build()
                .map(Node::getValue);
    }

    /**
     * Returns elements within rectangle(inclusive)
     * @return Stream of tree elements
     */
    public Stream<T> searchWithin(Rectangle rectangle) {
        Stream.Builder<T> streamBuilder = Stream.builder();
        this.navigate(this.root, rectangle, streamBuilder::add);
        return streamBuilder.build();
    }

    /**
     * Returns elements within circle(inclusive)
     * @return Stream of tree elements
     */
    public Stream<T> searchWithin(Point point, double radius) {
        Stream.Builder<T> streamBuilder = Stream.builder();
        this.navigate(this.root, point, radius, streamBuilder::add);
        return streamBuilder.build();
    }

    private void navigate(Node<T> node, Point point, double radius, Consumer<T> consumer) {
        switch (node.getNodeType()) {
            case LEAF:
                if (node.getPoint().withinCircle(point, radius))
                    consumer.accept(node.getValue());
                break;
            case POINTER:
                //looks ugly, but works 1.5 times faster, than enumMap/Array/Stream
                if (node.getNeNode().getRectangle().isIntersect(point, radius))
                    this.navigate(node.getNeNode(), point, radius, consumer);
                if (node.getSeNode().getRectangle().isIntersect(point, radius))
                    this.navigate(node.getSeNode(), point, radius, consumer);
                if (node.getSwNode().getRectangle().isIntersect(point, radius))
                    this.navigate(node.getSwNode(), point, radius, consumer);
                if (node.getNwNode().getRectangle().isIntersect(point, radius))
                    this.navigate(node.getNwNode(), point, radius, consumer);
                break;
        }
    }

    private void navigate(Node<T> node, Rectangle rectangle, Consumer<T> consumer) {
        switch (node.getNodeType()) {
            case LEAF:
                if (rectangle.contains(node.getPoint()))
                    consumer.accept(node.getValue());
                break;
            case POINTER:
                //looks ugly, but works 1.5 times faster, than enumMap/Array/Stream
                if (rectangle.isIntersect(node.getNeNode().getRectangle()))
                    this.navigate(node.getNeNode(), rectangle, consumer);
                if (rectangle.isIntersect(node.getSeNode().getRectangle()))
                    this.navigate(node.getSeNode(), rectangle, consumer);
                if (rectangle.isIntersect(node.getSwNode().getRectangle()))
                    this.navigate(node.getSwNode(), rectangle, consumer);
                if (rectangle.isIntersect(node.getNwNode().getRectangle()))
                    this.navigate(node.getNwNode(), rectangle, consumer);
        }
    }

    /**
     * Clones the quad-tree and returns the new instance.
     * @return {QuadTree} A clone of the tree.
     */
    public QuadTree<T> clone() {
        final QuadTree<T> clone = new QuadTree<>(this.root.getRectangle());
        // This is inefficient as the clone needs to recalculate the structure of the
        // tree, even though we know it already.  But this is easier and can be
        // optimized when/if needed.
        this.traverse(this.root, node -> clone.put(node.getPoint(), node.getValue()));

        return clone;
    }

    private void traverse(Node<T> node, Consumer<Node<T>> consumer) {
        switch (node.getNodeType()) {
            case LEAF:
                consumer.accept(node);
                break;
            case POINTER:
                node.getChildNodes()
                        .forEach(childNode -> this.traverse(childNode, consumer));
                break;
        }
    }

    private Optional<Node<T>> find(Node<T> node, Point point) {
        switch (node.getNodeType()) {
            case EMPTY:
                return Optional.empty();
            case LEAF:
                return node.getPoint().equals(point) ? Optional.of(node) : Optional.empty();
            case POINTER:
                return this.find(node.getQuadrantNode(point), point);
            default:
                throw new QuadTreeException("Invalid nodeType");
        }
    }

    private boolean insert(Node<T> parent, Point point, T value) {
        Boolean result;
        switch (parent.getNodeType()) {
            case EMPTY:
                this.setPointForNode(parent, point, value);
                result = true;
                break;
            case LEAF:
                if (parent.getPoint().getX() == point.getX() && parent.getPoint().getY() == point.getY()) {
                    this.setPointForNode(parent, point, value);
                    result = false;
                } else {
                    this.split(parent);
                    result = this.insert(parent, point, value);
                }
                break;
            case POINTER:
                result = this.insert(parent.getQuadrantNode(point), point, value);
                break;

            default:
                throw new QuadTreeException("Invalid nodeType in parent");
        }
        return result;
    }

    private void split(Node<T> node) {
        Point oldPoint = node.getPoint();
        T oldValue = node.getValue();

        node.clearAndSplit();

        this.insert(node, oldPoint, oldValue);
    }

    private void balance(Node<T> node) {
        switch (node.getNodeType()) {
            case EMPTY:
            case LEAF:
                if (node.getParent() != null) {
                    this.balance(node.getParent());
                }
                break;

            case POINTER: {
                List<Node<T>> nonEmptyNodes =  node.getChildNodes()
                        .filter(childNode -> childNode.getNodeType() != NodeType.EMPTY)
                        .collect(Collectors.toList());

                if (nonEmptyNodes.size() > 1)
                {
                    //this node can't be balanced
                    break;
                }

                if (nonEmptyNodes.isEmpty()) {
                    // All child nodes are empty: so make this node empty.
                    node.clear();

                }
                else
                {
                    Node<T> firstLeaf = nonEmptyNodes.get(0);
                    if (firstLeaf.getNodeType() == NodeType.POINTER) {
                        // Only child was a pointer, therefore we can't rebalance.
                        break;
                    }
                    else
                    {
                        // Only child was a leaf: so update node's point and make it a leaf.
                        node.setPoint(firstLeaf.getPoint(), firstLeaf.getValue());
                    }
                }

                // Try and balance the parent as well.
                if (node.getParent() != null) {
                    this.balance(node.getParent());
                }
            }
            break;
        }
    }

    private void setPointForNode(Node<T> node, Point point, T value) {
        if (node.getNodeType() == NodeType.POINTER) {
            throw new QuadTreeException("Can not put point for node of type POINTER");
        }
        node.setNodeType(NodeType.LEAF);
        node.setPoint(point, value);
    }
}

