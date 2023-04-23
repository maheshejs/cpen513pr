package proj;

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
}
