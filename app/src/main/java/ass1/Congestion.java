package ass1;
import java.util.List;
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
   
    /** Returns the congestion history of the given cell.
     * @param cell the cell to get the congestion history of
     * @return the congestion history of the given cell
     *         0 if cell is not in the congestion map
    */
    public int getHistory(Point2D cell) {
        if (congestionMap.containsKey(cell))
            return congestionMap.get(cell) >> numWires;
        return 0;
    }
    
    /**
     * Updates the congestion history for all cells in the congestion map.
     * Also, updates boolean hasCongestions.
    */
    public void updateHistory() {
        hasCongestions = false;
        for (Point2D cell : congestionMap.keySet()) {
            int history = this.getHistory(cell);
            int present = this.getPresent(cell);
            if (present > 1)
            {
                hasCongestions = true;
                history += present - 1; 
                congestionMap.put(cell, ((history << numWires) | present));
            }
        }
    }

    /** Returns the present congestion of the given cell.
     * @param cell the cell to get the present congestion of
     * @return the present congestion of the given cell
     *         0 if cell is not in the congestion map
    */
    public int getPresent(Point2D cell) {
        if (congestionMap.containsKey(cell))
            return Integer.bitCount(congestionMap.get(cell) & ((1 << numWires) - 1));
        return 0;
    }

    /** Updates the present congestion for all cells in the given route identified by wire ID.
     * @param route the route
     * @param wireID the wire ID 
    */
    public void updatePresent(List<INode> route, int wireID) {
        for(Point2D cell : route)
            congestionMap.put(cell, congestionMap.get(cell) | (1 << wireID));
    }

    /**
     * Clears the present congestion for all cells in the congestion map,
     * but keep congestion history.
     * Also, clears boolean hasCongestions
    */
    public void clear() {
        congestionMap.replaceAll((key, value) -> ((value >> numWires) << numWires));
        hasCongestions = false;
    }

    public boolean hasCongestions() {
        return hasCongestions;
    }
}