package ass3;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Benchmark {
    private int numBlocks;
    private int numConnections;
    private Block[] blocks;

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
            
            for (int connectionIndex = 0; connectionIndex < numConnections; ++connectionIndex) {
                int numBlocksConnection = scanner.nextInt();
                while (numBlocksConnection-- > 0) {
                    int blockIndex = scanner.nextInt();
                    blocks[blockIndex].addConnectionIndex(connectionIndex);
                }
            }

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
     * Returns the number of blocks
     * @return the number of blocks
     */
    public int getNumBlocks () {
        return numBlocks;
    }
}