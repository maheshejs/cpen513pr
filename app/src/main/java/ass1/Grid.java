package ass1;

import static ass1.Constants.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.FileNotFoundException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

public class Grid extends Group{
    private int width;
    private int height; 
    private int numWires;
    private int numObstructedCells;
    private int frame;
    private Set<Point2D> allCells;
    private Set<Point2D> obstructedCells;
    private Set<Point2D> sharedCells;
    private List<List<Point2D>> wires;
    private Map<String, String> cellStyles;
    private Timeline timeline;

    Grid(String benchmarkFile) {
        super();

        width = 0;
        height = 0; 
        numObstructedCells = 0;
        numWires = 0;
        frame = 0;
        allCells = new HashSet<>();
        obstructedCells = new HashSet<>();
        sharedCells = new HashSet<>();
        wires = new ArrayList<>();
        cellStyles = new HashMap<>();
        timeline = new Timeline();

        parseBenchmark(benchmarkFile);
        setAllCells();
        setSharedCells();
        setCellStyles();
        draw();
    }

    /**
     * Parses the benchmark file and sets the width, height, obstructed cells, and wires for the grid
     * @param benchmarkFile the file to be parsed
     */
    private void parseBenchmark(String benchmarkFile) {
        try{
            Scanner scanner = new Scanner(new File("build/benchmarks/" + benchmarkFile));
            
            width = scanner.nextInt();
            height = scanner.nextInt();
            numObstructedCells = scanner.nextInt();
            
            for (int i = 0; i < numObstructedCells; ++i) {
                int x = scanner.nextInt();
                int y = scanner.nextInt();
                obstructedCells.add(new Point2D(x, y));
            }

            numWires = scanner.nextInt();
            
            for (int i = 0; i < numWires; ++i) {
                int numTerminalCells = scanner.nextInt();
                List<Point2D> terminalCells = new ArrayList<>();
                for (int j = 0; j < numTerminalCells; ++j) {
                    int x = scanner.nextInt();
                    int y = scanner.nextInt();
                    terminalCells.add(new Point2D(x, y));
                }
                wires.add(terminalCells);
            }
            scanner.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Unable to load benchmark file: " + benchmarkFile);
            e.printStackTrace();
        }
    }

    /**
     * Sets a list of all cells in the grid
    */
    private void setAllCells() {
        for(int x = 0 ; x < width; ++x) 
            for(int y = 0; y < height; ++y)
                allCells.add(new Point2D(x, y));
    }

    /**
     * Sets a list of all shared resources (cells) in the grid
    */
    private void setSharedCells() {
        sharedCells.addAll(allCells);
        sharedCells.removeAll(obstructedCells);
        for (List<Point2D> terminalCells : wires)
            sharedCells.removeAll(terminalCells);
    }

    /**
     * Sets cell styles used to display the grid.
    */
    private void setCellStyles() {
        cellStyles.put("obstructed", "-fx-fill: blue; -fx-stroke: black");
        cellStyles.put("shared", "-fx-fill: white; -fx-stroke: black");
        cellStyles.put("explored", "-fx-fill: fuchsia; -fx-stroke: black");
        for (int i = 0; i < TERMINAL_COLORS.length; ++i) {
            cellStyles.put("route"    + i, "-fx-fill: " + TERMINAL_COLORS[i] + "; " + "-fx-stroke: blue");
            cellStyles.put("terminal" + i, "-fx-fill: " + TERMINAL_COLORS[i] + "; " +
                                "-fx-stroke: black; -fx-stroke-width: 3; -fx-stroke-type: inside; " +
                                "-fx-stroke-line-join: round; -fx-stroke-line-cap: round;");
        }
    }

    /** Add route nodes (cells) to obstructed cells and remove them from shared cells
     *  when negotiated congestion algorithm and we switch to a greedy algorithm
     * @param cells to be added
     */
    public void updateCells(List<INode> route) {
        obstructedCells.addAll(route);
        sharedCells.removeAll(route);
    }

