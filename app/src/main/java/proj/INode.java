package proj;

import java.util.Set;
import java.util.HashSet;


/**
 * Represents an intelligent node in a tree for branch and bound partitioning
 */
public class INode { 
    private int numLeftChildren;
    private int numRightChildren;
    private int blockIndex;
    private INode parent;
    private Set<Integer> leftConnectionIndexes;
    private Set<Integer> rightConnectionIndexes;

    /**
     * Creates a new INode
     */
    INode() {
        this.parent = null;

        numLeftChildren  = 0;
        numRightChildren = 0;
        blockIndex = -1;
        leftConnectionIndexes  = new HashSet<>();
        rightConnectionIndexes = new HashSet<>();
    }

    /**
     * Creates a new INode
     * @param parent the parent of the node
     * @param isLeftChild whether the node is a left child
     * @param connectionIndexes the connections of the node
     */
    INode(INode parent, boolean isLeftChild, Set<Integer> connectionIndexes) {
        this.parent = parent;

        blockIndex = parent.blockIndex + 1;
        numLeftChildren  = parent.numLeftChildren;
        numRightChildren = parent.numRightChildren;

        leftConnectionIndexes  = new HashSet<>(parent.leftConnectionIndexes);
        rightConnectionIndexes = new HashSet<>(parent.rightConnectionIndexes);

        if (isLeftChild) {
            ++numLeftChildren;
            leftConnectionIndexes.addAll(connectionIndexes);
        }
        else {
            ++numRightChildren;
            rightConnectionIndexes.addAll(connectionIndexes);
        }
    }

    /**
     * Returns the parent of the node
     * @return the parent of the node
     */
    INode getParent() {
        return parent;
    }

    /**
     * Returns the number of left children so far in the tree
     * @return the number of left children

     */
    int getNumLeftChildren() {
        return numLeftChildren;
    }

    /**
     * Returns the number of right children nodes so far in the tree
     * @return the number of right children nodes
     */
    int getNumRightChildren() {
        return numRightChildren;
    }

    /**
     * Returns the associated block of the node
     * @return the block index
     */
    int getBlockIndex() {
        return blockIndex;
    }

    /**
     * Returns the cost of the node
     * @return the cost
     */
    int getCost() {
        return Math.toIntExact(leftConnectionIndexes.stream()
                                                    .filter(rightConnectionIndexes::contains)
                                                    .count());
    }
}