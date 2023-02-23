package ass3;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a connection
 */
public class Connection {
    private List<Integer> blockIndexes;

    /**
     * Creates a new connection
     */
    public Connection () {
        blockIndexes = new ArrayList<>();
    }

    /**
     * Adds a block index to the list of block indexes
     * @param blockIndex the index of the block
     */
    public void addBlockIndex (int blockIndex) {
        blockIndexes.add(blockIndex);
    }

    /**
     * Returns the list of connection indexes
     * @return the list of connection indexes
     */
    public List<Integer> getBlockIndexes () {
        return blockIndexes;
    }
}