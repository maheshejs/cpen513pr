package ass3;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.Random;
import java.util.stream.Collectors;

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
        gc.fillRect(CANVAS_WIDTH/2 - 2.5, 0, 5, CANVAS_HEIGHT);
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
                                  partitionBenchmark(benchmark);
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
     * @param benchmark
     */
    public static void partitionBenchmark(Benchmark benchmark) {
        // TODO: Implement KL algorithm
        Block[] blocks = benchmark.getBlocks();
        int numBlocks = benchmark.getNumBlocks();
        
        int[] solution = new int[numBlocks];
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
                    while (backNode.getParent() != null) {
                        if (backNode.getParent().getNumLeftChildren() + 1 == backNode.getNumLeftChildren())
                            solution[blockIndex] = 0;
                        else
                            solution[blockIndex] = 1;
                        backNode = backNode.getParent();
                        --blockIndex;
                    }
                    solutionCost = node.getCost();
                }
            }
            else {
                Set<Integer> connectionIndexes = blocks[node.getBlockIndex() + 1].getConnectionIndexes();
                INode leftChildNode  = new INode(node, true,  connectionIndexes);
                INode rightChildNode = new INode(node, false, connectionIndexes);
                List<INode> childNodes = List.of(leftChildNode, rightChildNode);
                for (INode childNode : childNodes) {
                    boolean isBalanced  = childNode.getNumLeftChildren() <= upperBoundPartitionSize &&
                                            childNode.getNumRightChildren() <= upperBoundPartitionSize;
                    boolean isPromising = childNode.getCost() < upperBoundPartitionCost;
                    if (isBalanced && isPromising) {
                        queue.push(childNode);
                    }
                }
            }
        }
        benchmark.setSolution(solution);
        benchmark.setSolutionCost(solutionCost);
        
        // Print solution
        System.out.printf("Solution cost : %d\n", solutionCost);
        System.out.printf("Solution : %s\n",  Arrays.stream(solution)
                                                    .mapToObj(e -> e == 0 ? "L" : "R")
                                                    .collect(Collectors.joining()));
        int numLeftBlocks = (int) Arrays.stream(solution).filter(e -> e == 0).count();
        System.out.printf("L = %d, R = %d\n", numLeftBlocks, numBlocks - numLeftBlocks);
    }

    /**
     * Draws the benchmark solution
     * @param benchmark
     * @param group
     */
    public static void drawBenchmarkSolution (Benchmark benchmark, Group group) {
        // TODO : Assign numbers to pins
        Connection[] connections = benchmark.getConnections();
        int numBlocks = benchmark.getNumBlocks();
        int numConnections = benchmark.getNumConnections();
        int[] solution = benchmark.getSolution();

        Random random = new Random();
        Map<Integer, Point2D> placements = new HashMap<>();
        for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
            Point2D loc = solution[blockIndex] == 0 ? new Point2D(random.nextDouble(2 * CANVAS_PADDING, CANVAS_WIDTH/2 - CANVAS_PADDING),
                                                                    random.nextDouble(CANVAS_PADDING, CANVAS_HEIGHT - CANVAS_PADDING)) : 
                                                      new Point2D(random.nextDouble(CANVAS_WIDTH/2 + CANVAS_PADDING, CANVAS_WIDTH - 2 * CANVAS_PADDING),
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
    }
}