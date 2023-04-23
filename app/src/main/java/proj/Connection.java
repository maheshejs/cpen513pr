package proj;

import java.util.function.Function;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import javafx.geometry.Point2D;
import javafx.util.Pair;

/**
 * Represents a connection
 */
public class Connection {
    private List<Queue<Pair<Integer, Point2D>>> queues;

    /**
     * Creates a new connection
     */
    public Connection () {
        Function<Pair<Integer, Point2D>, Point2D> function = Pair<Integer, Point2D>::getValue;
        Comparator<Pair<Integer, Point2D>> compX = Comparator.comparing(function.andThen(Point2D::getX));
        Comparator<Pair<Integer, Point2D>> compY = Comparator.comparing(function.andThen(Point2D::getY));
        // Initialize 4 priority queues, one for each direction
        queues = new ArrayList<>();
        queues.add(new PriorityQueue<>(compX));
        queues.add(new PriorityQueue<>(compX.reversed()));
        queues.add(new PriorityQueue<>(compY));
        queues.add(new PriorityQueue<>(compY.reversed()));
    }

    /**
     * Adds a block placement to the connection
     * @param blockIndex the index of the block
     * @param loc the location of the block
     */
    public void addBlockPlacement (int blockIndex, Point2D loc) {
        queues.forEach(e -> e.add(new Pair<>(blockIndex, loc)));
    }

    /**
     * Removes a block placement from the connection
     * @param blockIndex the index of the block
     * @param loc the location of the block
     */
    public void removeBlockPlacement (int blockIndex, Point2D loc) {
        queues.forEach(e -> e.remove(new Pair<>(blockIndex, loc)));
    }
    
    /**
     * Moves a block placement from one location to another
     * @param blockIndex the index of the block
     * @param fromLoc the location of the block before the move
     * @param toLoc the location of the block after the move
     */
    public void moveBlockPlacement (int blockIndex, Point2D fromLoc, Point2D toLoc) {
        this.removeBlockPlacement(blockIndex, fromLoc);
        this.addBlockPlacement(blockIndex, toLoc);
    }

    /**
     * Returns the cost of the connection
     * @return the cost of the connection
     */
    public double getCost () {
        Point2D[] locs = queues.stream()
                               .map(e -> e.peek().getValue())
                               .toArray(Point2D[]::new);
        double width  = locs[1].getX() - locs[0].getX();
        double height = locs[3].getY() - locs[2].getY();
        return width + height;
    }

    /**
     * Returns block placements in the connection
     * @return block placements in the connection
     */
    public List<Pair<Integer, Point2D>> getBlockPlacements () {
        return queues.get(0)
                     .stream()
                     .toList();
    }
    
    /**
     * Returns the list of block indexes
     * @return the list of block indexes
     */
    public List<Integer> getBlockIndexes () {
        return queues.get(0)
                     .stream()
                     .map(e -> e.getKey())
                     .toList();
    }

    /**
     * Returns the string representation of the connection
     * @return the string representation of the connection
     */
    @Override
    public String toString () {
        return queues.get(0)
                     .stream()
                     .map(e -> String.format("%d@[x = %.1f , y = %.1f]", e.getKey(), e.getValue().getX(), e.getValue().getY()))
                     .collect(Collectors.joining(" - "));
    }
}