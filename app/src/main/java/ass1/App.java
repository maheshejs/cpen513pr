package ass1;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.PriorityQueue;

import static ass1.Constants.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application{
    private Algo algo = Algo.A_STAR;
    private Grid grid = new Grid("misty.infile"); 
    private int numWires = grid.getWires().size();
    private Congestion congestion = new Congestion(grid.getSharedCells(), numWires);
    private Comparator<INode> iNodeComparator = Comparator.comparing(INode::getCost, Comparator.naturalOrder());
    private final int NUM_ITERATIONS = 500;

    @Override
    public void start(Stage stage){
        String algos[] = {"A*", "Lee-Moore"};
        ChoiceBox algoBox = new ChoiceBox(FXCollections.observableArrayList(algos));
        algoBox.setValue("A*");

        String benchmarks[] = { "impossible", "impossible2", "kuma", "misty", "oswald", 
                                "rusty", "stanley", "stdcell", "temp", "wavy"};
        ChoiceBox benchmarkBox = new ChoiceBox(FXCollections.observableArrayList(benchmarks));
        benchmarkBox.setValue("sydney");

        Label algoLabel = new Label("Algo : ");
        Label benchmarkLabel = new Label("Benchmark : ");

        Button button = new Button("Route");
        button.setOnAction(e -> routeAllWires());

        GridPane pane = new GridPane();
        pane.setConstraints(algoLabel, 0, 1);
        pane.setConstraints(algoBox, 1, 1);
        pane.setConstraints(benchmarkLabel, 0, 0);
        pane.setConstraints(benchmarkBox, 1, 0);
        pane.getChildren().addAll(algoLabel, algoBox, benchmarkLabel, benchmarkBox);

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(25, 25, 25, 25));
        vBox.getChildren().addAll(grid, button);

        Scene scene = new Scene(vBox);

        stage.setScene(scene);
        stage.setTitle("Routing");
        stage.show();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }

    public void routeAllWires() {
        int iter = 0;
        do {
            iter++;

            // Redraw grid
            grid.redrawGrid();
            
            // Clear present congestions, but keep congestion history 
            congestion.clear();

            for (int wireID = 0; wireID < numWires; ++wireID) {
                LinkedList<Point2D> terminalCells = grid.getWires().get(wireID);
                Set<INode> route = routeWire(wireID, new LinkedList<>(terminalCells));

                // Remove terminal cells in route so that route consists of only shared resources (cells)
                route.removeAll(terminalCells);

                // Update present congestions after routing each wire
                congestion.updatePresent(route, wireID);
                
                // Draw route
                drawRoute(route, wireID);
            }

            // Update congestion history after each iteration and update hasCongestions
            congestion.updateHistory();

        } while (congestion.hasCongestions());
        System.out.printf("ITERATIONS : %d\n", iter);
    }

    public Set<INode> routeWire(int wireID, LinkedList<Point2D> terminalCells) {
        Set<INode> route  = new HashSet<>();
        Queue<INode> open = new PriorityQueue<>(iNodeComparator);
        Set<INode> closed = new HashSet<>();
                
        route.add(new INode(terminalCells.remove()));

        while (!terminalCells.isEmpty()){
            INode terminalNode = new INode(terminalCells.remove());

            for (INode iNode : route) {
                iNode.setParent(null);
                if (algo == Algo.A_STAR)
                    iNode.setCost(normalizeDistance(iNode.distance(terminalNode)));
                else
                    iNode.setCost(0);
                open.add(iNode);
            }

            closed.clear();

            while (true) {
                if (open.isEmpty()) {
                    route.add(terminalNode);
                    break; // Failure 
                }

                INode leafNode = open.remove();
                if (leafNode.equals(terminalNode)) {
                    // Success - Backtrack to construct route
                    INode iNode = leafNode;
                    while (iNode != null) {
                        if (iNode.getParent() != null)
                            route.add(iNode);
                        iNode = iNode.getParent();
                    }
                    break;
                }

                closed.add(leafNode);
                List<INode> childNodes = findNeighborNodes(leafNode, terminalNode, terminalCells);
                
                for (INode childNode : childNodes) {
                    if (!closed.contains(childNode) && !open.contains(childNode)) {
                        open.add(childNode);
                    }
                    else if (open.contains(childNode)) {
                        for (INode iNode : open) {
                            if (iNode.equals(childNode) && iNode.getCost() > childNode.getCost()) {
                                open.remove(iNode);
                                open.add(childNode);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return route;
    }

    public void drawRoute (Set<INode> route, int wireID) {
        for(INode iNode : route) {
            String gBoxID = "#" + Grid.GBox.createID(iNode.getX(), iNode.getY());
            grid.lookup(gBoxID).setStyle(grid.getCellStyles().get("route" + wireID));
        }
    }

    public double normalizeDistance(double distance) {
        return distance / Math.hypot(grid.getWidth(), grid.getHeight());
    }

    public List<INode> findNeighborNodes(INode currentNode, INode terminalNode, LinkedList<Point2D> terminalCells) { 
        List<INode> neighborNodes = new LinkedList<>();
        for (int i = 0; i < DIRECTIONS.length; ++i) {
            Point2D cell = new Point2D (currentNode.getX() + DIRECTIONS[i][0],
                                        currentNode.getY() + DIRECTIONS[i][1]);
            
            if(grid.getSharedCells().contains(cell) || terminalCells.contains(cell) || terminalNode.equals(cell) ) {
                INode neighborNode = new INode(cell);
                neighborNode.setParent(currentNode);
                double parentCost = neighborNode.getParent().getCost();
                                    /*  k * hn * pn */
                double stepCost = CONGESTION_WEIGHT * congestion.getHistory(neighborNode) * 
                                    (1 + congestion.getPresent(neighborNode)); 
                double cost = parentCost + stepCost;
                if (algo == Algo.A_STAR)
                {
                    double heurCost = normalizeDistance(neighborNode.distance(terminalNode));
                    cost += heurCost;
                }
                neighborNode.setCost(cost);
                neighborNodes.add(neighborNode);
            }
        }
        return neighborNodes;
    }
}