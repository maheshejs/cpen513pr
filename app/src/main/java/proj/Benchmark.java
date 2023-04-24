package proj;

import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import static proj.Utils.*;

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
    private int[] absoluteBlockIndexes;
    private BitSet partitionSolution;
    private int partitionSolutionCost;

    public Benchmark () {
        this.useRowSpacing = false;
        this.numBlocks = 0;
        this.numConnections = 0;
        this.numRows = 0;
        this.numCols = 0;
        this.numLocs = 0;
        this.blocks = new Block[0];
        this.connections = new Connection[0];
        this.locs = new Point2D[0];
        this.absoluteBlockIndexes = new int[0];
        this.partitionSolution = new BitSet(0);
        this.partitionSolutionCost = -1;
    }

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
        try (Scanner scanner = new Scanner(new File("data/benchmarks/ass2/" + benchmarkFile + ".txt"))) {
            numBlocks = scanner.nextInt();
            numConnections = scanner.nextInt();
            numRows = scanner.nextInt();
            numCols = scanner.nextInt();
            numLocs = numRows * numCols;

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
                    // Add block placement at location (0, 0) for now
                    connections[connectionIndex].addBlockPlacement(blockIndex, new Point2D(0, 0));
                }
            }

            locs = new Point2D[numLocs];
            for (int locIndex = 0; locIndex < numLocs; ++locIndex)
                locs[locIndex] = getLoc(locIndex, numRows, numCols, useRowSpacing);
        } catch (FileNotFoundException e) {
            System.err.println("Unable to load benchmark file: " + benchmarkFile);
            System.exit(1);
        }
    }
    
    /**
     * Returns whether to use row spacing
     * @return whether to use row spacing
     */
    public boolean getUseRowSpacing () {
        return useRowSpacing;
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
    
    public int[] getAbsoluteBlockIndexes () {
        return absoluteBlockIndexes;
    }
    
    /**
     * Returns partitioning solution
     * @return partitioning solution
     */
    public BitSet getPartitionSolution () {
        return partitionSolution;
    }

    /**
     * Sets the partitioning solution
     * @param partitionSolution partitioning solution
     */
    public void setPartitionSolution (BitSet partitionSolution) {
        this.partitionSolution = partitionSolution;
    }

    /**
     * Returns partitioning solution cost
     * @return partitioning solution cost
     */
    public int getPartitionSolutionCost () {
        return partitionSolutionCost;
    }

    /**
     * Sets partitioning solution cost
     * @param partitionSolutionCost the partitionSolution cost
     */
    public void setPartitionSolutionCost (int partitionSolutionCost) {
        this.partitionSolutionCost = partitionSolutionCost;
    }

    public Benchmark getSubBenchmark (List<Integer> blockIndexes, int numRows, int numCols) {
        List<Block> blocks = new ArrayList<>();
        List<Connection> connections = new ArrayList<>();
        List<Point2D> locs = new ArrayList<>();
        List<Integer> absoluteBlockIndexes = new ArrayList<>();
        for (int blockIndex : blockIndexes)
        {
            blocks.add(new Block());
            absoluteBlockIndexes.add(this.absoluteBlockIndexes[blockIndex]);
        }
            
        for (Connection connection : this.connections)
        {
            List<Integer> blockConnectionIndexes = new ArrayList<>(connection.getBlockIndexes());
            blockConnectionIndexes.retainAll(blockIndexes);
            blockConnectionIndexes.replaceAll(blockIndex -> blockIndexes.indexOf(blockIndex));
            if (!blockConnectionIndexes.isEmpty())
            {
                int connectionIndex = connections.size();
                connections.add(new Connection());
                for (int blockIndex : blockConnectionIndexes) {
                    blocks.get(blockIndex).addConnectionIndex(connectionIndex);
                    connections.get(connectionIndex).addBlockPlacement(blockIndex, new Point2D(0, 0));
                }
                ++connectionIndex;
            }
        }

        for (int locIndex = 0; locIndex < numRows * numCols; ++locIndex)
            locs.add(getLoc(locIndex, numRows, numCols, useRowSpacing));

        Benchmark subBenchmark = new Benchmark();
        subBenchmark.useRowSpacing = this.useRowSpacing;
        subBenchmark.blocks = blocks.toArray(new Block[0]);
        subBenchmark.connections = connections.toArray(new Connection[0]);
        subBenchmark.locs = locs.toArray(new Point2D[0]);
        subBenchmark.absoluteBlockIndexes = absoluteBlockIndexes.stream().mapToInt(e -> e).toArray();
        subBenchmark.numBlocks = blocks.size();
        subBenchmark.numConnections = connections.size();
        subBenchmark.numLocs = locs.size();
        subBenchmark.numRows = numRows;
        subBenchmark.numCols = numCols;
        subBenchmark.partitionSolution = new BitSet(subBenchmark.numBlocks);
        subBenchmark.partitionSolutionCost = -1;
        
        return subBenchmark;
    }
}