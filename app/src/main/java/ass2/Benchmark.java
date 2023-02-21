package ass2;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import javafx.geometry.Point2D;

public class Benchmark {
    private boolean useRowSpacing;
    private int numBlocks;
    private int numConnections;
    private int numRows;
    private int numCols;
    private int numLocs;
    private Block[] blocks;
    private Connection[] connections;
    private Point2D[] locs;

    /**
     * Creates a new benchmark
     * @param benchmarkFile the benchmark file
     * @param useRowSpacing whether to use row spacing
     */
    public Benchmark (String benchmarkFile, boolean useRowSpacing) {
        this.useRowSpacing = useRowSpacing;
        parseBenchmarkFile(benchmarkFile);
    }

    /**
     * Parses the benchmark file
     * @param benchmarkFile the benchmark file
     */
    private void parseBenchmarkFile (String benchmarkFile) {
        try (Scanner scanner = new Scanner(new File("data/benchmarks/ass2/" + benchmarkFile))) {
            numBlocks = scanner.nextInt();
            numConnections = scanner.nextInt();
            numRows = scanner.nextInt();
            numCols = scanner.nextInt();
            numLocs = numRows * numCols;

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
                    // Add block placement at location (0, 0) for now
                    connections[connectionIndex].addBlockPlacement(blockIndex, new Point2D(0, 0));
                }
            }

            locs = new Point2D[numLocs];
            for (int locIndex = 0; locIndex < numLocs; ++locIndex)
                locs[locIndex] = getLoc(locIndex);
        } catch (FileNotFoundException e) {
            System.err.println("Unable to load benchmark file: " + benchmarkFile);
            System.exit(1);
        }
    }
    
    /**
     * Returns the location at the given index
     * @param locIndex the location index
     * @return the location at the given index
     */
    public Point2D getLoc (int locIndex) {
        return new Point2D(locIndex % numCols, (locIndex / numCols) * (useRowSpacing ? 2 : 1));
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
     * Returns locations
     * @return locations
     */
    public Point2D[] getLocs () {
        return locs;
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
     * Returns the number of rows
     * @return the number of rows
     */
    public int getNumRows () {
        return numRows;
    }

    /**
     * Returns the number of columns
     * @return the number of columns
     */
    public int getNumCols () {
        return numCols;
    }
    
    /**
     * Returns the number of locations
     * @return the number of locations
     */
    public int getNumLocs() {
        return numLocs;
    }
}