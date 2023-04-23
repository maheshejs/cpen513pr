package proj;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javafx.util.Pair;


/**
 * Represents a bucket array
 */
public class BucketArray {
    private List<DoublyLinkedList<Integer>> buckets;
    private Map<Integer, Pair<Integer, DNode<Integer>>> repository;
    private int highestFilledBucketIndex;

    /**
     * Creates a new bucket array
     */
    public BucketArray (int numBlocks, int numBuckets) {
        buckets = new ArrayList<>(numBuckets);
        for (int bucketIndex = 0; bucketIndex < numBuckets; ++bucketIndex) {
            buckets.add(new DoublyLinkedList<>());
        }

        repository = new HashMap<>(numBlocks);
        for (int blockIndex = 0; blockIndex < numBlocks; ++blockIndex) {
            repository.put(blockIndex, new Pair<>(-1, null));
        }

        highestFilledBucketIndex = -1;
    }

    /**
     * Adds a block to the bucket array
     * @param blockIndex the index of the block
     * @param bucketIndex the index of the bucket
     */
    public void addBlock (int blockIndex, int bucketIndex) {
        DNode<Integer> node = new DNode<Integer>(blockIndex);
        buckets.get(bucketIndex).add(node);
        repository.put(blockIndex, new Pair<>(bucketIndex, node));

        // Update highest filled bucket
        if (bucketIndex > highestFilledBucketIndex)
            highestFilledBucketIndex = bucketIndex;
    }

    /**
     * Removes a block from the bucket array
     * @param blockIndex the index of the block
     */
    public void removeBlock (int blockIndex) {
        DNode<Integer> node = repository.get(blockIndex).getValue();
        int bucketIndex = repository.get(blockIndex).getKey();
        buckets.get(bucketIndex).remove(node);
        repository.put(blockIndex, new Pair<>(-1, null));

        // Update highest filled bucket
        boolean found = false;
        for (bucketIndex = highestFilledBucketIndex; bucketIndex >= 0; --bucketIndex) {
            if (!buckets.get(bucketIndex).isEmpty()) {
                highestFilledBucketIndex = bucketIndex;
                found = true;
                break;
            }
        }
        if (!found)
            highestFilledBucketIndex = -1;
    }
    
    /**
     * Updates a block in the bucket array
     * @param blockIndex the index of the block
     * @param newBucketIndex the new index of the bucket
     */
    private void updateBlock (int blockIndex, int newBucketIndex) {
        removeBlock(blockIndex);
        addBlock(blockIndex, newBucketIndex);
    }

    /**
     * Moves a block up in the bucket array
     * @param blockIndex the index of the block
     */
    public void moveUpBlock (int blockIndex) {
        int bucketIndex = repository.get(blockIndex).getKey();
        updateBlock(blockIndex, bucketIndex + 1);
    }

    /**
     * Moves a block down in the bucket array
     * @param blockIndex the index of the block
     */
    public void moveDownBlock (int blockIndex) {
        int bucketIndex = repository.get(blockIndex).getKey();
        updateBlock(blockIndex, bucketIndex - 1);
    }

    /**
     * Returns the highest filled bucket
     * @return the highest filled bucket index
     */
    public int getHighestFilledBucketIndex () {
        return highestFilledBucketIndex;
    }

    /**
     * Returns the maximum bucket index
     * @return the maximum bucket index
     */
    public int getMaxBucketIndex () {
        return buckets.size() - 1;
    }

    /**
     * Peeks the first element in the bucket
     * @param bucketIndex the index of the bucket
     * @return the first element in the bucket
     */
    public int peekBucket (int bucketIndex) {
        return buckets.get(bucketIndex).peek();
    }

    /**
     * Returns the repository which stores pointers to the bucket elements associated with each block
     * @return the repository
     */
    public Map<Integer, Pair<Integer, DNode<Integer>>> getRepository () {
        return repository;
    }
    
    @Override
    public String toString() {
        int numBuckets = buckets.size();
        String str = "";
        for (int bucketIndex = 0; bucketIndex < numBuckets; ++bucketIndex)
            str += String.format("Bucket %+d: %s\n", bucketIndex, buckets.get(bucketIndex));
        return str;
    }
}