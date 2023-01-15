package ass1;

import static ass1.Constants.*;
import java.util.List;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Grid extends Group{
    private int width  = 10;
    private int height = 10; 
    private int numNetworks = 1;
    private int numObstructedCells = 0;
    private Set<Point2D> cells;
    private Set<Point2D> obstructedCells;
    private LinkedList<LinkedList<Point2D>> networks;

    Grid(String benchmark){
        super();

        cells = new HashSet<>();
        obstructedCells = new HashSet<>();
        networks = new LinkedList<>();

        parseBenchmark(benchmark);

        for(int x = 0 ; x < this.width; ++x) {
            for(int y = 0; y < this.height; ++y) {
                //allPoints.add(new Point2D(x, y));
                GBox gBox = new GBox(x, y);
                Point2D cell = new Point2D(x, y);

                if (obstructedCells.contains(cell))
                    gBox.setFill(Color.BLUE);
                
                for (int i = 0; i < numNetworks; ++i) {
                    LinkedList<Point2D> terminalCells = networks.get(i);
                    if (terminalCells.contains(cell))
                        gBox.setFill(COLORS[i]);
                }
                
                this.cells.add(cell);
                this.getChildren().add(gBox);
            }
        }
    }

    public void parseBenchmark(String benchmark) {
        try{
            Scanner scanner = new Scanner(new File("build/benchmarks/" + benchmark));
            int[] pair = new int[2];

            for (int i = 0; i < pair.length; ++i) 
                pair[i] = scanner.nextInt();

            this.width  = pair[0];
            this.height = pair[1];
            this.numObstructedCells = scanner.nextInt();

            for (int i = 0; i < this.numObstructedCells; ++i) {
                for (int j = 0; j < 2; ++j)
                    pair[j] = scanner.nextInt();
                this.obstructedCells.add(new Point2D(pair[0], pair[1]));
            }

            this.numNetworks = scanner.nextInt();

            for (int i = 0; i < this.numNetworks; ++i) {
                int numTerminalCells = scanner.nextInt();
                LinkedList<Point2D> terminalCells = new LinkedList<>();
                for (int j = 0; j < numTerminalCells; ++j) {
                    for (int k = 0; k < 2; ++k)
                        pair[k] = scanner.nextInt();
                    terminalCells.add(new Point2D(pair[0], pair[1]));
                }
                this.networks.add(terminalCells);
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("--- Unable to load benchmark file ---");
            e.printStackTrace();
        }

    }

    public Set<Point2D> getCells() {
        return this.cells;
    }
    
    public Set<Point2D> getObstructedCells() {
        return this.obstructedCells;
    }
    
    public LinkedList<LinkedList<Point2D>> getNetworks() {
        return this.networks;
    }
    private class GBox extends Rectangle{
        private GBox(double x, double y){
            super(x * CELL_WIDTH, y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
            this.setStroke(Color.BLACK);
            this.setFill(Color.WHITE);
            this.setOnMouseClicked(e -> action (e));
        }
        
        // Manually add obstructed cells by clicking on grid
        private void action (MouseEvent e){
            Point2D point = new Point2D(this.getX(), this.getY());
            if(e.getButton().equals(MouseButton.PRIMARY)){
                Grid.this.obstructedCells.add(point);
                this.setFill(Color.BLUE);
            }
        }
    }
}