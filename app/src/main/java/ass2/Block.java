package ass2;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a block
 */
public class Block {
    private List<Integer> connectionIndexes;

    /**
     * Creates a new block
     */
    public Block () {
        connectionIndexes = new ArrayList<>();
    }

    /**
     * Adds a connection index to the list of connection indexes
     * @param connection
     */
    public void addConnectionIndex (int connectionIndex) {
        connectionIndexes.add(connectionIndex);
    }

    /**
     * Returns the list of connection indexes
     * @return the list of connection indexes
     */
    public List<Integer> getConnectionIndexes () {
        return connectionIndexes;
    }
}