package ass3;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.HashSet;
import java.util.BitSet;
import java.util.Random;
import java.util.Comparator;

import javafx.util.Pair;

import static ass3.Constants.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        /////////////////////////////////////////////////////////////////////
        /////////////////////// GRAPHICS - SETUP START //////////////////////
        /////////////////////////////////////////////////////////////////////
        Group group = new Group();

        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.GRAY);
        gc.fillRect(CANVAS_WIDTH / 2 - 2.5, 0, 5, CANVAS_HEIGHT);
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(5);
        gc.strokeRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        group.getChildren().add(canvas);

        Label label = new Label("Benchmark: ");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ComboBox<String> cBox = new ComboBox<>(FXCollections.observableArrayList(BENCHMARK_FILES));
        cBox.setValue("cm82a");
        cBox.setVisibleRowCount(3);

        HBox hBox = new HBox();
        hBox.getChildren().addAll(label, cBox);

        Button button = new Button("Partition");
        button.setOnAction(e -> {
            group.getChildren().clear();
            group.getChildren().add(canvas);
            String benchmarkFile = cBox.getValue() + ".txt";
            Benchmark benchmark = new Benchmark(benchmarkFile);
            partitionBenchmark(benchmark, true);
            printBenchmarkSolution(benchmark);
            drawBenchmarkSolution(benchmark, group);
        });

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(25, 25, 25, 25));
        vBox.getChildren().addAll(hBox, group, button);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("Partitioning");
        stage.show();
        /////////////////////////////////////////////////////////////////////
        /////////////////////// GRAPHICS - SETUP END ////////////////////////
        /////////////////////////////////////////////////////////////////////
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Partitions a benchmark using a Branch and Bound algorithm
     * @param benchmark the benchmark to partition
     * @param useFM whether to use FM algorithm to initialize the solution
     */
    public static void partitionBenchmark(Benchmark benchmark, boolean useFM) {
        partitionBenchmarkTriv(benchmark);
        // Trivial solution
        BitSet solution = benchmark.getSolution();
        int solutionCost = benchmark.getSolutionCost();
        if (useFM) {
            partitionBenchmarkFM(benchmark, solution, solutionCost, NUM_FM_PASSES);
            // FM solution
            solution = benchmark.getSolution();
            solutionCost = benchmark.getSolutionCost();
        }
        partitionBenchmarkBB(benchmark, solution, solutionCost);
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

        benchmark.setSolution(solution);
        benchmark.setSolutionCost(solutionCost);
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
        benchmark.setSolution(solution);
        benchmark.setSolutionCost(solutionCost);

        System.out.println("Number of visited nodes: " + numVisitedNodes);
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
        benchmark.setSolution(solution);
        benchmark.setSolutionCost(solutionCost);
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

    /**
     * Prints the benchmark solution
     * @param benchmark the benchmark to print the solution for
     */
    public static void printBenchmarkSolution(Benchmark benchmark) {
        BitSet solution = benchmark.getSolution();
        int solutionCost = benchmark.getSolutionCost();
        int numBlocks = benchmark.getNumBlocks();
        int numRightBlocks = solution.cardinality();
        int numLeftBlocks = numBlocks - numRightBlocks;
        System.out.printf("Solution cost : %d\n", solutionCost);
        System.out.printf("Solution : Right blocks %s\n", solution.toString());
        System.out.printf("L = %d, R = %d\n", numLeftBlocks, numRightBlocks);
    }

    /**
     * Draws the benchmark solution
     * @param benchmark the benchmark
     * @param group the container to draw the solution in
     */
    public static void drawBenchmarkSolution(Benchmark benchmark, Group group) {
        Connection[] connections = benchmark.getConnections();
        int numBlocks = benchmark.getNumBlocks();
        int numConnections = benchmark.getNumConnections();
        BitSet solution = benchmark.getSolution();

        Random random = new Random();
        Map<Integer, Point2D> placements = new HashMap<>();
        for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
            Point2D loc = solution.get(blockIndex) ? new Point2D(random.nextDouble(CANVAS_WIDTH / 2 + CANVAS_PADDING, CANVAS_WIDTH - 2 * CANVAS_PADDING), 
                                                                 random.nextDouble(CANVAS_PADDING, CANVAS_HEIGHT - CANVAS_PADDING)) 
                                                   : new Point2D(random.nextDouble(2 * CANVAS_PADDING, CANVAS_WIDTH / 2 - CANVAS_PADDING),
                                                                 random.nextDouble(CANVAS_PADDING, CANVAS_HEIGHT - CANVAS_PADDING));
            placements.put(blockIndex, loc);
        }

        for (int connectionIndex = 0; connectionIndex < numConnections; ++connectionIndex) {
            String color = COLORS[connectionIndex % COLORS.length];
            Iterator<Integer> iterator = connections[connectionIndex].getBlockIndexes().iterator();
            Point2D source = placements.get(iterator.next());
            Circle sourcePin = new Circle(source.getX(), source.getY(), PIN_RADIUS);
            sourcePin.setStyle("-fx-fill: " + color + ";");
            while (iterator.hasNext()) {
                Point2D sink = placements.get(iterator.next());
                Circle sinkPin = new Circle(sink.getX(), sink.getY(), PIN_RADIUS);
                sinkPin.setStyle("-fx-fill: " + color + ";");
                Line line = new Line(source.getX(), source.getY(), sink.getX(), sink.getY());
                line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2.5;");
                group.getChildren().addAll(line, sinkPin);
            }
            group.getChildren().add(sourcePin);
        }

        for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
            Text text = new Text(placements.get(blockIndex).getX() + 5, 
                                 placements.get(blockIndex).getY() + 5, 
                                 String.valueOf(blockIndex));
            text.setStyle("-fx-font-size: 12px;");
            group.getChildren().add(text);
        }
    }

    /**
     * Converts a boolean to an integer
     * @param b the boolean to convert
     * @return 1 if b is true, 0 otherwise
     */
    public static int boolToInt(boolean b) { 
        return b ? 1 : 0;
    }
}