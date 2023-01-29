package ass1;

import static ass1.Constants.*;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.FileNotFoundException;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class Grid extends Group{
    private int width;
    private int height; 
    private int numWires;
    private int numObstructedCells;
    private Set<Point2D> allCells;
    private Set<Point2D> obstructedCells;
    private Set<Point2D> sharedCells;
    private LinkedList<LinkedList<Point2D>> wires;
    private Map<String, String> cellStyles = new HashMap<>();

    Grid(String benchmarkFile) {
        super();

        width = 0;
        height = 0; 
        numObstructedCells = 0;
        numWires = 0;
        allCells = new HashSet<>();
        obstructedCells = new HashSet<>();
        sharedCells = new HashSet<>();
        wires = new LinkedList<>();
        cellStyles = new HashMap<>();

        parseBenchmark(benchmarkFile);
        setAllCells();
        setSharedCells();
        setCellStyles();
        drawGrid();
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
                LinkedList<Point2D> terminalCells = new LinkedList<>();
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
        for (LinkedList<Point2D> terminalCells : wires)
            sharedCells.removeAll(terminalCells);
    }

    /**
     * Sets cell styles used to display the grid.
    */
    private void setCellStyles() {
        // Add the style for obstructed cells
        cellStyles.put("obstructed", "-fx-fill: blue; -fx-stroke: black");
        // Add the style for shared cells
        cellStyles.put("shared", "-fx-fill: white; -fx-stroke: black");
        // Add the styles for terminal cells
        for (int i = 0; i < TERMINAL_COLORS.length; ++i) {
            cellStyles.put("route"    + i, "-fx-fill: " + TERMINAL_COLORS[i] + "; " + "-fx-stroke: blue");
            cellStyles.put("terminal" + i, "-fx-fill: " + TERMINAL_COLORS[i] + "; " +
                                "-fx-stroke: black; -fx-stroke-width: 5; -fx-stroke-type: inside; " +
                                "-fx-stroke-line-join: round; -fx-stroke-line-cap: round;");
        }
    }

    /**
     * Draws grid with obstructed, shared and terminal cells
    */
    public void drawGrid() {
        for (Point2D cell : allCells) {
            GBox gBox = new GBox(cell.getX(), cell.getY());
            if (obstructedCells.contains(cell)) {
                gBox.setStyle(cellStyles.get("obstructed"));
            }
            else if (sharedCells.contains(cell))
            {
                gBox.setStyle(cellStyles.get("shared"));
            }
            else {
                for (int wireID = 0; wireID < numWires; ++wireID) {
                    LinkedList<Point2D> terminalCells = wires.get(wireID);
                    if (terminalCells.contains(cell)) {
                        gBox.setStyle(cellStyles.get("terminal" + wireID));
                        break;
                    }
                }
            }
            this.getChildren().add(gBox);
        }
    }

    /**
     * Redraws grid by redrawing shared cells
    */
    public void redrawGrid() {
        for (Point2D cell : sharedCells)
        {
            String gBoxID = "#" + GBox.createID(cell.getX(), cell.getY());
            this.lookup(gBoxID).setStyle(cellStyles.get("shared"));
        }
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
     * @return a list of wires in the grid
     */
    public LinkedList<LinkedList<Point2D>> getWires() {
        return wires;
    }
    
    /**
     * @return cell styles used to display the grid.
     */
    public Map<String, String> getCellStyles() {
        return cellStyles;
    }

    public class GBox extends Rectangle {
        private GBox(double x, double y) {
            super(x * CELL_WIDTH, y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
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