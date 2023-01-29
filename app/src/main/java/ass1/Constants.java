package ass1;
public class Constants{
    private Constants () {}
    static final double CELL_WIDTH   = 25;
    static final double CELL_HEIGHT  = 25;
    static final String[] TERMINAL_COLORS = {   "red", "yellow", "green", 
                                                "orange", "purple", "sienna",
                                                " gray", "magenta"  };
    enum Algo {
        LEE_MOORE,
        A_STAR,
    }
}