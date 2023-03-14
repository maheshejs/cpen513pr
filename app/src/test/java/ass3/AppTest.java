
package ass3;

import java.util.List;
import java.util.Set;
import java.util.BitSet;
import static ass3.Constants.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    /////////////////////////
    /// example benchmark ///
    /////////////////////////
    //       8 4           // 
    //       2 0 4         // 
    //       2 1 5         //
    //       2 2 6         //
    //       2 3 7         //
    /////////////////////////
    Benchmark    benchmark   = new Benchmark("example.txt");
    Block[]      blocks      = benchmark.getBlocks();
    Connection[] connections = benchmark.getConnections();

    @Test void testBlock () {
        assertEquals(benchmark.getNumBlocks(), 8);
        assertEquals(blocks.length, 8);
        assertEquals(blocks[0].getConnectionIndexes(), Set.of(0));
        assertEquals(blocks[1].getConnectionIndexes(), Set.of(1));
        assertEquals(blocks[2].getConnectionIndexes(), Set.of(2));
        assertEquals(blocks[3].getConnectionIndexes(), Set.of(3));
        assertEquals(blocks[4].getConnectionIndexes(), Set.of(0));
        assertEquals(blocks[5].getConnectionIndexes(), Set.of(1));
        assertEquals(blocks[6].getConnectionIndexes(), Set.of(2));
        assertEquals(blocks[7].getConnectionIndexes(), Set.of(3));
    }
    
    @Test void testConnection () {
        assertEquals(benchmark.getNumConnections(), 4);
        assertEquals(connections.length, 4);
        assertEquals(connections[0].getBlockIndexes(), List.of(0, 4));
        assertEquals(connections[1].getBlockIndexes(), List.of(1, 5));
        assertEquals(connections[2].getBlockIndexes(), List.of(2, 6));
        assertEquals(connections[3].getBlockIndexes(), List.of(3, 7));
    }

    @Test void testTrivialSolution () {
        ///////////////////////
        // Expected solution //
        ///////////////////////
        // 0 1 2 3 | 4 5 6 7 //
        ///////////////////////
        App.partitionBenchmarkTriv(benchmark);
        int numBlocks = benchmark.getNumBlocks();
        BitSet expSolution = new BitSet(numBlocks);
        expSolution.set(0, numBlocks / NUM_PARTITIONS);
        int expSolutionCost = 4;
        assertEquals(benchmark.getSolution(), expSolution);
        assertEquals(benchmark.getSolutionCost(), expSolutionCost);
    }

    @Test void testFMSolution1Pass () {
        ///////////////////////
        // Expected solution //
        ///////////////////////
        // 1 3 5 7 | 0 2 4 6 //
        ///////////////////////
        App.partitionBenchmarkTriv(benchmark);
        BitSet solution = benchmark.getSolution();
        int solutionCost = benchmark.getSolutionCost();

        App.partitionBenchmarkFM(benchmark, solution, solutionCost, 1);
        int numBlocks = benchmark.getNumBlocks();
        BitSet expSolution = new BitSet(numBlocks);
        expSolution.set(1);
        expSolution.set(3);
        expSolution.set(5);
        expSolution.set(7);
        int expSolutionCost = 0;
        assertEquals(benchmark.getSolution(), expSolution);
        assertEquals(benchmark.getSolutionCost(), expSolutionCost);
    }
    
    @Test void testBBSolution () {
        ///////////////////////
        // Expected solution //
        ///////////////////////
        // 0 1 4 5 | 2 3 6 7 //
        ///////////////////////
        App.partitionBenchmarkTriv(benchmark);
        BitSet solution = benchmark.getSolution();
        int solutionCost = benchmark.getSolutionCost();

        App.partitionBenchmarkBB(benchmark, solution, solutionCost);
        int numBlocks = benchmark.getNumBlocks();
        BitSet expSolution = new BitSet(numBlocks);
        expSolution.set(0);
        expSolution.set(1);
        expSolution.set(4);
        expSolution.set(5);
        int expSolutionCost = 0;
        assertEquals(benchmark.getSolution(), expSolution);
        assertEquals(benchmark.getSolutionCost(), expSolutionCost);
    }
}
