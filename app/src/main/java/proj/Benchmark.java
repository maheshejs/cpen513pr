package proj;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.Scanner;
import java.util.BitSet;
import java.io.File;
import java.io.FileNotFoundException;

public class Benchmark {
    private int numBlocks;
    private int numConnections;
    private Block[] blocks;
    private Connection[] connections;
    private BitSet solution;
    private int solutionCost;
    private int[] absoluteBlockIndexes;

    public Benchmark (Block[] blocks, Connection[] connections, int[] absoluteBlockIndexes) {
        this.numBlocks = blocks.length;
        this.numConnections = connections.length;
        this.blocks = blocks;
        this.connections = connections;
        this.solution = new BitSet(numBlocks);
        this.solutionCost = -1;
        this.absoluteBlockIndexes = absoluteBlockIndexes;
    }

    /**
     * Creates a new benchmark
     * @param benchmarkFile the benchmark file
     */
    public Benchmark (String benchmarkFile) {
        parseBenchmarkFile(benchmarkFile);
    }

    /**
     * Parses the benchmark file
     * @param benchmarkFile the benchmark file
     */
    private void parseBenchmarkFile (String benchmarkFile) {
        try (Scanner scanner = new Scanner(new File("data/benchmarks/ass3/" + benchmarkFile))) {
            numBlocks = scanner.nextInt();
            numConnections = scanner.nextInt();

            blocks = new Block[numBlocks];
            absoluteBlockIndexes = new int[numBlocks];
            for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex)
            {
                blocks[blockIndex] = new Block();
                absoluteBlockIndexes[blockIndex] = blockIndex;
            }
            
            connections = new Connection[numConnections];
            for (int connectionIndex = 0; connectionIndex < numConnections; ++connectionIndex) {
                connections[connectionIndex] = new Connection();
                int numBlocksConnection = scanner.nextInt();
                while (numBlocksConnection-- > 0) {
                    int blockIndex = scanner.nextInt();
                    blocks[blockIndex].addConnectionIndex(connectionIndex);
                    connections[connectionIndex].addBlockIndex(blockIndex);
                }
            }
            solution = new BitSet(numBlocks);
            solutionCost = -1;
        } catch (FileNotFoundException e) {
            System.err.println("Unable to load benchmark file: " + benchmarkFile);
            System.exit(1);
        }
    }
    
    /**
     * Returns blocks
     * @return blocks
     */
    public Block[] getBlocks () {
        return blocks;
    }
    
    /**
     * Returns connections
     * @return connections
     */
    public Connection[] getConnections () {
        return connections;
    }

    /**
     * Returns the number of blocks
     * @return the number of blocks
     */
    public int getNumBlocks () {
        return numBlocks;
    }
    
    /**
     * Returns the number of connections
     * @return the number of connections
     */
    public int getNumConnections () {
        return numConnections;
    }

    /**
     * Returns the solution
     * @return the solution
     */
    public BitSet getSolution () {
        return solution;
    }

    /**
     * Sets the solution
     * @param solution the solution
     */
    public void setSolution (BitSet solution) {
        this.solution = solution;
    }

    /**
     * Returns the solution cost
     * @return the solution cost
     */
    public int getSolutionCost () {
        return solutionCost;
    }

    /**
     * Sets the solution cost
     * @param solutionCost the solution cost
     */
    public void setSolutionCost (int solutionCost) {
        this.solutionCost = solutionCost;
    }

    public Benchmark getChildBenchmark (boolean isLeft) {
        BitSet solution = (BitSet) this.solution.clone();
        if (isLeft) solution.flip(0, numBlocks);
        List<Integer> blockIndexes = solution.stream()
                                             .boxed()
                                             .toList();
        List<Block> newBlocks = new ArrayList<>();
        List<Connection> newConnections = new ArrayList<>();
        IntStream.range(0, blockIndexes.size())
                 .forEach(i -> newBlocks.add(new Block()));
            
        for (Connection connection : connections)
        {
            List<Integer> newBlockIndexes = new ArrayList<>(connection.getBlockIndexes());
            newBlockIndexes.retainAll(blockIndexes);
            newBlockIndexes.replaceAll(blockIndex -> blockIndexes.indexOf(blockIndex));
            if (!newBlockIndexes.isEmpty())
            {
                newBlockIndexes.forEach(blockIndex -> newBlocks.get(blockIndex)
                                                               .addConnectionIndex(newConnections.size()));
                newConnections.add(new Connection(newBlockIndexes));
            }
        }

        int[] newAbsoluteBlockIndexes = new int[blockIndexes.size()];
        for (int blockIndex = 0; blockIndex < newAbsoluteBlockIndexes.length; ++blockIndex)
        {
            newAbsoluteBlockIndexes[blockIndex] = absoluteBlockIndexes[blockIndexes.get(blockIndex)];
        }
        
        return new Benchmark(newBlocks.toArray(new Block[0]), 
                             newConnections.toArray(new Connection[0]),
                             newAbsoluteBlockIndexes);
    }
   
    public int[] getAbsoluteBlockIndexes () {
        return absoluteBlockIndexes;
    }
}