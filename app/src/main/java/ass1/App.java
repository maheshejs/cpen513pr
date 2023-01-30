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
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application{
    ///////////////////////////////////////////////////////////////////////////////////////
    private Algo algo = Algo.A_STAR; // or Algo.LEE_MOORE
    private Grid grid = new Grid("oswald.infile"); // Default benchmark file
    ///////////////////////////////////////////////////////////////////////////////////////
    private Congestion congestion = new Congestion(grid.getSharedCells(), grid.getWires().size());

    @Override
    public void start(Stage stage){
        /////////////////////////////////////////////////////////////////////
        /////////////////////// GRAPHICS - SETUP START ////////////////////// 
        /////////////////////////////////////////////////////////////////////
        Button button = new Button("Route all wires");
        button.setOnAction(e -> {
                                 routeAllWires();
                                 grid.animate();
                                });
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(25, 25, 25, 25));
        vBox.getChildren().addAll(grid, button);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("Routing");
        stage.show();
        /////////////////////////////////////////////////////////////////////
        /////////////////////// GRAPHICS - SETUP END //////////////////////// 
        /////////////////////////////////////////////////////////////////////
    }
    public static void main(String[] args) {
        Application.launch(args);
    }

    /** Routes all wires
     */
    public void routeAllWires() {
        // When Negotiated Congestion algorithm fails after NUM_ITERATIONS iterations
        //  we switch to a greedy algorithm with congestion history
        boolean isGreedy = false;

        int iteration = 1;
        int wiresRouted = 0;

        for (; iteration <= NUM_ITERATIONS + 1; ++iteration) {
            wiresRouted = 0;

            // Redraw grid
            grid.redraw();
            
            // Clear present congestions, but keep congestion history 
            congestion.clear();

            for (int wireID = 0; wireID < grid.getWires().size(); ++wireID) {
                List<Point2D> terminalCells = grid.getWires().get(wireID);
                RouteInfo routeInfo = routeWire(wireID, new ArrayDeque<>(terminalCells));
                List<INode> route = routeInfo.getRoute(); 

                if (routeInfo.isRouted())
                    ++wiresRouted;

                // Remove terminal cells in route so that route consists of only shared resources (cells)
                route.removeAll(terminalCells);

                // Add route to obstructed cells and remove it from shared cells
                if (isGreedy)
                    grid.updateCells(route);

                // Update present congestions after routing each wire
                congestion.updatePresent(route, wireID);
                
                // Draw route
                grid.drawRoute(route, wireID);
            }

            // Update congestion history after each iteration and update hasCongestions
            congestion.updateHistory();

            if (!congestion.hasCongestions())
                break;
            else if (iteration == NUM_ITERATIONS - 1)
                isGreedy = true;
        }
        System.out.printf("ITERATIONS TAKEN : %d | WIRES SUCCESSFULLY ROUTED : %d/%d \n", 
                            iteration, wiresRouted, grid.getWires().size());
    }

    /** Routes all terminal cells of a wire
     * @param wireID the wire ID
     * @param terminalCells the terminal cells of the wire
     * @return route info which consits of route and route status, is successfully routed or not
     */
    public RouteInfo routeWire(int wireID, Queue<Point2D> terminalCells) {
        List<INode> route  = new ArrayList<>();
        Comparator<INode> comparator = Comparator.comparing(INode::getCost, Comparator.naturalOrder());
        Queue<INode> frontier = new PriorityQueue<>(comparator);
        Set<INode> explored = new HashSet<>();
        boolean isRouted = true;
        route.add(new INode(terminalCells.remove()));

        while (!terminalCells.isEmpty()){
            INode terminalNode = new INode(terminalCells.remove());
            for (INode iNode : route) {
                iNode.setParent(null);
                if (algo == Algo.A_STAR)
                    iNode.setCost(iNode.manhattanDistance(terminalNode));
                else
                    iNode.setCost(0);
                frontier.add(iNode);
            }

            explored.clear();
            while (true) {
                // Failure
                if (frontier.isEmpty()) {
                    isRouted = false;
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
                // ONLY FOR DEBUGGING PURPOSES : Draw explored node
                if (IS_DEBUG)
                    grid.drawExploredNode(leafNode);
                
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
        return new RouteInfo(route, isRouted);
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
                    double heurCost = neighborNode.manhattanDistance(terminalNode);
                    cost += heurCost;
                }
                neighborNode.setCost(cost);
                neighborNodes.add(neighborNode);
            }
        }
        return neighborNodes;
    }
} 