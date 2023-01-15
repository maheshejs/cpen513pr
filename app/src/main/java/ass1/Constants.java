package ass1;
import javafx.scene.paint.Color;
public class Constants{
    private Constants () {}
    static final double CELL_WIDTH   = 25;
    static final double CELL_HEIGHT  = 25;
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