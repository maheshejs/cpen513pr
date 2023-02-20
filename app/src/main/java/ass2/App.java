package ass2;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.geometry.Point2D;

public class App {
    private static final Random random = new Random(); 
    public static void main (String[] args) {
        // Check command line arguments
        if (args.length != 2) {
            System.out.println("Usage: args=\"<benchmarkFile> <useRowSpacing>\"");
            System.exit(1);
        }

        // Set benchmark
        Benchmark    benchmark   = new Benchmark(args[0], Boolean.parseBoolean(args[1]));
        Block[]      blocks      = benchmark.getBlocks();
        Connection[] connections = benchmark.getConnections();
        Point2D[]    locs        = benchmark.getLocs();
        
        // Set tabu search and simulated annealing parameters
        final int    TABU_MAX_FAILURES      = 100;
        final int    TABU_NEIGHBORHOOD_SIZE = 100;
        final int    TABU_LIST_SIZE         = 100;
        final int    ANNEALING_MAX_FAILURES          = 1000;
        final int    ANNEALING_MOVES_PER_TEMPERATURE = (int) (10 * Math.pow(blocks.length, 4.0/3.0));
        final double ANNEALING_COOLING_RATE          = 0.95;
        final double ANNEALING_INITIAL_TEMPERATURE   = 8.0;
        
        // Initialize tabu list, annealing temperature and failure counter
        Queue<List<Integer>> tabus = new ArrayDeque<>(TABU_LIST_SIZE+1);
        double temperature = ANNEALING_INITIAL_TEMPERATURE;
        int fails = 0;

        // Initialize solution
        int[] solution = new int[locs.length];
        double cost = initializeBlockPlacements(solution, blocks, connections, locs, random);
        int[] bestSolution = solution;
        double bestCost = cost;
        
        int iteration = 0;
        boolean isAnnealing = false;
        boolean isDone = false;
        System.out.println("TABU SEARCH STARTED");
        while (!isDone) {
            boolean hasImproved = false;
            // TABU SEARCH
            if (!isAnnealing) {
                System.out.printf("TABU | Cost = %.1f | Iteration = %d\n", cost, iteration);
                // Generate neighborhood
                Comparator<Neighbor> comp = Comparator.comparing(Neighbor::getMoveCost);
                Queue<Neighbor> neighborhood = new PriorityQueue<>(comp);
                for (int n = 0; n < TABU_NEIGHBORHOOD_SIZE; ++n) 
                    neighborhood.add(new Neighbor(solution, blocks, connections, locs, random));

                // Select best neighbor that improves cost or is not tabu
                Neighbor neighbor = null;
                boolean hasSelected = false;
                boolean isTabu = false;
                while (!hasSelected && !neighborhood.isEmpty()) {
                    neighbor = neighborhood.peek();
                    List<Integer> likelyTabu = List.of(solution[neighbor.getFirstLocIndex()],
                                                       solution[neighbor.getSecondLocIndex()],
                                                       neighbor.getFirstLocIndex(),
                                                       neighbor.getSecondLocIndex());
                    hasImproved = cost + neighbor.getMoveCost() < bestCost;
                    isTabu = checkTabu(likelyTabu, tabus);
                    if (hasImproved || !isTabu)
                        hasSelected = true;
                    else
                        neighborhood.remove();
                }

                if (hasSelected) {
                    ++iteration;
                    swapBlockPlacements(solution, blocks, connections, locs, neighbor.getFirstLocIndex(), neighbor.getSecondLocIndex());
                    cost += neighbor.getMoveCost();
                    // Checkpoint solution if improved
                    if (cost < bestCost) {
                        bestCost = cost;
                        bestSolution = solution;
                        //System.out.println("TABU : " +  bestCost);
                    }
                    
                    List<Integer> tabu = List.of(solution[neighbor.getSecondLocIndex()],
                                                 solution[neighbor.getFirstLocIndex()],
                                                 neighbor.getFirstLocIndex(),
                                                 neighbor.getSecondLocIndex());
                    
                    // Update tabu list
                    if (!isTabu) {
                        tabus.add(tabu);
                        if (tabus.size() > TABU_LIST_SIZE)
                            tabus.remove();
                    }

                    // Check if done
                    if (hasImproved)
                        fails = 0;
                    else if (++fails > TABU_MAX_FAILURES) {
                        isAnnealing = true;
                        fails = 0;
                        iteration = 0;
                        System.out.println("SIMULATED ANNEALING STARTED");
                    }
                }
            }
            // SIMULATED ANNEALING
            else {
                int s = 0;
                for (int m = 0; m < ANNEALING_MOVES_PER_TEMPERATURE; ++m) {
                    System.out.printf("Cost = %.1f | Iteration = %d | Temperature = %.6f\n", cost, iteration, temperature);
                    ++iteration;
                    // Generate random neighbor
                    Neighbor neighbor = new Neighbor(solution, blocks, connections, locs, random);

                    if (random.nextDouble() < Math.exp(- neighbor.getMoveCost() / temperature)) {
                        ++s;
                        swapBlockPlacements(solution, blocks, connections, locs, neighbor.getFirstLocIndex(), neighbor.getSecondLocIndex());
                        cost += neighbor.getMoveCost();
                        // Checkpoint solution if improved
                        if (cost < bestCost) {
                            hasImproved = true;
                            bestCost = cost;
                            bestSolution = solution;
                            //System.out.println("ANNEALING : " + bestCost + " | " + temperature + " | " + iteration);
                        }
                    }
                }
                // Update temperature
                temperature *= ANNEALING_COOLING_RATE;

                // Check if done
                if (hasImproved)
                    fails = 0;
                else if (++fails > ANNEALING_MAX_FAILURES)
                    isDone = true;
            }
        }
        // Print final solution and cost
        System.out.println(Arrays.toString(bestSolution));
        System.out.println(bestCost);
    }

