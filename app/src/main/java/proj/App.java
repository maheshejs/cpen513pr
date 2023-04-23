package proj;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.HashSet;
import java.util.BitSet;
import java.util.Random;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static proj.Constants.*;
import static proj.Utils.*;

import javafx.util.Pair;
import javafx.geometry.Point2D;

public class App {
    private static final Random random = new Random(); 
    private static List<List<Integer>> partitions = new ArrayList<>();

    public static void main(String[] args) {
        String benchmarkFile = "cm162a.txt";
        Benchmark benchmark = new Benchmark(benchmarkFile, false);
        partitionBenchmark(benchmark, false, 0);

        for (List<Integer> partition : partitions)
            System.out.println(partition);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////    PARTITIONING   //////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     */
    public static void partitionBenchmark(Benchmark benchmark, boolean useFM, int recursionDepth) {
        // Trivial solution
        partitionBenchmarkTriv(benchmark);
        BitSet solutionTriv = benchmark.getPartitionSolution();
        int solutionCostTriv = benchmark.getPartitionSolutionCost();

        if (useFM)
            partitionBenchmarkFM(benchmark, solutionTriv, solutionCostTriv, NUM_FM_PASSES);
        else
            partitionBenchmarkBB(benchmark, solutionTriv, solutionCostTriv);

        BitSet solution = benchmark.getPartitionSolution();
        for (int i = 0; i < NUM_PARTITIONS; ++i) {
            solution.flip(0, benchmark.getNumBlocks());
            List<Integer> blockIndexes = solution.stream().boxed().toList();
            if (recursionDepth != MAX_RECURSION_DEPTH) {
                partitionBenchmark(benchmark.getSubBenchmark(blockIndexes, 0, 0), useFM, recursionDepth + 1);
            }
            else {
                partitions.add(blockIndexes.stream()
                                           .map(blockIndex -> benchmark.getAbsoluteBlockIndexes()[blockIndex])
                                           .toList());
            }
        }
    }

    /**
     * Partitions a benchmark using trivial algorithm
     * @param benchmark the benchmark to partition
     */
    public static void partitionBenchmarkTriv(Benchmark benchmark) {
        Block[] blocks = benchmark.getBlocks();
        Connection[] connections = benchmark.getConnections();
        int numBlocks = benchmark.getNumBlocks();

        BitSet solution = new BitSet(numBlocks);
        solution.set(0, numBlocks / NUM_PARTITIONS);
        int solutionCost = computeSolutionCost(blocks, connections, solution);

        benchmark.setPartitionSolution(solution);
        benchmark.setPartitionSolutionCost(solutionCost);
    }

    /**
     * Partitions a benchmark using a branch and bound algorithm
     * @param benchmark the benchmark to partition
     * @param solution the initial balanced solution of the benchmark
     * @param solutionCost the cost of the initial balanced solution
     */
    public static void partitionBenchmarkBB(Benchmark benchmark, BitSet solution, int solutionCost) {
        Block[] blocks = benchmark.getBlocks();
        int numBlocks = benchmark.getNumBlocks();

        int upperBoundPartitionCost = solutionCost;
        int upperBoundPartitionSize = (numBlocks + 1) / NUM_PARTITIONS;

        BigInteger numVisitedNodes = BigInteger.valueOf(0);;
        Deque<INode> queue = new ArrayDeque<>();
        INode rootNode = new INode();
        queue.push(rootNode);
        while (!queue.isEmpty()) {
            numVisitedNodes = numVisitedNodes.add(BigInteger.valueOf(1));
            INode node = queue.pop();

            // Check if node is a leaf
            if (node.getBlockIndex() == numBlocks - 1) {
                if (node.getCost() < upperBoundPartitionCost) {
                    upperBoundPartitionCost = node.getCost();

                    // Backtrack to construct solution
                    int blockIndex = numBlocks - 1;
                    INode backNode = node;
                    solution.clear();
                    while (backNode.getParent() != null) {
                        if (backNode.getParent().getNumRightChildren() + 1 == backNode.getNumRightChildren())
                            solution.set(blockIndex);
                        backNode = backNode.getParent();
                        --blockIndex;
                    }
                    solutionCost = node.getCost();
                }
            }
            else {
                Set<Integer> connectionIndexes = blocks[node.getBlockIndex() + 1].getConnectionIndexes();
                INode leftChildNode = new INode(node, true, connectionIndexes);
                INode rightChildNode = new INode(node, false, connectionIndexes);
                List<INode> childNodes = List.of(leftChildNode, rightChildNode);
                for (INode childNode : childNodes) {
                    boolean isSolutionBalanced = (childNode.getNumLeftChildren() <= upperBoundPartitionSize) &&
                            (childNode.getNumRightChildren() <= upperBoundPartitionSize);
                    boolean isSolutionPromising = childNode.getCost() < upperBoundPartitionCost;
                    if (isSolutionBalanced && isSolutionPromising) {
                        queue.push(childNode);
                    }
                }
            }
        }
        benchmark.setPartitionSolution(solution);
        benchmark.setPartitionSolutionCost(solutionCost);
    }

    /**
     * Partitions a benchmark using FM algorithm
     * @param benchmark the benchmark to partition
     * @param solution the initial balanced solution of the benchmark
     * @param solutionCost the cost of the initial balanced solution
     */
    public static void partitionBenchmarkFM(Benchmark benchmark, BitSet solution, int solutionCost, int numPasses) {
        Block[] blocks = benchmark.getBlocks();
        Connection[] connections = benchmark.getConnections();
        int numBlocks = benchmark.getNumBlocks();
        int numConnections = benchmark.getNumConnections();
        int maxGain = Arrays.stream(blocks)
                            .mapToInt(e -> e.getConnectionIndexes().size())
                            .max()
                            .orElse(-1);

        BitSet bestSolution = (BitSet) solution.clone();
        int bestSolutionCost = solutionCost;

        for (int pass = 0; pass < numPasses; ++pass) {
            // Initialize gains
            BucketArray bucketArrays[] = new BucketArray[NUM_PARTITIONS];
            for (int partitionIndex = 0; partitionIndex < NUM_PARTITIONS; ++partitionIndex)
                bucketArrays[partitionIndex] = new BucketArray(numBlocks, 2 * maxGain + 1);
            int fromPartitions[][] = new int[NUM_PARTITIONS][numConnections];
            int toPartitions[][] = new int[NUM_PARTITIONS][numConnections];
            initializeBlockGainsFM(bucketArrays, fromPartitions, toPartitions, blocks, solution);

            BitSet blockLocks = new BitSet(numBlocks);
            while (blockLocks.cardinality() != numBlocks) {
                // Choose block move
                Pair<Integer, Integer> pair = chooseBlockMoveFM(bucketArrays, blocks, solution);
                if (pair == null) 
                    break; //

                // Apply block move
                int baseBlockIndex = pair.getKey();
                int baseBlockGain  = pair.getValue() - maxGain;
                int partitionIndex = boolToInt(solution.get(baseBlockIndex));
                bucketArrays[partitionIndex].removeBlock(baseBlockIndex);
                blockLocks.set(baseBlockIndex);
                solution.flip(baseBlockIndex);
                solutionCost -= baseBlockGain;
                
                // Update gains
                updateBlockGainsFM(bucketArrays, fromPartitions, toPartitions, 
                                   blocks, connections, blockLocks, baseBlockIndex, solution);

                // Checkpoint solution if it is a balanced improvement
                int numRightBlocks = solution.cardinality();
                int numLeftBlocks = numBlocks - numRightBlocks;
                int blockDifference = Math.abs(numRightBlocks - numLeftBlocks);
                boolean isSolutionBalanced = blockDifference >= 0 && blockDifference <= 1;
                if (isSolutionBalanced && solutionCost < bestSolutionCost) {
                    bestSolution = (BitSet) solution.clone();
                    bestSolutionCost = solutionCost;
                }
            }
            // Rollback to best seen solution
            solution = (BitSet) bestSolution.clone();
            solutionCost = bestSolutionCost;
        }
        benchmark.setPartitionSolution(solution);
        benchmark.setPartitionSolutionCost(solutionCost);
    }

    /**
     * Compute the cost of a solution
     * @param blocks blocks
     * @param connections connections
     * @param solution solution
     * @return cost of the solution
     */
    public static int computeSolutionCost (Block blocks[], Connection connections[],
                                           BitSet solution) {
        int numBlocks = blocks.length;
        List<Set<Integer>> connectionsIndexes = new ArrayList<>();
        for (int partitionIndex = 0; partitionIndex < NUM_PARTITIONS; ++partitionIndex)
            connectionsIndexes.add(new HashSet<>());
        for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
            int partitionIndex = boolToInt(solution.get(blockIndex));
            for (int connectionIndex : blocks[blockIndex].getConnectionIndexes())
                connectionsIndexes.get(partitionIndex).add(connectionIndex);
        }
        return Math.toIntExact(connectionsIndexes.get(0).stream()
                                                        .filter(connectionsIndexes.get(1)::contains)
                                                        .count());
    }

