package proj;

import java.util.List;
import java.util.ArrayList;

import javafx.geometry.Point2D;
import javafx.util.Pair;

public class Utils {
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// Utility functions //////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Converts a boolean to an integer
     * @param b the boolean to convert
     * @return 1 if b is true, 0 otherwise
     */
    public static int boolToInt(boolean b) { 
        return b ? 1 : 0;
    }
    
    /**
     * Swap two elements in an array
     * @param array the array
     * @param i the first index
     * @param j the second index
     */
    public static final void swap (int[] array, int i, int j) {
        int t = array[i]; 
        array[i] = array[j]; 
        array[j] = t;
    }
    
    /**
     * Calculate the standard deviation of an array
     * @param array the array
     * @return the standard deviation
     */
    public static double calculateStandardDeviation(double[] array) {
        // get the sum of array
        double sum = 0.0;
        for (double i : array) {
            sum += i;
        }

        // get the mean of array
        int length = array.length;
        double mean = sum / length;

        // calculate the standard deviation
        double standardDeviation = 0.0;
        for (double num : array) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }
    
    /**
     * Returns the location at the given index
     * @param locIndex the location index
     * @return the location at the given index
     */
    public static Point2D getLoc (int locIndex, int numRows, int numCols, boolean useRowSpacing) {
        return new Point2D(locIndex % numCols, (locIndex / numCols) * (useRowSpacing ? 2 : 1));
    }

    public static List<Pair<Integer, Integer>> divideSegment (int length, int n) {
        List<Pair<Integer, Integer>> result = new ArrayList<>();
        int segmentLength = length / n;
        int remainder = length % n;
        int start = 0;
        for (int i = 0; i < n; ++i) {
            int end = start + segmentLength;
            if (remainder > 0) {
                ++end;
                --remainder;
            }
            result.add(new Pair<>(start, end - start));
            start = end;
        }
        return result;
    }
    
    public static List<Pair<Point2D, Point2D>> divideRectangle (int width, int height, int nWidth, int nHeight) {
        List<Pair<Integer, Integer>> heightPairs = divideSegment(height, nHeight);
        List<Pair<Integer, Integer>> widthPairs = divideSegment(width, nWidth);

        List<Pair<Point2D, Point2D>> result = new ArrayList<>();
        for (Pair<Integer, Integer> widthPair : widthPairs)
            for (Pair<Integer, Integer> heightPair : heightPairs)
                result.add(new Pair<>(new Point2D(widthPair.getKey(), heightPair.getKey()), 
                                      new Point2D(widthPair.getValue(), heightPair.getValue())));
        return result;
    }
}
