package ass3;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a doubly linked list
 */
public class DoublyLinkedList<E> {
	private DNode<E> head;
	private int size = 0;

	public void add(DNode<E> node) {
        node.next = head;
        if (head != null)
            head.prev = node;
        head = node;
        node.prev = null;
        ++size;
	}
 
	public void remove(DNode<E> node) {
        if (node.prev != null)
            node.prev.next = node.next;
        else
            head = node.next;
    
        if (node.next != null)
            node.next.prev = node.prev;
        --size;
	}

    public E peek () {
        if (head == null)
            throw new NoSuchElementException();
        return head.e;
    }

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

    @Override
    public String toString() {
        List<E> es = new ArrayList<E>();
        DNode<E> node = head;
        while (node != null) {
            es.add(node.e);
            node = node.next;
        }
        return es.toString();
    }

}