    /**
     * Choose a block move for the FM algorithm
     * @param bucketArrays bucket arrays
     * @param blocks blocks
     * @param solution solution
     * @return block move encoded as a pair of block index and bucket index
     */
    public static Pair<Integer, Integer> chooseBlockMoveFM (BucketArray bucketArrays[],
                                                            Block blocks[],
                                                            BitSet solution) {
        int numBlocks = blocks.length;
        List<Pair<Integer, Integer>> pairs = new ArrayList<>(NUM_PARTITIONS);
        for (int partitionIndex = 0; partitionIndex < NUM_PARTITIONS; ++partitionIndex) {
            int bucketIndex = bucketArrays[partitionIndex].getHighestFilledBucketIndex();
            if (bucketIndex != -1) {
                int blockIndex  = bucketArrays[partitionIndex].peekBucket(bucketIndex);
                if (boolToInt(solution.get(blockIndex)) != partitionIndex)
                    throw new RuntimeException("Error - Partition does not match solution");
                solution.flip(blockIndex);
                boolean isMoveBalanced = (NUM_PARTITIONS * solution.cardinality() >= numBlocks - NUM_PARTITIONS) &&
                                            (NUM_PARTITIONS * solution.cardinality() <= numBlocks + NUM_PARTITIONS);
                if (isMoveBalanced)
                    pairs.add(new Pair<>(blockIndex, bucketIndex));
                solution.flip(blockIndex);
            }
        }

        Pair<Integer, Integer> pair = pairs.stream()
                                           .max(Comparator.comparing(Pair::getValue))
                                           .orElse(null);
        return pair;
    }

