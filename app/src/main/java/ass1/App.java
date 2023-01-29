package ass1;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.PriorityQueue;

import static ass1.Constants.*;
import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
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
    private Grid grid = new Grid("oswald.infile"); 
    private int numWires = grid.getWires().size();
    private Congestion congestion = new Congestion(grid.getSharedCells(), numWires);
    private int frame = 0;
    private Comparator<INode> iNodeComparator = Comparator.comparing(INode::getCost, Comparator.naturalOrder());

    @Override
    public void start(Stage stage){
        Timeline timeline = new Timeline();
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
        button.setOnAction(e -> {
                                routeAllWires(timeline);
                                 timeline.play();
                                });

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


    /** Routes all wires
     * @param timeline the timeline to schedule drawing of cells
     */
    public void routeAllWires(Timeline timeline) {
        // When Negotiated Congestion algorithm fails after NUM_ITERATIONS iterations
        //  we switch to a greedy algorithm with congestion history
        boolean isGreedy = false;

        int iteration;

        for (iteration = 1; iteration <= NUM_ITERATIONS + 1; ++iteration) {
            // Redraw grid
            grid.redrawGrid(timeline, getFrameDuration(frame));
            
            // Clear present congestions, but keep congestion history 
            congestion.clear();

            for (int wireID = 0; wireID < numWires; ++wireID) {
                List<Point2D> terminalCells = grid.getWires().get(wireID);
                List<INode> route = routeWire(wireID, new ArrayDeque<>(terminalCells));

                // Remove terminal cells in route so that route consists of only shared resources (cells)
                route.removeAll(terminalCells);

                // Add route to obstructed cells and remove it from shared cells
                if (isGreedy)
                    grid.updateCells(route);

                // Update present congestions after routing each wire
                congestion.updatePresent(route, wireID);
                
                // Draw route
                drawRoute(route, wireID, timeline);
            }

            // Update congestion history after each iteration and update hasCongestions
            congestion.updateHistory();

            if (!congestion.hasCongestions())
                break;
            else if (iteration == NUM_ITERATIONS - 1)
            {
                System.out.println(iteration);
                isGreedy = true;
            }
        } 
        System.out.printf("ITERATIONS : %d\n", iteration);
    }

    /** Routes all terminal cells of a wire
     * @param wireID the wire ID
     * @param terminalCells the terminal cells of the wire
     * @return the route of the wire
     */
    public List<INode> routeWire(int wireID, Queue<Point2D> terminalCells) {
        List<INode> route  = new ArrayList<>();
        Queue<INode> frontier = new PriorityQueue<>(iNodeComparator);
        Set<INode> explored = new HashSet<>();
                
        route.add(new INode(terminalCells.remove()));

        while (!terminalCells.isEmpty()){
            INode terminalNode = new INode(terminalCells.remove());

            for (INode iNode : route) {
                iNode.setParent(null);
                if (algo == Algo.A_STAR)
                    iNode.setCost(normalizeDistance(iNode.distance(terminalNode)));
                else
                    iNode.setCost(0);
                frontier.add(iNode);
            }

            explored.clear();

            while (true) {
                // Failure
                if (frontier.isEmpty()) {
                    route.add(terminalNode);
                    break; 
                }

                INode leafNode = frontier.remove();
                // Success 
                if (leafNode.equals(terminalNode)) {
                    // Backtrack to construct route
                    INode iNode = leafNode;
                    while (iNode != null) {
                        if (iNode.getParent() != null)
                            route.add(iNode);
                        iNode = iNode.getParent();
                    }
                    break;
                }

                explored.add(leafNode);

                List<INode> childNodes = findNeighborNodes(leafNode, terminalNode, terminalCells);
                
                for (INode childNode : childNodes) {
                    // If childNode is not in frontier or explored, add it to frontier
                    if (!explored.contains(childNode) && !frontier.contains(childNode)) {
                        frontier.add(childNode);
                    }
                    else if (frontier.contains(childNode)) {
                        for (INode iNode : frontier) {
                            // If childNode is already in frontier, update cost if it is less than the cost of iNode
                            if (iNode.equals(childNode) && iNode.getCost() > childNode.getCost()) {
                                frontier.remove(iNode);
                                frontier.add(childNode);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return route;
    }

    /** Draws the route on the grid
     * Also, schedules drawing of each node (cell) of the route in a timeline
     * @param route the route to be drawn
     * @param wireID the wire ID
     * @param timeline the timeline
     */
    public void drawRoute (List<INode> route, int wireID, Timeline timeline) {
        for(INode iNode : route) {
            ++frame;
            KeyFrame keyFrame = new KeyFrame(getFrameDuration(frame), e -> { 
                String gBoxID = "#" + Grid.GBox.createID(iNode.getX(), iNode.getY());
                grid.lookup(gBoxID).setStyle(grid.getCellStyles().get("route" + wireID));});
            timeline.getKeyFrames().add(keyFrame);
        }
    }

    /** Normalizes distance according to the grid size
     * @param distance the distance to be normalized
     * @return normalized distance
     */
    public double normalizeDistance(double distance) {
        return distance / Math.hypot(grid.getWidth(), grid.getHeight());
    }

    /** Return duration of a frame
     * @param frame the frame
     * @return the duration
     */
    public Duration getFrameDuration(int frame) {
        return Duration.millis(FRAME_FACTOR * frame);
    }

    /** Finds the neighbor nodes of the current node
     * @param currentNode the current node 
     * @param terminalNode the current terminal node
     * @param terminalCells the remaining terminal cells (nodes)
     * @return the list of neighbor nodes
     */
    public List<INode> findNeighborNodes(INode currentNode, INode terminalNode, Queue<Point2D> terminalCells) { 
        List<INode> neighborNodes = new ArrayList<>();
        for (int i = 0; i < DIRECTIONS.length; ++i) {
            Point2D cell = new Point2D (currentNode.getX() + DIRECTIONS[i][0],
                                        currentNode.getY() + DIRECTIONS[i][1]);
            
            if(grid.getSharedCells().contains(cell) || terminalCells.contains(cell) || terminalNode.equals(cell) ) {
                INode neighborNode = new INode(cell);
                neighborNode.setParent(currentNode);
                double parentCost = neighborNode.getParent().getCost();
                                    /* Negotiation congestion cost :  k * hn * pn */
                double stepCost = CONGESTION_FACTOR * congestion.getHistory(neighborNode) * 
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