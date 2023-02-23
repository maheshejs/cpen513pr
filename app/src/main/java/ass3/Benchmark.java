package ass3;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Benchmark {
    private int numBlocks;
    private int numConnections;
    private Block[] blocks;
    private Connection[] connections;
    private int[] solution;
    private int solutionCost;

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
            for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex)
                blocks[blockIndex] = new Block();
            
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
            solution = new int[numBlocks];
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
    public int[] getSolution () {
        return solution;
    }

    /**
     * Sets the solution
     * @param solution the solution
     */
    public void setSolution (int[] solution) {
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
}