package ass1;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import ass1.Constants.Algo;
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
    private Grid grid = new Grid("oswald.infile"); 
    private Algo algo = Algo.A_STAR;
    private HashMap<Point2D, Integer> shared = new HashMap<>();
    private final int NUM_ITERATIONS = 500;
    private final int numWires = grid.getWires().size();
    private Comparator<INode> iNodeComparator = Comparator.comparing(INode::getCost, Comparator.naturalOrder());

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
        for (Point2D cell : grid.getSharedCells()) 
            shared.put(cell, (1 << numWires));

        int iter = 0;
        boolean isGreedy = false;
        //for (int i = 0; i < NUM_ITERATIONS; ++i) {
        boolean retry;
        do {
            iter++;
            retry = false;

            shared.replaceAll((key, value) -> ((value >> numWires) << numWires));

            for (int wire = 0; wire < numWires; ++wire) {
                
                //LinkedList<Point2D> terminalCells = new LinkedList<>();
                //for (Point2D cell : grid.getWires().get(wire))
                //    terminalCells.add(cell);
                LinkedList<Point2D> terminalCells = grid.getWires().get(wire);
                
                // Copy terminal cells to redraw them later
                //LinkedList<Point2D> copyTerminalCells = new LinkedList<>();
                //for (Point2D cell : terminalCells)
                //    copyTerminalCells.add(cell);

                Set<INode> route = routeWire(new LinkedList<>(terminalCells), wire);

                // Draw route
                for(INode iNode : route){
                    if (shared.containsKey(iNode))
                        shared.put(iNode, shared.get(iNode) | (1 << wire));
                    /*
                    if (i == NUM_ITERATIONS - 1) {
                    Rectangle rect = new Rectangle(iNode.getX() * CELL_WIDTH, iNode.getY() * CELL_HEIGHT,
                                                        CELL_WIDTH, CELL_HEIGHT);
                    rect.setStroke(Color.BLUE);
                    rect.setFill(COLORS[wire]);
                    grid.getChildren().add(rect);
                    }
                    */
                }
                
                /*
                // Redraw terminal cells
                for(Point2D cell : terminalCells) {
                    Rectangle rect = new Rectangle(cell.getX() * CELL_WIDTH, cell.getY() * CELL_HEIGHT,
                                                        CELL_WIDTH, CELL_HEIGHT);
                    rect.setStroke(Color.BLACK);
                    rect.setStrokeWidth(5.0);
                    rect.setStrokeType(StrokeType.INSIDE);
                    rect.setStrokeLineJoin(StrokeLineJoin.ROUND);
                    rect.setStrokeLineCap(StrokeLineCap.ROUND);
                    rect.setFill(COLORS[wire]);
                    grid.getChildren().addAll(rect);
                }
                */
            }

            for (Point2D cell : shared.keySet()) {
                int usage = shared.get(cell);
                int usagePresent = (usage & ((1 << numWires) - 1));
                int usagePresentAmount = Integer.bitCount(usagePresent);
                if (usagePresentAmount > 1)
                {
                    retry = true;
                    int usageHistory = (usage >> numWires);
                    usageHistory += (usagePresentAmount - 1); 
                    shared.put(cell, ((usageHistory << numWires) | usagePresent));
                }
            }
        } while (retry);

        System.out.printf("ITERATIONS : %d\n", iter);
    }

    public Set<INode> routeWire(LinkedList<Point2D> terminalCells, int wireID) {
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
                List<INode> childNodes = findNeighborNodes(leafNode, terminalNode, wireID);
                
                for (INode childNode : childNodes) {
                    if (!(closed.contains(childNode) || open.contains(childNode))) {
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

    public double normalizeDistance(double distance){
        return distance/Math.hypot(grid.getWidth(), grid.getHeight());
    }

    public List<INode> findNeighborNodes(INode iNode, INode terminalNode, int wire){ 
        List<INode> neighborNodes = new LinkedList<>();
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                double x = iNode.getX() + (2 * j - 1) *    i    ;
                double y = iNode.getY() + (2 * j - 1) * (1 - i) ;
                Point2D cell = new Point2D(x, y);
                
                boolean isValidCell = true;
                for (int w = 0; w < grid.getWires().size(); ++w) {
                    if (w != wire && grid.getWires().get(w).contains(cell)){
                        isValidCell = false;
                        break;
                    }
                }

                if(isValidCell && grid.getAllCells().contains(cell) && !(grid.getObstructedCells().contains(cell))) {
                    INode neighborNode = new INode(cell);
                    
                    int neighborUsagePresent = 0;
                    int neighborUsageHistory = 0;
                    if (shared.containsKey(neighborNode))
                    {
                        int neighborUsage = shared.get(neighborNode);
                        neighborUsageHistory = neighborUsage >> numWires;
                        neighborUsagePresent = Integer.bitCount(neighborUsage & ((1 << numWires) - 1) & (~(1 << wire)));
                    }
                    
                    neighborNode.setParent(iNode);
                    double parentCost = neighborNode.getParent().getCost();
                    double stepCost = 8 * neighborUsageHistory * (1 + neighborUsagePresent); // hn * pn
                    double cost = parentCost + stepCost;
                    if (algo == Algo.A_STAR)
                    {
                        double heurCost = normalizeDistance(neighborNode.distance(terminalNode));
                        cost +=  heurCost;
                    }
                    
                    neighborNode.setCost(cost);
                    neighborNodes.add(neighborNode);
                }
            }
        }
        return neighborNodes;
    }
}