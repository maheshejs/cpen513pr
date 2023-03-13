package ass3;

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
            partitionBenchmarkFM(benchmark);
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
     * Partitions a benchmark using a branch and bound algorithm
     * 
     * @param benchmark
     */
    public static void partitionBenchmark(Benchmark benchmark) {
        Block[] blocks = benchmark.getBlocks();
        int numBlocks = benchmark.getNumBlocks();

        BitSet solution = new BitSet(numBlocks);
        int solutionCost = -1;
        int upperBoundPartitionCost = Integer.MAX_VALUE;
        int upperBoundPartitionSize = (numBlocks + 1) / 2;

        Deque<INode> queue = new ArrayDeque<>();
        INode rootNode = new INode();
        queue.push(rootNode);
        while (!queue.isEmpty()) {
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
            } else {
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

        // Print solution
        System.out.printf("Solution cost : %d\n", solutionCost);
        System.out.printf("Solution : Right blocks %s\n", solution.toString());
        int numRightBlocks = solution.cardinality();
        System.out.printf("L = %d, R = %d\n", numBlocks - numRightBlocks, numRightBlocks);
    }

    public static void partitionBenchmarkFM(Benchmark benchmark) {
        Block[] blocks = benchmark.getBlocks();
        Connection[] connections = benchmark.getConnections();
        int numBlocks = benchmark.getNumBlocks();
        int numConnections = benchmark.getNumConnections();
        int numPartitions = 2;
        int numPasses = 5;

        int maxGain = Arrays.stream(blocks)
                            .mapToInt(e -> e.getConnectionIndexes().size())
                            .max()
                            .orElse(-1);

        BucketArray bucketArrays[] = new BucketArray[numPartitions];
        for (int partitionIndex = 0; partitionIndex < numPartitions; ++partitionIndex)
            bucketArrays[partitionIndex] = new BucketArray(numBlocks, 2 * maxGain + 1);

        // Initialize solution
        BitSet solution = new BitSet(numBlocks);
        solution.set(0, numBlocks / numPartitions);

        List<Set<Integer>> connectionsIndexes = new ArrayList<>();
        for (int partitionIndex = 0; partitionIndex < numPartitions; ++partitionIndex)
            connectionsIndexes.add(new HashSet<>());
        for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
            int partitionIndex = solution.get(blockIndex) ? 1 : 0;
            for (int connectionIndex : blocks[blockIndex].getConnectionIndexes())
                connectionsIndexes.get(partitionIndex).add(connectionIndex);
        }
        int solutionCost = Math.toIntExact(connectionsIndexes.get(0).stream()
                                                                    .filter(connectionsIndexes.get(1)::contains)
                                                                    .count());
        
        BitSet bestSolution = (BitSet) solution.clone();
        int bestSolutionCost = solutionCost;

        while (numPasses-- > 0) {
            int fromPartitions[][] = new int[numPartitions][numConnections];
            int toPartitions[][] = new int[numPartitions][numConnections];
            for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
                int partitionIndex = solution.get(blockIndex) ? 1 : 0;
                for (int connectionIndex : blocks[blockIndex].getConnectionIndexes()) {
                    ++fromPartitions[partitionIndex][connectionIndex];
                    ++toPartitions[1 - partitionIndex][connectionIndex];
                }
            }

            int gains[] = new int[numBlocks];
            for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
                int partitionIndex = solution.get(blockIndex) ? 1 : 0;
                for (int connectionIndex : blocks[blockIndex].getConnectionIndexes()) {
                    if (fromPartitions[partitionIndex][connectionIndex] == 1)
                        ++gains[blockIndex];
                    if (toPartitions[partitionIndex][connectionIndex] == 0)
                        --gains[blockIndex];
                }
            }
            for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
                int partitionIndex = solution.get(blockIndex) ? 1 : 0;
                bucketArrays[partitionIndex].addBlock(blockIndex, gains[blockIndex] + maxGain);
            }

            BitSet lockBlocks = new BitSet(numBlocks);
            while (lockBlocks.cardinality() != numBlocks) {
                List<Pair<Integer, Integer>> pairs = new ArrayList<>(numPartitions);
                int baseBlockGain  = -1;
                int baseBlockIndex = -1;
                for (int partitionIndex = 0; partitionIndex < numPartitions; ++partitionIndex) {
                    int bucketIndex = bucketArrays[partitionIndex].getHighestFilledBucketIndex();
                    baseBlockGain   = bucketIndex - maxGain;
                    baseBlockIndex  = bucketArrays[partitionIndex].peekBucket(bucketIndex);
                    if (baseBlockIndex != -1) {
                        if (solution.get(baseBlockIndex) != (partitionIndex == 1))
                            throw new RuntimeException("Error - Partition index does not match solution");
                        solution.flip(baseBlockIndex);
                        boolean isMoveBalanced = (numPartitions * solution.cardinality() >= numBlocks - numPartitions) &&
                                                   (numPartitions * solution.cardinality() <= numBlocks + numPartitions);
                        if (isMoveBalanced)
                            pairs.add(new Pair<>(baseBlockIndex, baseBlockGain));
                        solution.flip(baseBlockIndex);
                    }
                }

                Pair<Integer, Integer> pair = pairs.stream()
                                                   .max(Comparator.comparing(Pair::getValue))
                                                   .orElse(null);

                if (pair == null)
                    break;

                baseBlockIndex = pair.getKey();
                baseBlockGain = pair.getValue();
                int partitionIndex = solution.get(baseBlockIndex) ? 1 : 0;
                bucketArrays[partitionIndex].removeBlock(baseBlockIndex);
                lockBlocks.set(baseBlockIndex);
                solution.flip(baseBlockIndex);
                solutionCost -= baseBlockGain;
                
                for (int connectionIndex : blocks[baseBlockIndex].getConnectionIndexes()) {
                    if (toPartitions[partitionIndex][connectionIndex] == 0) {
                        for (int blockIndex : connections[connectionIndex].getBlockIndexes()) {
                            if (!lockBlocks.get(blockIndex))
                                bucketArrays[partitionIndex].moveUpBlock(blockIndex);
                        }
                    } 
                    else if (toPartitions[partitionIndex][connectionIndex] == 1) {
                        for (int blockIndex : connections[connectionIndex].getBlockIndexes()) {
                            if ((solution.get(blockIndex) != (partitionIndex == 1)) && !lockBlocks.get(blockIndex))
                                bucketArrays[1 - partitionIndex].moveDownBlock(blockIndex);
                        }
                    }

                    --fromPartitions[partitionIndex][connectionIndex];
                    --toPartitions[1-partitionIndex][connectionIndex];
                    
                    ++toPartitions[partitionIndex][connectionIndex];
                    ++fromPartitions[1-partitionIndex][connectionIndex];

                    if (fromPartitions[partitionIndex][connectionIndex] == 0) {
                        for (int blockIndex : connections[connectionIndex].getBlockIndexes()) {
                            if (!lockBlocks.get(blockIndex))
                                bucketArrays[1 - partitionIndex].moveDownBlock(blockIndex);
                        }
                    } 
                    else if (fromPartitions[partitionIndex][connectionIndex] == 1) {
                        for (int blockIndex : connections[connectionIndex].getBlockIndexes()) {
                            if ((solution.get(blockIndex) == (partitionIndex == 1)) && !lockBlocks.get(blockIndex))
                                bucketArrays[partitionIndex].moveUpBlock(blockIndex);
                        }
                    }
                }

                int numRightBlocks = solution.cardinality();
                int numLeftBlocks = numBlocks - numRightBlocks;
                int blockDifference = Math.abs(numRightBlocks - numLeftBlocks);
                boolean isSolutionBalanced = blockDifference >= 0 && blockDifference <= 1;
                if (isSolutionBalanced) {
                    if (solutionCost < bestSolutionCost) {
                        bestSolution = (BitSet) solution.clone();
                        bestSolutionCost = solutionCost;
                    }
                }
            }
            solution = (BitSet) bestSolution.clone();
            solutionCost = bestSolutionCost;
        }

        benchmark.setSolution(solution);
        benchmark.setSolutionCost(solutionCost);

        // Print solution
        System.out.printf("Solution cost : %d\n", solutionCost);
        System.out.printf("Solution : Right blocks %s\n", solution.toString());
        int numRightBlocks = solution.cardinality();
        System.out.printf("L = %d, R = %d\n", numBlocks - numRightBlocks, numRightBlocks);
    }

    /**
     * Draws the benchmark solution
     * 
     * @param benchmark
     * @param group
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
}