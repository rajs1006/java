package de.funke.tools.testutil.utils;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * This class is to maintain the hierarchy tree.
 *
 * @param <T> Data type.i.e; String.
 * @author sraj 28-Sep-2018
 */
public class Tree<T> {

    /**
     * Data, like URL.
     */
    private T data;

    /**
     * The head and parent
     */
    private Tree<T> parent;
    /**
     * List of children
     */
    private List<Tree<T>> child;
    /**
     * Boolean to decide if T type data is present or not.
     */
    private boolean isPresent;

    /**
     * Constructor
     *
     * @param data Data, like URL.
     */
    public Tree(T data) {
        this.data = data;
        isPresent = true;
        this.child = new LinkedList<>();
    }

    /**
     * Secondary Constructor
     *
     * @param data      Data, like URL.
     * @param isPresent Boolean if data is present or not.
     */
    public Tree(T data, boolean isPresent) {
        this.data = data;
        this.isPresent = isPresent;
        this.child = new LinkedList<>();
    }

    /**
     * use the data of child and parent to build the hierarchy.
     *
     * @param child  Child data
     * @param parent Parent data
     */
    public void addChild(@Nonnull T child, T parent) {
        Tree<T> node = get(parent, true);
        Tree<T> childNode = new Tree<>(child);

        node.child.add(childNode);
        childNode.parent = node;
        childNode.isPresent = true;
    }

    /**
     * Checks if Tree contains particular data or not.
     *
     * @param data Data to be checked
     * @return True, if present else false.
     */
    public boolean contains(@Nonnull T data) {
        return get(data, false).isPresent;
    }

    /**
     * Return the child and its immediate parent for logginbg purpose.
     *
     * @param data Child data.
     * @return String message for logging.
     */
    public String toString(@Nonnull T data) {
        Tree<T> parent = get(data, false).parent;
        return (data + (parent != null ? (" is child of " + parent.data) : ""));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tree)) return false;
        Tree<?> tree = (Tree<?>) o;
        return Objects.equals(data, tree.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    /**
     * Returns already present Tree or crate a new one to return.
     *
     * @param data      Data need to be checked if present or not.
     * @param isPresent Boolean, to initialize the tree.
     * @return New or already existing instance of Tree.
     */
    private Tree<T> get(T data, boolean isPresent) {
        if (data != null) {
            if (this.data.equals(data)) {
                return this;
            } else {
                for (Tree<T> d : this.child) {
                    if (d.data.equals(data)) {
                        return d;
                    }
                }
            }
        }
        return new Tree<>(data, isPresent);
    }
}
