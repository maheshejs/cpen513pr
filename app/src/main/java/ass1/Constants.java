package ass1;
public class Constants{
    private Constants () {}
    static final boolean IS_DEBUG = false;
    static final int CONGESTION_FACTOR = 16;
    static final int FRAME_FACTOR = 100;
    static final int NUM_ITERATIONS = 1000;
    static final int[][] DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
    static final String[] TERMINAL_COLORS = {   "red", "yellow", "lime", 
                                                "burlywood", "lightpink", "darkgreen",
                                                "#6495ed", "#b03060"  };
    static String BENCHMARKS[] = { "impossible", "impossible2", "kuma", "misty", "oswald", 
                                    "rusty", "stanley", "stdcell", "temp", "wavy"};
    static String ALGOS[]      = {"A*", "Lee-Moore"};
    enum Algo {
        LEE_MOORE,
        A_STAR,
    }
}