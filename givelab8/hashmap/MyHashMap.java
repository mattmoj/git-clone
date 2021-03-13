package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int size;
    private double loadFactor;
    private HashSet<K> keys;

    private static final int DEFAULT_INIT_SIZE = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        this(DEFAULT_INIT_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        size = 0;
        loadFactor = maxLoad;
        keys = new HashSet<K>();
    }

    /**Returns a new node to be placed in a hash table bucket **/
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    /** Removes all of the mappings from this map. */
    public void clear() {
        buckets = createTable(DEFAULT_INIT_SIZE);
        size = 0;
        keys = new HashSet<>();
    }

    /** Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(K key) {
        return keySet().contains(key);
    }

    public Iterator<K> iterator() {
        return keys.iterator();
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    public V get(K key) {
        Node node = getNode(key);
        if (node == null) {
            return null;
        }
        return node.value;
    }

    /** Returns the number of key-value mappings in this map. */
    public int size() {
        return size;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    public void put(K key, V value) {
        Node n = getNode(key);

        if (n != null) { //updateNode, node already exists
            n.value = value;
        }

        if ((double) size / buckets.length > loadFactor) {
            rebucket(buckets.length * 2);
        }
        int idx = findBucket(key); //insertion case
        Collection<Node> bucketList = buckets[idx];

        //bucket doesnt exist (first time adding to bucket, make it)
        if (bucketList == null) {
            bucketList = createBucket();
            buckets[idx] = bucketList;
        }
        bucketList.add(createNode(key, value));
        size += 1;
        keys.add(key);
    }

    /** Returns a Set view of the keys contained in this map. */
    public Set<K> keySet() {
        return keys;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 8. If you don't implement this,
     * throw an UnsupportedOperationException.
     */
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    private int findBucket(K key, int numBuckets) {
        return Math.floorMod(key.hashCode(), numBuckets);
    }

    private int findBucket(K key) {
        return findBucket(key, buckets.length);
    }

    private Node getNode(K key) {
        int idx = findBucket(key);
        Collection<Node> bucketList = buckets[idx];
        if (bucketList != null) {
            for (Node node : bucketList) {
                if (node.key.equals(key)) {
                    return node;
                }
            }
        }
        return null;
    }

    private void rebucket(int targetSize) {
        Collection<Node>[] newBuckets = createTable(targetSize);
        for (K key : keys) {
            int idx = findBucket(key, newBuckets.length);
            if (newBuckets[idx] == null) {
                newBuckets[idx] = createBucket();
            }
            newBuckets[idx].add(getNode(key)); //keys?
        }
        buckets = newBuckets;
    }

}