    /**
     * Draws grid with obstructed, shared and terminal cells
    */
    public void draw() {
        // Get screen size
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double cellWidth = primaryScreenBounds.getWidth() / width;
        double cellHeight = primaryScreenBounds.getHeight() / height;
        double cellSize = 0.95 * Math.min(cellWidth, cellHeight);
        
        for (Point2D cell : allCells) {
            GBox gBox = new GBox(cell.getX(), cell.getY(), cellSize);
            if (obstructedCells.contains(cell)) {
                gBox.setStyle(cellStyles.get("obstructed"));
            }
            else if (sharedCells.contains(cell))
            {
                gBox.setStyle(cellStyles.get("shared"));
            }
            else {
                for (int wireID = 0; wireID < numWires; ++wireID) {
                    List<Point2D> terminalCells = wires.get(wireID);
                    if (terminalCells.contains(cell)) {
                        gBox.setStyle(cellStyles.get("terminal" + wireID));
                        break;
                    }
                }
            }
            this.getChildren().add(gBox);
        }
    }
    
    /** Redraws grid and schedules in a timeline 
     * @param timeline the timeline
     */
    public void redraw() {
        for (Point2D cell : sharedCells)
        {
            //KeyFrame keyFrame = new KeyFrame(getFrameDuration(frame), e -> { 
                String gBoxID = "#" + GBox.createID(cell.getX(), cell.getY());
                this.lookup(gBoxID).setStyle(cellStyles.get("shared"));
            //});
            //timeline.getKeyFrames().add(keyFrame);
        }
    }

    /** Draws the route on the grid and schedules it in a timeline
     * @param route the route to be drawn
     * @param wireID the wire ID
     * @param timeline the timeline
     */
    public void drawRoute (List<INode> route, int wireID) {
        for(INode iNode : route) {
            //KeyFrame keyFrame = new KeyFrame(getFrameDuration(++frame), e -> { 
                String gBoxID = "#" + GBox.createID(iNode.getX(), iNode.getY());
                this.lookup(gBoxID).setStyle(cellStyles.get("route" + wireID));
            //});
            //timeline.getKeyFrames().add(keyFrame);
        }
    }

    /** ONLY FOR DEBUGGING PURPOSES
     * Draws explored node on the grid and schedules it in a timeline
     * @param exploredNode explored node to be drawn
     * @param timeline the timeline
     */
    public void drawExploredNode(INode exploredNode) {
        if (sharedCells.contains(exploredNode))
        {
            ++frame;
            //KeyFrame keyFrame = new KeyFrame(getFrameDuration(frame), e -> { 
                String gBoxID = "#" + GBox.createID(exploredNode.getX(), exploredNode.getY());
                this.lookup(gBoxID).setStyle(cellStyles.get("explored"));
            //});
            //timeline.getKeyFrames().add(keyFrame);
        }
    }

    public void animate() {
        timeline.play();
    }
    
    /** Returns duration of a frame
     * @param frame the frame
     * @return the duration
     */
    public static Duration getFrameDuration(int frame) {
        return Duration.millis(FRAME_FACTOR * frame);
    }

    /**
     * @return the width of the grid
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * @return the height of the grid
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * @return a list of all cells in the grid
     */
    public Set<Point2D> getAllCells() {
        return allCells;
    }
    
    /**
     * @return a list of all obstructed cells in the grid
     */
    public Set<Point2D> getObstructedCells() {
        return obstructedCells;
    }
    
    /**
     * @return a list of all shared resources (cells) in the grid
     */
    public Set<Point2D> getSharedCells() {
        return sharedCells;
    }
    
    /**
     * @return a list of wires in the grid, each wire consists of its terminal cells
     */
    public List<List<Point2D>> getWires() {
        return wires;
    }
    
    public class GBox extends Rectangle {
        private GBox(double x, double y, double cellSize) {
            super(x * cellSize, y * cellSize, cellSize, cellSize);
            this.setId(createID(x, y));
            this.setOnMouseClicked(e -> action (e));
        }
        
        // Manually add obstructed allCells by clicking on grid for debugging purposes
        private void action (MouseEvent e) {
            Point2D point = new Point2D(this.getX(), this.getY());
            if(e.getButton().equals(MouseButton.PRIMARY)){
                Grid.this.obstructedCells.add(point);
                this.setStyle(cellStyles.get("obstructed"));
            }
        }

        public static String createID(double x, double y) {
            return String.format("gBox|%dx%d", (int)x, (int)y);
        }
    }
}