    /**
     * Checks if a move is tabu.
     * @param likelyTabu The move to check.
     * @param tabus the tabu list.
     * @return true if the move is tabu, false otherwise.
     */
    public static boolean checkTabu (List<Integer> likelyTabu, Queue<List<Integer>> tabus) {
        /*
        for (List<Integer> tabu : tabus) {
            if (tabu.get(0) == likelyTabu.get(0) && tabu.get(1) == likelyTabu.get(1)) {
                return true;
            }
            if (tabu.get(0) == likelyTabu.get(1) && tabu.get(1) == likelyTabu.get(0)) {
                return true;
            }
        }
        */
        for (List<Integer> tabu : tabus) {
            if (tabu.get(0) == likelyTabu.get(0) && tabu.get(2) == likelyTabu.get(2)) {
                return true;
            }
            if (tabu.get(0) == likelyTabu.get(1) && tabu.get(2) == likelyTabu.get(3)) {
                return true;
            }
            if (tabu.get(1) == likelyTabu.get(0) && tabu.get(3) == likelyTabu.get(2)) {
                return true;
            }
            if (tabu.get(1) == likelyTabu.get(1) && tabu.get(3) == likelyTabu.get(3)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Swap block placements at given locations.
     * @param solution the solution array.
     * @param firstLocIndex the index of the first location.
     * @param secondLocIndex the index of the second location.
     */
    public static void swapBlockPlacements (int[] solution, Block[] blocks, Connection[] connections, Point2D[] locs, int firstLocIndex, int secondLocIndex) {
        int firstBlockIndex  = solution[firstLocIndex];
        int secondBlockIndex = solution[secondLocIndex];
        Point2D firstLoc  = locs[firstLocIndex];
        Point2D secondLoc = locs[secondLocIndex];
        
        if (firstBlockIndex < blocks.length) {
            for (int connectionIndex : blocks[firstBlockIndex].getConnectionIndexes()) {
                connections[connectionIndex].moveBlockPlacement(firstBlockIndex, firstLoc, secondLoc);
            }
        }
        if (secondBlockIndex < blocks.length) {
            for (int connectionIndex : blocks[secondBlockIndex].getConnectionIndexes()) {
                connections[connectionIndex].moveBlockPlacement(secondBlockIndex, secondLoc, firstLoc);
            }
        }

        swap(solution, firstLocIndex, secondLocIndex);
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
     * Initialize block placements
     * @param solution the solution array
     * @param blocks blocks
     * @param connections connections
     * @param locs locations
     * @param random the random number generator
     * @return the cost of the solution
     */
    public static double initializeBlockPlacements (int[] solution, Block[] blocks, Connection[] connections, Point2D[] locs, Random random) {
        List<Integer> blockIndexes = IntStream.range(0, locs.length).boxed().collect(Collectors.toList());
        // Shuffle block indexes or not if random is null for testing (deterministic behavior)
        if (random != null)
            Collections.shuffle(blockIndexes, random);
        IntStream.range(0, locs.length).forEach(e -> solution[e] = blockIndexes.get(e));

        Point2D defaultLoc = new Point2D(0, 0);
        for (int locIndex = 0; locIndex < locs.length; ++locIndex) {
            Point2D loc = locs[locIndex];
            int blockIndex = solution[locIndex];
            if (blockIndex < blocks.length)
                for (int connectionIndex : blocks[blockIndex].getConnectionIndexes())
                    connections[connectionIndex].moveBlockPlacement(blockIndex, defaultLoc, loc);
        }

        return Arrays.stream(connections).mapToDouble(e -> e.getCost()).sum();
    }
}