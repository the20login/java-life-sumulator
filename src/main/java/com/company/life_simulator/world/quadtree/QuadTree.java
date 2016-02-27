package com.company.life_simulator.world.quadtree;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Datastructure: A point Quad Tree for representing 2D data. Each
 * region has the same ratio as the bounds for the tree.
 * <p/>
 * The implementation currently requires pre-determined bounds for data as it
 * can not rebalance itself to that degree.
 */
public class QuadTree<T> {


    private Node<T> root;
    private int count_ = 0;

    /**
     * Constructs a new quad tree.
     *
     * @param {double} minX Minimum x-value that can be held in tree.
     * @param {double} minY Minimum y-value that can be held in tree.
     * @param {double} maxX Maximum x-value that can be held in tree.
     * @param {double} maxY Maximum y-value that can be held in tree.
     */
    public QuadTree(Rectangle rectangle) {
        this.root = new Node<>(rectangle, null);
    }

    /**
     * Returns a reference to the tree's root node.  Callers shouldn't modify nodes,
     * directly.  This is a convenience for visualization and debugging purposes.
     *
     * @return {Node} The root node.
     */
    public Node getRootNode() {
        return this.root;
    }

    /**
     * Sets the value of an (x, y) point within the quad-tree.
     *
     * @param {double} x The x-coordinate.
     * @param {double} y The y-coordinate.
     * @param {Object} value The value associated with the point.
     */
    public void put(Point point, T value) {

        if (!root.getRectangle().isContains(point)) {
            throw new QuadTreeException("Out of bounds: " + point);
        }
        if (this.insert(root, point, value)) {
            this.count_++;
        }
    }

    /**
     * Gets the value of the point at (x, y) or null if the point is empty.
     *
     * @param {double} x The x-coordinate.
     * @param {double} y The y-coordinate.
     * @param {Object} opt_default The default value to return if the node doesn't
     *                 exist.
     * @return {*} The value of the node, the default value if the node
     *         doesn't exist, or undefined if the node doesn't exist and no default
     *         has been provided.
     */
    public Optional<T> get(Point point) {
        Optional<Node<T>> optional = this.find(this.root, point);
        return optional.isPresent() ? Optional.of(optional.get().getValue()) : Optional.empty();
    }

    /**
     * Removes a point from (x, y) if it exists.
     *
     * @param {double} x The x-coordinate.
     * @param {double} y The y-coordinate.
     * @return {Object} The value of the node that was removed, or null if the
     *         node doesn't exist.
     */
    public Optional<T> remove(Point point) {
        Optional<Node<T>> optional = this.find(this.root, point);
        if (optional.isPresent()) {
            Node<T> node = optional.get();
            T value = node.getValue();
            node.setPoint(null, null);
            node.setNodeType(NodeType.EMPTY);
            this.balance(node);
            this.count_--;
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns true if the point at (x, y) exists in the tree.
     *
     * @param {double} x The x-coordinate.
     * @param {double} y The y-coordinate.
     * @return {boolean} Whether the tree contains a point at (x, y).
     */
    public boolean contains(Point point) {
        return this.get(point).isPresent();
    }

    /**
     * @return {boolean} Whether the tree is empty.
     */
    public boolean isEmpty() {
        return this.root.getNodeType() == NodeType.EMPTY;
    }

    /**
     * @return {number} The number of items in the tree.
     */
    public int getCount() {
        return this.count_;
    }

    /**
     * Removes all items from the tree.
     */
    public void clear() {
        root.clear();
        this.count_ = 0;
    }

    public Stream<T> getValues() {
        Stream.Builder<Node<T>> streamBuilder = Stream.builder();
        this.traverse(this.root, streamBuilder::add);
        return streamBuilder.build()
                .map(Node::getValue);
    }

    public Stream<T> searchWithin(Rectangle rectangle) {
        Stream.Builder<Node<T>> streamBuilder = Stream.builder();
        this.navigate(this.root, rectangle, streamBuilder::add);
        return streamBuilder.build()
            .map(Node::getValue);
    }

    public Stream<T> searchWithin(Point point, double radius) {
        Stream.Builder<Node<T>> streamBuilder = Stream.builder();
        this.navigate(this.root, point, radius, streamBuilder::add);
        return streamBuilder.build()
            .map(Node::getValue);
    }

    private void navigate(Node<T> node, Point point, double radius, Consumer<Node<T>> consumer) {
        switch (node.getNodeType()) {
            case LEAF:
                if (node.getPoint().withinCircle(point, radius))
                    consumer.accept(node);
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

    private void navigate(Node<T> node, Rectangle rectangle, Consumer<Node<T>> consumer) {
        switch (node.getNodeType()) {
            case LEAF:
                if (rectangle.isContains(node.getPoint()))
                    consumer.accept(node);
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

    /**
     * Traverses the tree depth-first, with quadrants being traversed in clockwise
     * order (NE, SE, SW, NW).  The provided function will be called for each
     * leaf node that is encountered.
     * @param {QuadTree.Node} node The current node.
     * @param {function(QuadTree.Node)} fn The function to call
     *     for each leaf node. This function takes the node as an argument, and its
     *     return value is irrelevant.
     * @private
     */
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

    /**
     * Finds a leaf node with the same (x, y) coordinates as the target point, or
     * null if no point exists.
     * @param {QuadTree.Node} node The node to search in.
     * @param {number} x The x-coordinate of the point to search for.
     * @param {number} y The y-coordinate of the point to search for.
     * @return {QuadTree.Node} The leaf node that matches the target,
     *     or null if it doesn't exist.
     * @private
     */
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

    /**
     * Inserts a point into the tree, updating the tree's structure if necessary.
     * @param {.QuadTree.Node} parent The parent to insert the point
     *     into.
     * @param {QuadTree.Point} point The point to insert.
     * @return {boolean} True if a new node was added to the tree; False if a node
     *     already existed with the correpsonding coordinates and had its value
     *     reset.
     * @private
     */
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

    /**
     * Converts a leaf node to a pointer node and reinserts the node's point into
     * the correct child.
     * @param {QuadTree.Node} node The node to split.
     * @private
     */
    private void split(Node<T> node) {
        Point oldPoint = node.getPoint();
        T oldValue = node.getValue();

        node.clearAndSplit();

        this.insert(node, oldPoint, oldValue);
    }

    /**
     * Attempts to balance a node. A node will need balancing if all its children
     * are empty or it contains just one leaf.
     * @param {QuadTree.Node} node The node to balance.
     * @private
     */
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

    /**
     * Sets the point for a node, as long as the node is a leaf or empty.
     * @param {QuadTree.Node} node The node to put the point for.
     * @param {QuadTree.Point} point The point to put.
     * @private
     */
    private void setPointForNode(Node<T> node, Point point, T value) {
        if (node.getNodeType() == NodeType.POINTER) {
            throw new QuadTreeException("Can not put point for node of type POINTER");
        }
        node.setNodeType(NodeType.LEAF);
        node.setPoint(point, value);
    }
}

