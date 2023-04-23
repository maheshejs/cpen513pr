package proj;

import java.util.Set;
import java.util.HashSet;

/**
 * Represents a block
 */
public class Block {
    private Set<Integer> connectionIndexes;

    /**
     * Creates a new block
     */
    public Block () {
        connectionIndexes = new HashSet<>();
    }

    /**
     * Adds a connection index to the set of connection indexes
     * @param connection
     */
    public void addConnectionIndex (int connectionIndex) {
        connectionIndexes.add(connectionIndex);
    }

    /**
     * Returns the set of connection indexes
     * @return the set of connection indexes
     */
    public Set<Integer> getConnectionIndexes () {
        return connectionIndexes;
    }
}