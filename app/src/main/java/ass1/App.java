package ass1;
import static ass1.Constants.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class App extends Application{
    private Comparator<INode> comparator = Comparator.comparing(INode::getCost, Comparator.naturalOrder());
    private Queue<INode> open = new PriorityQueue<>(comparator);
    private Set<INode> closed = new HashSet<>();
    private Set<INode> route  = new HashSet<>();
    private Grid grid = new Grid("wavy.infile"); 
    private LinkedList<Point2D> terminalCells = grid.getNetworks().get(0);

    @Override
    public void start(Stage stage){
        Algo algo = Algo.A_STAR;
        Button button = new Button("Route");
        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);

        button.setOnAction(e -> findPath(algo));
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(25, 25, 25, 25));
        vBox.getChildren().addAll(grid, button);
        stage.setScene(scene);
        stage.setTitle("Routing");
        stage.show();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }

    public void findPath(Algo algo){
        // Copy terminal cells to redraw them later
        LinkedList<Point2D> copyTerminalCells = new LinkedList<>();
        for (Point2D cell : terminalCells)
            copyTerminalCells.add(cell);

        route.add(new INode(terminalCells.remove()));
        while (!terminalCells.isEmpty()) {
            INode terminalNode = new INode(terminalCells.remove());

            for (INode iNode : route) {
                iNode.setParent(null);
                iNode.setCost(iNode.distance(terminalNode));
                open.add(iNode);
            }

            closed.clear();

            while (true) {
                if (open.isEmpty()) {
                    route.add(terminalNode);
                    break; // failure
                }

                INode leafNode = open.remove();

                if (leafNode.equals(terminalNode)) {
                    INode iNode = leafNode;
                    while (iNode != null) {
                        if (iNode.getParent() != null)
                            route.add(iNode);
                        iNode = iNode.getParent();
                    }
                    break; //success
                }

                closed.add(leafNode);
                List<INode> childNodes = findNeighborNodes(leafNode, terminalNode, algo);
                
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

        // Draw route
        for(INode iNode : route){
            Rectangle rect = new Rectangle(iNode.getX() * CELL_WIDTH, iNode.getY() * CELL_HEIGHT,
                                                CELL_WIDTH, CELL_HEIGHT);
            rect.setStroke(Color.BLUE);
            rect.setFill(Color.PURPLE);
            grid.getChildren().add(rect);
        }

        // Redraw terminal cells
        for(Point2D cell : copyTerminalCells){
            Rectangle rect = new Rectangle(cell.getX() * CELL_WIDTH, cell.getY() * CELL_HEIGHT,
                                                CELL_WIDTH, CELL_HEIGHT);
            rect.setStroke(Color.BLUE);
            rect.setFill(Color.RED);
            grid.getChildren().add(rect);
        }
    }

    public List<INode> findNeighborNodes(INode iNode, INode terminalNode, Algo algo){ 
        List<INode> neighborNodes = new LinkedList<>();
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                double x = iNode.getX() + (2 * j - 1) *    i    ;
                double y = iNode.getY() + (2 * j - 1) * (1 - i) ;
                Point2D point = new Point2D(x, y);

                if(grid.getCells().contains(point) && !(grid.getObstructedCells().contains(point))){
                    INode neighborNode = new INode(point);
                    neighborNode.setParent(iNode);
                    double parentCost = neighborNode.getParent().getCost();
                    double heurCost = neighborNode.distance(terminalNode);
                    double stepCost = neighborNode.distance(neighborNode.getParent());
                    double cost = parentCost + stepCost;
                    if (algo == Algo.A_STAR)
                        cost +=  heurCost;

                    neighborNode.setCost(cost);
                    neighborNodes.add(neighborNode);
                }
            }
        }
        return neighborNodes;
    }
}