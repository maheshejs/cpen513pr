package ass1;
import static ass1.Constants.*;
import java.util.HashSet;
import java.util.Set;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Field extends Group{
    static Set<Point2D> allPoints  = new HashSet<>();
    static Set<Point2D> forbPoints = new HashSet<>();
    static INode startNode = new INode(); 
    static INode endNode   = new INode();
    static boolean isFirstPrimaryMouseEvent   = true;
    static boolean isFirstSecondaryMouseEvent = true;

    Field(){
        super(); 
        double y = START_Y;
        for(int i = 0 ; i < SIZE; i++){
            double x = START_X;
            for(int j = 0; j < SIZE; j++){
                GBox gBox = new GBox(x, y);
                allPoints.add(new Point2D(x, y));
                this.getChildren().add(gBox);
                x += WIDTH;
            }
            y += HEIGHT;
        }
    }

    private class GBox extends Rectangle{
        private GBox(double x, double y){
            super(x, y, WIDTH, HEIGHT);
            this.setStroke(Color.BLUE);
            this.setFill(Color.WHITE);
            this.setOnMouseClicked(e -> action (e));
        }
        
        private void action (MouseEvent e){
            Point2D point = new Point2D(this.getX(), this.getY());
            if(e.getButton().equals(MouseButton.PRIMARY)){
                if(isFirstPrimaryMouseEvent){
                    isFirstPrimaryMouseEvent = false;
                    Field.startNode = new INode(point);
                    this.setFill(Color.RED);
                }
                else{
                    forbPoints.add(point);
                    this.setFill(Color.BLACK);
                }
            }
            else if(e.getButton().equals(MouseButton.SECONDARY)){
                if(isFirstSecondaryMouseEvent){
                    isFirstSecondaryMouseEvent = false;
                    Field.endNode = new INode(point);
                    this.setFill(Color.BLUE);
                }
            }
        }
    }
}