package ass1;
import static ass1.Constants.*;

import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
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
    private Field field = new Field(); 
    private Set<Point2D> allPoints  = Field.allPoints;
    private Set<Point2D> forbPoints = Field.forbPoints;
    private LinkedList<INode> terminalNodes = Field.terminalNodes;

    @Override
    public void start(Stage stage){
        Algo algo = Algo.A_STAR;
        Button button = new Button("Find path");
        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);

        button.setOnAction(e -> findPath(algo));
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(25, 25, 25, 25));
        vBox.getChildren().addAll(field, button);
        stage.setScene(scene);
        stage.setTitle("Shortest path");
        stage.show();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }

    public void findPath(Algo algo){
        // Copy terminal nodes to redraw them later
        List<INode> nodes = new LinkedList<>();
        for (INode iNode : terminalNodes)
            nodes.add(iNode);

        route.add(terminalNodes.remove());
        while (!terminalNodes.isEmpty()) {
            INode terminalNode = terminalNodes.remove();

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
            Rectangle rect = new Rectangle(iNode.getX(), iNode.getY(), WIDTH, HEIGHT);
            rect.setStroke(Color.BLUE);
            rect.setFill(Color.PURPLE);
            field.getChildren().add(rect);
        }

        // Redraw terminal nodes
        for(INode iNode : nodes){
            Rectangle rect = new Rectangle(iNode.getX(), iNode.getY(), WIDTH, HEIGHT);
            rect.setStroke(Color.BLUE);
            rect.setFill(Color.RED);
            field.getChildren().add(rect);
        }
    }

    public List<INode> findNeighborNodes(INode iNode, INode terminalNode, Algo algo){ 
        List<INode> neighborNodes = new LinkedList<>();
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                double x = iNode.getX() + (2 * j - 1) *    i    * WIDTH;
                double y = iNode.getY() + (2 * j - 1) * (1 - i) * HEIGHT;
                Point2D point = new Point2D(x, y);

                if(allPoints.contains(point) && !(forbPoints.contains(point))){
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