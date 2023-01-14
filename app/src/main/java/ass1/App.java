package ass1;
import static ass1.Constants.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

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
    private Field field = new Field(); 
    private Set<Point2D> allPoints = Field.allPoints;
    private Set<Point2D> forbPoints = Field.forbPoints;
    private INode startNode = new INode();
    private INode endNode = new INode();
    @Override
    public void start(Stage stage){
        VBox vBox = new VBox();
        Button btn = new Button("Find path");
        btn.setOnAction(e -> findPath());
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(25, 25, 25, 25));
        vBox.getChildren().addAll(field, btn);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("Shortest path");
        stage.show();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }
    public void findPath(){
        startNode = Field.startNode;
        endNode = Field.endNode;
        startNode.setCost(startNode.distance(endNode));
        open.offer(startNode);
        while(true){
            if(open.isEmpty()){
                System.out.println("No path found");
                break; //failure
            }
            INode leafNode = open.poll();
                    
            if(leafNode.equals(endNode)){
                List<Point2D> parcours = new ArrayList<>();
                INode parentLeafNode = leafNode.getParent();
                while(parentLeafNode != null){
                    if(parentLeafNode.getParent() != null)
                        parcours.add(parentLeafNode);
                    parentLeafNode = parentLeafNode.getParent();
                }
                for(Point2D point : parcours){
                    // Solution
                    Rectangle rect = new Rectangle(point.getX(), point.getY(), WIDTH, HEIGHT);
                    rect.setStroke(Color.BLUE);
                    rect.setFill(Color.PURPLE);
                    field.getChildren().add(rect);
                }

                break; //success
            }
            closed.add(leafNode);
            List<INode> childrenNodes = findNeighborNodes(leafNode);
            for(INode childNode : childrenNodes){
                if(!(closed.contains(childNode) || open.contains(childNode))){
                    open.offer(childNode);
                }
                else if (open.contains(childNode)){
                    for(INode iterNode : open){
                        if(iterNode.equals(childNode) && iterNode.getCost()>childNode.getCost()){
                            open.remove(iterNode);
                            open.offer(childNode);
                            break;
                        }
                    }
                }
            }
        }
    }

    public List<INode> findNeighborNodes(INode iNode){ 
        List<INode> neighborNodes = new ArrayList<>();
        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 2; j++){
                double x = iNode.getX() + (2 * j - 1) *    i    * WIDTH;
                double y = iNode.getY() + (2 * j - 1) * (1 - i) * HEIGHT;
                Point2D point = new Point2D(x, y);

                if(allPoints.contains(point) && !(forbPoints.contains(point))){
                    INode neighborNode = new INode(point);
                    neighborNode.setParent(iNode);
                    double parentCost = neighborNode.getParent().getCost();
                    double heurCost = neighborNode.distance(endNode);
                    double stepCost = neighborNode.distance(neighborNode.getParent());
                    double cost = parentCost + heurCost + stepCost;

                    neighborNode.setCost(cost);
                    neighborNodes.add(neighborNode);
                }
            }
        }
        return neighborNodes;
    }
}