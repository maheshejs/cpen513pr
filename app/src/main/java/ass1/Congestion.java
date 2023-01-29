package ass1;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import javafx.geometry.Point2D;

public class Congestion {
    private int numWires;
    private Map<Point2D, Integer> congestionMap = new HashMap<>();
    private boolean hasCongestions;

    public Congestion(Set<Point2D> sharedCells, int numWires) {
        this.numWires = numWires;
        this.congestionMap = new HashMap<>();
        for (Point2D cell : sharedCells) 
            congestionMap.put(cell, (1 << numWires));
        this.hasCongestions = false;
    } 
   
    public int getHistory(Point2D cell) {
        if (congestionMap.containsKey(cell))
            return congestionMap.get(cell) >> numWires;
        return 0;
    }
    
    public void updateHistory() {
        hasCongestions = false;
        for (Point2D cell : congestionMap.keySet()) {
            int history = this.getHistory(cell);
            int present = this.getPresent(cell);
            if (present > 1)
            {
                hasCongestions = true;
                // Update congestion history each iteration
                history += present - 1; 
                congestionMap.put(cell, ((history << numWires) | present));
            }
        }
    }

    public int getPresent(Point2D cell) {
        if (congestionMap.containsKey(cell))
            return Integer.bitCount(congestionMap.get(cell) & ((1 << numWires) - 1));
        return 0;
    }

    public void updatePresent(Set<INode> route, int wireID) {
        for(Point2D cell : route)
            congestionMap.put(cell, congestionMap.get(cell) | (1 << wireID));
    }

    public void clear() {
        congestionMap.replaceAll((key, value) -> ((value >> numWires) << numWires));
        hasCongestions = false;
    }

    public boolean hasCongestions() {
        return hasCongestions;
    }
}
