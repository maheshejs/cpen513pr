package ass1;
public class Constants{
    private Constants () {}
    static final double CONGESTION_WEIGHT = 8;
    static final int NUM_ITERATIONS = 500;
    static final String[] TERMINAL_COLORS = {   "red", "yellow", "green", 
                                                "orange", "purple", "sienna",
                                                " gray", "magenta"  };
    static final int[][] DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
    enum Algo {
        LEE_MOORE,
        A_STAR,
    }
}