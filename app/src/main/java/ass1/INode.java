package ass1;
import javafx.geometry.Point2D;

public class INode extends Point2D { 
    private double cost = 0;
    private INode parent = null;

    INode(){
        super(0, 0);
    } 

    INode(Point2D point) {
        super(point.getX(), point.getY());
    }

    void setCost(double cost){
        this.cost = cost;
    }

    double getCost(){
        return cost;
    }

    void setParent(INode parent){
        this.parent = parent;
    }

    INode getParent(){
        return parent;
    }

    public double manhattanDistance(INode iNode){
        return Math.abs(this.getX() - iNode.getX()) + Math.abs(this.getY() - iNode.getY());
    }

    @Override
    public String toString(){
        return "INode [x = "+this.getX()+", y = "+this.getY()+"]" + "@Cost:" + Math.round(this.cost);
    }

}