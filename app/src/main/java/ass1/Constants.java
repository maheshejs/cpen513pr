package ass1;
import javafx.scene.paint.Color;
public class Constants{
    private Constants () {}
    static final double CELL_WIDTH   = 25;
    static final double CELL_HEIGHT  = 25;
    static final String[] TERMINAL_COLORS = {   "red", "yellow", "green", 
                                                "orange", "purple", "sienna",
                                                " gray", "magenta"  };
    static final Color[] COLORS = { 
                                    Color.RED, 
                                    Color.YELLOW, 
                                    Color.GREEN, 
                                    Color.ORANGE,
                                    Color.PURPLE, 
                                    Color.SIENNA, 
                                    Color.GRAY, 
                                    Color.MAGENTA
                                  };
    enum Algo {
        LEE_MOORE,
        A_STAR,
    }
}
                        
                        /*
                        if (wire == 0 && i == NUM_ITERATIONS-1)
                        {
                            System.out.println(leafNode);
                            Rectangle rect = new Rectangle(leafNode.getX() * CELL_WIDTH, leafNode.getY() * CELL_HEIGHT,
                                                                CELL_WIDTH, CELL_HEIGHT);
                            rect.setStroke(Color.BLUE);
                            rect.setFill(Color.BLACK);
                            grid.getChildren().add(rect);
                        }
                        */