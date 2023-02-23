package ass3;

import java.util.Set;
import java.util.HashSet;

public class INode { 
    private int numLeftChildren;
    private int numRightChildren;
    private int blockIndex;
    private INode parent;
    private Set<Integer> leftConnectionIndexes;
    private Set<Integer> rightConnectionIndexes;

    INode() {
        this.parent = null;

        numLeftChildren  = 0;
        numRightChildren = 0;
        blockIndex = -1;
        leftConnectionIndexes  = new HashSet<>();
        rightConnectionIndexes = new HashSet<>();
    }

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

    INode getParent(){
        return parent;
    }

    int getNumLeftChildren() {
        return numLeftChildren;
    }

    int getNumRightChildren() {
        return numRightChildren;
    }

    int getBlockIndex() {
        return blockIndex;
    }

    int getCost() {
        return Math.toIntExact(leftConnectionIndexes.stream().filter(rightConnectionIndexes::contains).count());
    }
}