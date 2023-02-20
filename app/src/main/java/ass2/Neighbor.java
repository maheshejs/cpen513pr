package ass2;

import java.util.Random;

import javafx.geometry.Point2D;

/**
 * Represents a neighbor
 */
public class Neighbor {
    int firstLocIndex;
    int secondLocIndex;
    double moveCost;

    /**
     * Creates a new neighbor
     * @param solution the solution
     * @param blocks the blocks
     * @param connections the connections
     * @param locs the locations
     * @param random the random number generator
     */
    public Neighbor(int[] solution, Block[] blocks, Connection[] connections, Point2D[] locs, Random random) {
        // Select two random locations or the first two locations if random is null for testing (deterministic behavio)
        firstLocIndex  = random != null ? random.nextInt(locs.length) : 0;
        secondLocIndex = random != null ? random.nextInt(locs.length) : 1;
        moveCost = 0.0;

        int firstBlockIndex  = solution[firstLocIndex];
        int secondBlockIndex = solution[secondLocIndex];
        Point2D firstLoc  = locs[firstLocIndex];
        Point2D secondLoc = locs[secondLocIndex];
        
        // Make move and calculate move cost
        if (firstBlockIndex < blocks.length) {
            for (int connectionIndex : blocks[firstBlockIndex].getConnectionIndexes()) {
                moveCost -= connections[connectionIndex].getCost();
                connections[connectionIndex].moveBlockPlacement(firstBlockIndex, firstLoc, secondLoc);
                moveCost += connections[connectionIndex].getCost();
            }
        }
        if (secondBlockIndex < blocks.length) {
            for (int connectionIndex : blocks[secondBlockIndex].getConnectionIndexes()) {
                moveCost -= connections[connectionIndex].getCost();
                connections[connectionIndex].moveBlockPlacement(secondBlockIndex, secondLoc, firstLoc);
                moveCost += connections[connectionIndex].getCost();
            }
        }
        
        // Undo move
        if (firstBlockIndex < blocks.length)
            for (int connectionIndex : blocks[firstBlockIndex].getConnectionIndexes())
                connections[connectionIndex].moveBlockPlacement(firstBlockIndex, secondLoc, firstLoc);
        if (secondBlockIndex < blocks.length)
            for (int connectionIndex : blocks[secondBlockIndex].getConnectionIndexes())
                connections[connectionIndex].moveBlockPlacement(secondBlockIndex, firstLoc, secondLoc);
    }

    /**
     * Returns the first location index
     * @return the first location index
     */
    public int getFirstLocIndex () {
        return firstLocIndex;
    }

    /**
     * Returns the second location index
     * @return the second location index
     */
    public int getSecondLocIndex () {
        return secondLocIndex;
    }

    /**
     * Returns the move cost
     * @return the move cost
     */
    public double getMoveCost () {
        return moveCost;
    }
}