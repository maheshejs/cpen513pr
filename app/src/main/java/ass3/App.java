package ass3;

import java.util.Arrays;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Set;

public class App {
    public static void main(String[] args) {
        // Check command line arguments
        if (args.length != 1) {
            System.err.println("Usage: args=\"<benchmarkFile>\"");
            System.exit(1);
        }

        // Set benchmark
        Benchmark benchmark = new Benchmark(args[0]);
        Block[] blocks = benchmark.getBlocks();
        int numBlocks = benchmark.getNumBlocks();
        
        int[] solution = new int[numBlocks];
        Deque<INode> queue = new ArrayDeque<>();
        queue.push(new INode());
        int upperBoundPartitionCost = Integer.MAX_VALUE;
        int upperBoundPartitionSize = (numBlocks + 1) / 2;
        while (!queue.isEmpty()) {
            INode node = queue.pop();

            if (node.getBlockIndex() == numBlocks - 1) {
                if (node.getCost() < upperBoundPartitionCost) {
                    upperBoundPartitionCost = node.getCost();

                    // Backtrack to construct solution
                    int blockIndex = numBlocks - 1;
                    INode backNode = node;
                    while (backNode.getParent() != null) {
                        if (backNode.getParent().getNumLeftChildren() + 1 == backNode.getNumLeftChildren())
                            solution[blockIndex] = 0;
                        else
                            solution[blockIndex] = 1;
                        backNode = backNode.getParent();
                        --blockIndex;
                    }
                }
            }
            else {
                Set<Integer> connectionIndexes = blocks[node.getBlockIndex() + 1].getConnectionIndexes();
                INode leftChildNode  = new INode(node, true,  connectionIndexes);
                INode rightChildNode = new INode(node, false, connectionIndexes);
                List<INode> childNodes = List.of(leftChildNode, rightChildNode);
                for (INode childNode : childNodes) {
                    boolean isBalanced  = childNode.getNumLeftChildren() <= upperBoundPartitionSize &&
                                            childNode.getNumRightChildren() <= upperBoundPartitionSize;
                    boolean isPromising = childNode.getCost() < upperBoundPartitionCost;
                    if (isBalanced && isPromising) {
                        queue.push(childNode);
                    }
                }
            }
        }

        // Print solution
        System.out.println("Solution");
        System.out.println(Arrays.toString (Arrays.stream(solution).mapToObj(e -> e == 0 ? "L" : "R").toArray()));
        System.out.printf("R = %d, L = %d\n", Arrays.stream(solution).filter(e -> e == 1).count(), 
                                              Arrays.stream(solution).filter(e -> e == 0).count());
        System.out.println(upperBoundPartitionCost);
    }
}