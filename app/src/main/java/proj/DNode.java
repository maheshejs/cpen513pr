package proj;

/**
 * Represents a node in a doubly linked list
 */
public class DNode<E> {
    E e;
    DNode<E> next;
    DNode<E> prev;

    /**
     * Creates a new DNode
     * @param e the element of the node
     */
    public DNode(E e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return String.valueOf(e);
    }
}