    /**
     * Initialize the block gains for the FM algorithm
     * @param bucketArrays bucket arrays
     * @param fromPartitions
     * @param toPartitions
     * @param blocks blocks
     * @param solution solution
     */
    public static void initializeBlockGainsFM (BucketArray bucketArrays[],
                                               int fromPartitions[][], int toPartitions[][],
                                               Block blocks[],
                                               BitSet solution) {
        int maxGain   = bucketArrays[0].getMaxBucketIndex() / 2;
        int numBlocks = blocks.length;
        for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
            int partitionIndex = boolToInt(solution.get(blockIndex));
            for (int connectionIndex : blocks[blockIndex].getConnectionIndexes()) {
                ++fromPartitions[partitionIndex][connectionIndex];
                ++toPartitions[1-partitionIndex][connectionIndex];
            }
        }

        int gains[] = new int[numBlocks];
        for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
            int partitionIndex = boolToInt(solution.get(blockIndex));
            for (int connectionIndex : blocks[blockIndex].getConnectionIndexes()) {
                if (fromPartitions[partitionIndex][connectionIndex] == 1)
                    ++gains[blockIndex];
                if (toPartitions[partitionIndex][connectionIndex] == 0)
                    --gains[blockIndex];
            }
        }
        for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
            int partitionIndex = boolToInt(solution.get(blockIndex));
            bucketArrays[partitionIndex].addBlock(blockIndex, gains[blockIndex] + maxGain);
        }
    }
    
    /**
     * Update the block gains for the FM algorithm
     * @param bucketArrays bucket arrays
     * @param fromPartitions
     * @param toPartitions
     * @param blocks blocks
     * @param connections connections
     * @param blockLocks block locks
     * @param baseBlockIndex base block index
     * @param solution solution
     */
    public static void updateBlockGainsFM (BucketArray bucketArrays[],
                                           int fromPartitions[][], int toPartitions[][],
                                           Block blocks[], Connection connections[],
                                           BitSet blockLocks, int baseBlockIndex,
                                           BitSet solution) {
        int partitionIndex = boolToInt(solution.get(baseBlockIndex));
        for (int connectionIndex : blocks[baseBlockIndex].getConnectionIndexes()) {
            if (toPartitions[1-partitionIndex][connectionIndex] == 0) {
                for (int blockIndex : connections[connectionIndex].getBlockIndexes()) {
                    if (!blockLocks.get(blockIndex))
                        bucketArrays[1-partitionIndex].moveUpBlock(blockIndex);
                }
            } 
            else if (toPartitions[1-partitionIndex][connectionIndex] == 1) {
                for (int blockIndex : connections[connectionIndex].getBlockIndexes())
                    if (boolToInt(solution.get(blockIndex)) == partitionIndex && !blockLocks.get(blockIndex))
                        bucketArrays[partitionIndex].moveDownBlock(blockIndex);
            }

            --fromPartitions[1-partitionIndex][connectionIndex];
            --toPartitions[partitionIndex][connectionIndex];
            
            ++toPartitions[1-partitionIndex][connectionIndex];
            ++fromPartitions[partitionIndex][connectionIndex];

            if (fromPartitions[1-partitionIndex][connectionIndex] == 0) {
                for (int blockIndex : connections[connectionIndex].getBlockIndexes())
                    if (!blockLocks.get(blockIndex))
                        bucketArrays[partitionIndex].moveDownBlock(blockIndex);
            } 
            else if (fromPartitions[1-partitionIndex][connectionIndex] == 1) {
                for (int blockIndex : connections[connectionIndex].getBlockIndexes())
                    if (boolToInt(solution.get(blockIndex)) != partitionIndex && !blockLocks.get(blockIndex))
                        bucketArrays[1-partitionIndex].moveUpBlock(blockIndex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////     PLACEMENT     //////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public static void placeBenchmark(Benchmark benchmark) {
        Block[]      blocks      = benchmark.getBlocks();
        Connection[] connections = benchmark.getConnections();
        Point2D[]    locs        = benchmark.getLocs();
        
        // Set simulated annealing parameters
        final int    ANNEALING_MOVES_PER_TEMPERATURE = (int) (10 * Math.pow(blocks.length, 4.0/3.0));
        final double ANNEALING_COOLING_RATE          = 0.95;
        
        // Initialize solution
        int[] solution = new int[locs.length];
        double cost = initializeBlockPlacements(solution, blocks, connections, locs, random);
        int[] bestSolution = solution;
        double bestCost = cost;
        
        // Initialize tabu list, annealing temperature and failure counter
        double[] costs = new double[blocks.length];
        for (int n = 0; n < blocks.length; ++n){
            Neighbor neighbor = new Neighbor(solution, blocks, connections, locs, random);
            costs[n] = cost + neighbor.getMoveCost();
        }
        double temperature = 20 * calculateStandardDeviation(costs);

        int iteration = 0;
        boolean isDone = false;
        while (!isDone) {
            for (int move = 0; move < ANNEALING_MOVES_PER_TEMPERATURE; ++move, ++iteration) {
                //System.out.printf("ANNEALING | Cost = %.1f | Iteration = %d | Temperature = %.6f\n", cost, iteration, temperature);
                // Generate random neighbor
                Neighbor neighbor = new Neighbor(solution, blocks, connections, locs, random);
                
                if (random.nextDouble() < Math.exp(- neighbor.getMoveCost() / temperature)) {
                    swapBlockPlacements(solution, blocks, connections, locs, neighbor.getFirstLocIndex(), neighbor.getSecondLocIndex());
                    cost += neighbor.getMoveCost();
                    // Checkpoint solution if improved
                    if (cost < bestCost) {
                        bestCost = cost;
                        bestSolution = solution;
                        //System.out.println("ANNEALING : " + bestCost + " | " + temperature + " | " + iteration);
                    }
                }
            }
            // Update temperature
            temperature *= ANNEALING_COOLING_RATE;

            // Check if done
            if (temperature < (0.001 * cost / connections.length))
                isDone = true;
        }
    }
    
    /**
     * Initialize block placements
     * @param solution the solution array
     * @param blocks blocks
     * @param connections connections
     * @param locs locations
     * @param random the random number generator
     * @return the cost of the solution
     */
    public static double initializeBlockPlacements (int[] solution, Block[] blocks, Connection[] connections, Point2D[] locs, Random random) {
        List<Integer> blockIndexes = IntStream.range(0, locs.length).boxed().collect(Collectors.toList());
        // Shuffle block indexes or not if random is null for testing (deterministic behavior)
        if (random != null)
            Collections.shuffle(blockIndexes, random);
        IntStream.range(0, locs.length).forEach(e -> solution[e] = blockIndexes.get(e));

        Point2D defaultLoc = new Point2D(0, 0);
        for (int locIndex = 0; locIndex < locs.length; ++locIndex) {
            Point2D loc = locs[locIndex];
            int blockIndex = solution[locIndex];
            if (blockIndex < blocks.length)
                for (int connectionIndex : blocks[blockIndex].getConnectionIndexes())
                    connections[connectionIndex].moveBlockPlacement(blockIndex, defaultLoc, loc);
        }

        return Arrays.stream(connections).mapToDouble(e -> e.getCost()).sum();
    }

    /**
     * Swap block placements at given locations.
     * @param solution the solution array.
     * @param firstLocIndex the index of the first location.
     * @param secondLocIndex the index of the second location.
     */
    public static void swapBlockPlacements (int[] solution, Block[] blocks, Connection[] connections, Point2D[] locs, int firstLocIndex, int secondLocIndex) {
        int firstBlockIndex  = solution[firstLocIndex];
        int secondBlockIndex = solution[secondLocIndex];
        Point2D firstLoc  = locs[firstLocIndex];
        Point2D secondLoc = locs[secondLocIndex];
        
        if (firstBlockIndex < blocks.length) {
            for (int connectionIndex : blocks[firstBlockIndex].getConnectionIndexes()) {
                connections[connectionIndex].moveBlockPlacement(firstBlockIndex, firstLoc, secondLoc);
            }
        }
        if (secondBlockIndex < blocks.length) {
            for (int connectionIndex : blocks[secondBlockIndex].getConnectionIndexes()) {
                connections[connectionIndex].moveBlockPlacement(secondBlockIndex, secondLoc, firstLoc);
            }
        }
        swap(solution, firstLocIndex, secondLocIndex);
    }
}