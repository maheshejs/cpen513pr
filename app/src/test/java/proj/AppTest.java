
package proj;

import java.util.List;
import java.util.Set;
import java.util.Random;

import static proj.Utils.*;
import javafx.geometry.Point2D;
import javafx.util.Pair;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    /////////////////////////
    /// example benchmark ///
    /////////////////////////
    //       3 3 2 2       // 
    //       3 0 1 2       // 
    //       2 2 0         //
    //       2 1 2         //
    /////////////////////////
    Benchmark    benchmark   = new Benchmark("example", true);
    Block[]      blocks      = benchmark.getBlocks();
    Connection[] connections = benchmark.getConnections();
    Point2D[]    locs        = benchmark.getLocs();
    int[]        solution    = new int[locs.length];
    // Define random as null to ensure that the random number generator is not used
    private static final Random random = null; 

    @Test void testBlock () {
        assertEquals(benchmark.getNumBlocks(), 3);
        assertEquals(blocks.length, 3);
        assertEquals(blocks[0].getConnectionIndexes(), Set.of(0, 1));
        assertEquals(blocks[1].getConnectionIndexes(), Set.of(0, 2));
        assertEquals(blocks[2].getConnectionIndexes(), Set.of(0, 1, 2));
    }
    
    @Test void testConnection () {
        Point2D defaultLoc = new Point2D(0, 0);

        assertEquals(benchmark.getNumConnections(), 3);
        assertEquals(connections.length, 3);
        assertEquals(connections[0].getBlockPlacements(), List.of(new Pair<>(0, defaultLoc), new Pair<>(1, defaultLoc), new Pair<>(2, defaultLoc)));
        assertEquals(connections[1].getBlockPlacements(), List.of(new Pair<>(2, defaultLoc), new Pair<>(0, defaultLoc)));
        assertEquals(connections[2].getBlockPlacements(), List.of(new Pair<>(1, defaultLoc), new Pair<>(2, defaultLoc)));
    }
    
    @Test void testLoc () {
        int numRows = benchmark.getNumRows();
        int numCols = benchmark.getNumCols();
        int numLocs = benchmark.getNumLocs();
        boolean useRowSpacing = benchmark.getUseRowSpacing();

        assertEquals(benchmark.getNumRows(), 2);
        assertEquals(benchmark.getNumCols(), 2);
        assertEquals(benchmark.getNumLocs(), 4);
        assertEquals(locs.length, 4);
        assertEquals(getLoc(0, numRows, numCols, useRowSpacing), new Point2D(0.0, 0.0));
        assertEquals(getLoc(1, numRows, numCols, useRowSpacing), new Point2D(1.0, 0.0));
        assertEquals(getLoc(2, numRows, numCols, useRowSpacing), new Point2D(0.0, 2.0));
        assertEquals(getLoc(3, numRows, numCols, useRowSpacing), new Point2D(1.0, 2.0));
    }

    @Test void testCost () {
        final int NUM_TESTS = 6;
        double[][] costs  = new double[NUM_TESTS][connections.length];
        int[][] solutions = new int[NUM_TESTS][locs.length];

        // Set initial solution to [0, 1, 2, 3]
        App.initializeBlockPlacements(solution, blocks, connections, locs, random);
        
        for (int i = 0; i < NUM_TESTS; ++i) {
            int firstLocIndex  = 2 - (i & 1); // 2 or 1
            int secondLocIndex = 3;
            App.swapBlockPlacements(solution, blocks, connections, locs, firstLocIndex, secondLocIndex);
            solutions[i] = solution.clone();
            costs[i][0]  = connections[0].getCost();
            costs[i][1]  = connections[1].getCost();
            costs[i][2]  = connections[2].getCost();
        }
        
        assertArrayEquals(solutions[0], new int[] {0, 1, 3, 2});
        assertEquals(costs[0][0], 3.0);
        assertEquals(costs[0][1], 3.0);
        assertEquals(costs[0][2], 2.0);
        // total cost = 3 + 3 + 2 = 8

        assertArrayEquals(solutions[1], new int[] {0, 2, 3, 1});
        assertEquals(costs[1][0], 3.0);
        assertEquals(costs[1][1], 1.0);
        assertEquals(costs[1][2], 2.0);
        // total cost = 3 + 1 + 2 = 6

        assertArrayEquals(solutions[2], new int[] {0, 2, 1, 3});
        assertEquals(costs[2][0], 3.0);
        assertEquals(costs[2][1], 1.0);
        assertEquals(costs[2][2], 3.0);
        // total cost = 3 + 1 + 3 = 7

        assertArrayEquals(solutions[3], new int[] {0, 3, 1, 2});
        assertEquals(costs[3][0], 3.0);
        assertEquals(costs[3][1], 3.0);
        assertEquals(costs[3][2], 1.0);
        // total cost = 3 + 3 + 1 = 7

        assertArrayEquals(solutions[4], new int[] {0, 3, 2, 1});
        assertEquals(costs[4][0], 3.0);
        assertEquals(costs[4][1], 2.0);
        assertEquals(costs[4][2], 1.0);
        // total cost = 3 + 2 + 1 = 6

        assertArrayEquals(solutions[5], new int[] {0, 1, 2, 3});
        assertEquals(costs[5][0], 3.0);
        assertEquals(costs[5][1], 2.0);
        assertEquals(costs[5][2], 3.0);
        // total cost = 3 + 2 + 3 = 8
    }
    
    @Test void testNeighbor () {
        // Set initial solution to [0, 1, 2, 3]
        App.initializeBlockPlacements(solution, blocks, connections, locs, random);
        // Change solution to [0, 3, 2, 1]
        App.swapBlockPlacements(solution, blocks, connections, locs, 1, 3);
        // Generate a neighbor by swapping locs 0 and 1
        Neighbor neighbor = new Neighbor(solution, blocks, connections, locs, random);
        
        assertArrayEquals(solution, new int[] {0, 3, 2, 1});
        assertEquals(neighbor.getFirstLocIndex(), 0);
        assertEquals(neighbor.getSecondLocIndex(), 1);
        assertEquals(neighbor.getMoveCost(), 1.0);
    }
}
