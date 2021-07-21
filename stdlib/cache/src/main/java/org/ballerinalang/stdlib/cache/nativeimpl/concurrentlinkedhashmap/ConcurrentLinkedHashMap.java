/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.cache.nativeimpl.concurrentlinkedhashmap;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Implementation of the ConcurrentLinked Hash Map.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentLinkedHashMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    /** The maximum weighted capacity of the map. */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /** The maximum number of pending operations per buffer. */
    static final int MAXIMUM_BUFFER_SIZE = 1 << 20;

    /** The number of pending operations per buffer before attempting to drain. */
    static final int BUFFER_THRESHOLD = 16;

    /** The number of buffers to use. */
    static final int NUMBER_OF_BUFFERS;

    /** Mask value for indexing into the buffers. */
    static final int BUFFER_MASK;

    /** The maximum number of operations to perform per amortized drain. */
    static final int AMORTIZED_DRAIN_THRESHOLD;

    static {
        int buffers = ceilingNextPowerOfTwo(Runtime.getRuntime().availableProcessors());
        AMORTIZED_DRAIN_THRESHOLD = (1 + buffers) * BUFFER_THRESHOLD;
        NUMBER_OF_BUFFERS = buffers;
        BUFFER_MASK = buffers - 1;
    }

    static int ceilingNextPowerOfTwo(int x) {
        // From Hacker's Delight, Chapter 3, Harry S. Warren Jr.
        return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
    }

    /** The draining status of the buffers. */
    enum DrainStatus {

        /** A drain is not taking place. */
        IDLE,

        /** A drain is required due to a pending write modification. */
        REQUIRED,

        /** A drain is in progress. */
        PROCESSING
    }

    // The backing data store holding the key-value associations
    final ConcurrentHashMap<K, Node> data;
    int concurrencyLevel = 16;

    // These fields provide support to bound the map by a maximum capacity
    transient LinkedDeque<Node> evictionDeque;

    // must write under lock
    volatile int weightedSize;

    // must write under lock
    volatile int capacity;

    volatile int nextOrder;
    int drainedOrder;

    final transient Lock evictionLock;
    final Queue<Task>[] buffers;
    transient ExecutorService executor = new DisabledExecutorService();;
    final Weigher<? super V> weigher;
    final AtomicIntegerArray bufferLengths;
    final AtomicReference<DrainStatus> drainStatus;

    transient Set<K> keySet;
    transient Set<Entry<K, V>> entrySet;

    /**
     * Creates an instance based on the builder's configuration.
     *
     * @param maximumCapacity The maximum capacity
     */
    @SuppressWarnings({
            "unchecked", "cast"
    })
    public ConcurrentLinkedHashMap(int maximumCapacity) {
        // The data store and its maximum capacity
        capacity = maximumCapacity;
        data = new ConcurrentHashMap<>(
                3,
                0.75f,
                16);

        // The eviction support
        weigher = Weighers.singleton();
        nextOrder = Integer.MIN_VALUE;
        drainedOrder = Integer.MIN_VALUE;
        evictionLock = new ReentrantLock();
        evictionDeque = new LinkedDeque<>();
        drainStatus = new AtomicReference<>(DrainStatus.IDLE);

        buffers = (Queue<Task>[]) new Queue[NUMBER_OF_BUFFERS];
        bufferLengths = new AtomicIntegerArray(NUMBER_OF_BUFFERS);
        for (int i = 0; i < NUMBER_OF_BUFFERS; i++) {
            buffers[i] = new ConcurrentLinkedQueue<>();
        }
    }

    /* ---------------- Eviction Support -------------- */

    /**
     * Sets the maximum weighted capacity of the map and eagerly evicts entries until it
     * shrinks to the appropriate size.
     *
     * @param capacity the maximum weighted capacity of the map
     * @throws IllegalArgumentException if the capacity is negative
     */
    public void setCapacity(int capacity) {
        evictionLock.lock();
        try {
            this.capacity = Math.min(capacity, MAXIMUM_CAPACITY);
            drainBuffers(AMORTIZED_DRAIN_THRESHOLD);
            evict();
        } finally {
            evictionLock.unlock();
        }
    }

    /** Determines whether the map has exceeded its capacity. */
    private boolean hasOverflowed() {
        return weightedSize > capacity;
    }

    /**
     * Evicts entries from the map while it exceeds the capacity and appends evicted
     * entries to the notification queue for processing.
     */
    private void evict() {
        // Attempts to evict entries from the map if it exceeds the maximum
        // capacity. If the eviction fails due to a concurrent removal of the
        // victim, that removal may cancel out the addition that triggered this
        // eviction. The victim is eagerly unlinked before the removal task so
        // that if an eviction is still required then a new victim will be chosen
        // for removal.
        while (hasOverflowed()) {
            Node node = evictionDeque.pollFirst();
            data.remove(node.key, node);
            node.makeDead();
        }
    }

    /**
     * Performs the post-processing work required after the map operation.
     *
     * @param task the pending operation to be applied
     */
    private void afterCompletion(Task task) {
        boolean delayable = schedule(task);
        if (shouldDrainBuffers(delayable)) {
            tryToDrainBuffers(AMORTIZED_DRAIN_THRESHOLD);
        }
    }

    /**
     * Schedules the task to be applied to the page replacement policy.
     *
     * @param task the pending operation
     * @return if the draining of the buffers can be delayed
     */
    private boolean schedule(Task task) {
        int index = bufferIndex();
        int buffered = bufferLengths.incrementAndGet(index);

        if (task.isWrite()) {
            buffers[index].add(task);
            drainStatus.set(DrainStatus.REQUIRED);
            return false;
        }

        // A buffer may discard a read task if its length exceeds a tolerance level
        if (buffered <= MAXIMUM_BUFFER_SIZE) {
            buffers[index].add(task);
            return (buffered <= BUFFER_THRESHOLD);
        } else { // not optimized for fail-safe scenario
            bufferLengths.decrementAndGet(index);
            return false;
        }
    }

    /** Returns the index to the buffer that the task should be scheduled on. */
    private static int bufferIndex() {
        // A buffer is chosen by the thread's id so that tasks are distributed in a
        // pseudo evenly manner. This helps avoid hot entries causing contention due
        // to other threads trying to append to the same buffer.
        return (int) Thread.currentThread().getId() & BUFFER_MASK;
    }

    public int nextOrdering() {
        // The next ordering is acquired in a racy fashion as the increment is not
        // atomic with the insertion into a buffer. This means that concurrent tasks
        // can have the same ordering and the buffers are in a weakly sorted order.
        return nextOrder++;
    }

    /**
     * Determines whether the buffers should be drained.
     *
     * @param delayable if a drain should be delayed until required
     * @return if a drain should be attempted
     */
    private boolean shouldDrainBuffers(boolean delayable) {
        if (executor.isShutdown()) {
            DrainStatus status = drainStatus.get();
            return (status != DrainStatus.PROCESSING)
                    && (!delayable || (status == DrainStatus.REQUIRED));
        }
        return false;
    }

    /**
     * Attempts to acquire the eviction lock and apply the pending operations to the page
     * replacement policy.
     *
     * @param maxToDrain the maximum number of operations to drain
     */
    private void tryToDrainBuffers(int maxToDrain) {
        if (evictionLock.tryLock()) {
            try {
                drainStatus.set(DrainStatus.PROCESSING);
                drainBuffers(maxToDrain);
            } finally {
                drainStatus.compareAndSet(DrainStatus.PROCESSING, DrainStatus.IDLE);
                evictionLock.unlock();
            }
        }
    }

    /**
     * Drains the buffers and applies the pending operations.
     *
     * @param maxToDrain the maximum number of operations to drain
     */
    private void drainBuffers(int maxToDrain) {
        // A mostly strict ordering is achieved by observing that each buffer
        // contains tasks in a weakly sorted order starting from the last drain.
        // The buffers can be merged into a sorted list in O(n) time by using
        // counting sort and chaining on a collision.

        // The output is capped to the expected number of tasks plus additional
        // slack to optimistically handle the concurrent additions to the buffers.
        Task[] tasks = new Task[maxToDrain];

        // Moves the tasks into the output array, applies them, and updates the
        // marker for the starting order of the next drain.
        int maxTaskIndex = moveTasksFromBuffers(tasks);
        runTasks(tasks, maxTaskIndex);
        updateDrainedOrder(tasks, maxTaskIndex);
    }

    /**
     * Moves the tasks from the buffers into the output array.
     *
     * @param tasks the ordered array of the pending operations
     * @return the highest index location of a task that was added to the array
     */
    private int moveTasksFromBuffers(Task[] tasks) {
        int maxTaskIndex = -1;
        for (int i = 0; i < buffers.length; i++) {
            int maxIndex = moveTasksFromBuffer(tasks, i);
            maxTaskIndex = Math.max(maxIndex, maxTaskIndex);
        }
        return maxTaskIndex;
    }

    /**
     * Moves the tasks from the specified buffer into the output array.
     *
     * @param tasks the ordered array of the pending operations
     * @param bufferIndex the buffer to drain into the tasks array
     * @return the highest index location of a task that was added to the array
     */
    private int moveTasksFromBuffer(Task[] tasks, int bufferIndex) {
        // While a buffer is being drained it may be concurrently appended to. The
        // number of tasks removed are tracked so that the length can be decremented
        // by the delta rather than set to zero.
        Queue<Task> buffer = buffers[bufferIndex];
        int removedFromBuffer = 0;

        Task task;
        int maxIndex = -1;
        while ((task = buffer.poll()) != null) {
            removedFromBuffer++;

            // The index into the output array is determined by calculating the offset
            // since the last drain
            int index = task.getOrder() - drainedOrder;
            if (index < 0) {
                // The task was missed by the last drain and can be run immediately
                task.run();
            } else if (index >= tasks.length) {
                // Due to concurrent additions, the order exceeds the capacity of the
                // output array. It is added to the end as overflow and the remaining
                // tasks in the buffer will be handled by the next drain.
                maxIndex = tasks.length - 1;
                addTaskToChain(tasks, task, maxIndex);
                break;
            } else {
                maxIndex = Math.max(index, maxIndex);
                addTaskToChain(tasks, task, index);
            }
        }
        bufferLengths.addAndGet(bufferIndex, -removedFromBuffer);
        return maxIndex;
    }

    /**
     * Adds the task as the head of the chain at the index location.
     *
     * @param tasks the ordered array of the pending operations
     * @param task the pending operation to add
     * @param index the array location
     */
    private void addTaskToChain(Task[] tasks, Task task, int index) {
        task.setNext(tasks[index]);
        tasks[index] = task;
    }

    /**
     * Runs the pending page replacement policy operations.
     *
     * @param tasks the ordered array of the pending operations
     * @param maxTaskIndex the maximum index of the array
     */
    private void runTasks(Task[] tasks, int maxTaskIndex) {
        for (int i = 0; i <= maxTaskIndex; i++) {
            runTasksInChain(tasks[i]);
        }
    }

    /**
     * Runs the pending operations on the linked chain.
     *
     * @param task the first task in the chain of operations
     */
    private void runTasksInChain(Task task) {
        while (task != null) {
            Task current = task;
            task = task.getNext();
            current.setNext(null);
            current.run();
        }
    }

    /**
     * Updates the order to start the next drain from.
     *
     * @param tasks the ordered array of operations
     * @param maxTaskIndex the maximum index of the array
     */
    private void updateDrainedOrder(Task[] tasks, int maxTaskIndex) {
        if (maxTaskIndex >= 0) {
            Task task = tasks[maxTaskIndex];
            drainedOrder = task.getOrder() + 1;
        }
    }

    /** Updates the node's location in the page replacement policy. */
    private class ReadTask extends AbstractTask {

        final Node node;

        ReadTask(Node node) {
            this.node = node;
        }

        public void run() {
            // An entry may scheduled for reordering despite having been previously
            // removed. This can occur when the entry was concurrently read while a
            // writer was removing it. If the entry is no longer linked then it does
            // not need to be processed.
            if (evictionDeque.contains(node)) {
                evictionDeque.moveToBack(node);
            }
        }

        public boolean isWrite() {
            return false;
        }
    }

    /** Adds the node to the page replacement policy. */
    private final class AddTask extends AbstractTask {

        final Node node;
        final int weight;

        AddTask(Node node, int weight) {
            this.weight = weight;
            this.node = node;
        }

        public void run() {
            weightedSize += weight;

            // ignore out-of-order write operations
            if (node.get().isAlive()) {
                evictionDeque.add(node);
            }
        }

        public boolean isWrite() {
            return true;
        }
    }

    /** Removes a node from the page replacement policy. */
    private final class RemovalTask extends AbstractTask {

        final Node node;

        RemovalTask(Node node) {
            this.node = node;
        }

        public void run() {
            // add may not have been processed yet
            evictionDeque.remove(node);
            node.makeDead();
        }

        public boolean isWrite() {
            return true;
        }
    }

    /** Updates the weighted size and evicts an entry on overflow. */
    private final class UpdateTask extends ReadTask {

        final int weightDifference;

        public UpdateTask(Node node, int weightDifference) {
            super(node);
            this.weightDifference = weightDifference;
        }

        @Override
        public void run() {
            super.run();
            weightedSize += weightDifference;
        }

        @Override
        public boolean isWrite() {
            return true;
        }
    }

    /* ---------------- Concurrent Map Support -------------- */

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public void clear() {
        // The alternative is to iterate through the keys and call #remove(), which
        // adds unnecessary contention on the eviction lock and buffers.
        evictionLock.lock();
        try {
            Node node;
            while ((node = evictionDeque.poll()) != null) {
                data.remove(node.key, node);
                node.makeDead();
            }

            // Drain the buffers and run only the write tasks
            for (int i = 0; i < buffers.length; i++) {
                Queue<Task> buffer = buffers[i];
                int removed = 0;
                Task task;
                while ((task = buffer.poll()) != null) {
                    if (task.isWrite()) {
                        task.run();
                    }
                    removed++;
                }
                bufferLengths.addAndGet(i, -removed);
            }
        } finally {
            evictionLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {

        for (Node node : data.values()) {
            if (node.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        final Node node = data.get(key);
        if (node == null) {
            return null;
        }
        afterCompletion(new ReadTask(node));
        return node.getValue();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {

    }

    public V putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public V replace(K key, V value) {
        return null;
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {

    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return null;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    /**
     * Adds a node to the list and the data store. If an existing node is found, then its
     * value is updated if allowed.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the prior value in the data store or null if no mapping was found
     */
    @Override
    public V put(K key, V value) {
        final int weight = weigher.weightOf(value);
        final WeightedValue<V> weightedValue = new WeightedValue<>(value, weight);
        final Node node = new Node(key, weightedValue);

        for (;;) {
            final Node prior = data.putIfAbsent(node.key, node);
            if (prior == null) {
                afterCompletion(new AddTask(node, weight));
                return null;
            }
            for (;;) {
                final WeightedValue<V> oldWeightedValue = prior.get();
                if (!oldWeightedValue.isAlive()) {
                    break;
                }
                if (prior.compareAndSet(oldWeightedValue, weightedValue)) {
                    final int weightedDifference = weight - oldWeightedValue.weight;
                    final Task task = (weightedDifference == 0)
                            ? new ReadTask(prior)
                            : new UpdateTask(prior, weightedDifference);
                    afterCompletion(task);
                    return oldWeightedValue.value;
                }
            }
        }
    }

    @Override
    public V remove(Object key) {
        final Node node = data.remove(key);
        if (node == null) {
            return null;
        }

        node.makeRetired();
        afterCompletion(new RemovalTask(node));
        return node.getValue();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks == null) ? (keySet = new KeySet()) : ks;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> es = entrySet;
        return (es == null) ? (entrySet = new EntrySet()) : es;
    }

    /** A value, its weight, and the entry's status. */
    private static final class WeightedValue<V> {

        private final int weight;
        private final V value;

        WeightedValue(V value, int weight) {
            this.weight = weight;
            this.value = value;
        }

        boolean hasValue(Object o) {
            return (o == value) || value.equals(o);
        }

        /**
         * If the entry is available in the hash-table and page replacement policy.
         */
        boolean isAlive() {
            return weight > 0;
        }
    }

    /**
     * A node contains the key, the weighted value, and the linkage pointers on the
     * page-replacement algorithm's data structures.
     */
    @SuppressWarnings("serial")
    private class Node extends AtomicReference<WeightedValue<V>> implements Linked<Node> {
        private static final long serialVersionUID = 1;
        private K key;

        private Node prev;
        private Node next;
        /** Creates a new, unlinked node. */
        Node(K key, WeightedValue<V> weightedValue) {
            super(weightedValue);
            this.key = key;
        }

        public Node getPrevious() {
            return prev;
        }

        public void setPrevious(Node prev) {
            this.prev = prev;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        /** Retrieves the value held by the current <tt>WeightedValue</tt>. */
        V getValue() {
            return get().value;
        }

        /**
         * Atomically transitions the node from the <tt>alive</tt> state to the
         * <tt>retired</tt> state, if a valid transition.
         */
        public void makeRetired() {
            for (;;) {
                WeightedValue<V> current = get();
                if (!current.isAlive()) {
                    return;
                }
                WeightedValue<V> retired = new WeightedValue<>(
                        current.value,
                        -current.weight);
                if (compareAndSet(current, retired)) {
                    return;
                }
            }
        }

        /**
         * Atomically transitions the node to the <tt>dead</tt> state and decrements the
         * <tt>weightedSize</tt>.
         */
        public void makeDead() {
            for (;;) {
                WeightedValue<V> current = get();
                WeightedValue<V> dead = new WeightedValue<>(current.value, 0);
                if (compareAndSet(current, dead)) {
                    weightedSize -= Math.abs(current.weight);
                    return;
                }
            }
        }
    }

    /** An adapter to safely externalize the keys. */
    private final class KeySet extends AbstractSet<K> {

        final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

        @Override
        public Iterator<K> iterator() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return map.data.keySet().toArray(array);
        }
    }

    /** An adapter to safely externalize the entries. */
    private final class EntrySet extends AbstractSet<Entry<K, V>> {

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    /** An adapter to safely externalize the entry iterator. */
    private final class EntryIterator implements Iterator<Entry<K, V>> {

        private final Iterator<Node> iterator = data.values().iterator();
        Node current;

        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            current = iterator.next();
            return new WriteThroughEntry(current);
        }

        public void remove() {
            if (current == null) {
                throw new IllegalStateException("Node can't be null");
            }
            ConcurrentLinkedHashMap.this.remove(current.key);
            current = null;
        }
    }

    /** An entry that allows updates to write through to the map. */
    private class WriteThroughEntry extends AbstractMap.SimpleEntry<K, V> {

        private static final long serialVersionUID = 1;

        public WriteThroughEntry(Node node) {
            super(node.key, node.getValue());
        }
    }

    /** An executor that is always terminated. */
    private static final class DisabledExecutorService extends AbstractExecutorService {

        @Override
        public void shutdown() {

        }

        @Override
        public List<Runnable> shutdownNow() {
            return null;
        }

        public boolean isShutdown() {
            return true;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void execute(Runnable command) {

        }
    }

    /** An operation that can be lazily applied to the page replacement policy. */
    private interface Task extends Runnable {

        /** The priority order. */
        int getOrder();

        /** If the task represents an add, modify, or remove operation. */
        boolean isWrite();

        /** Returns the next task on the link chain. */
        Task getNext();

        /** Sets the next task on the link chain. */
        void setNext(Task task);
    }

    /** A skeletal implementation of the <tt>Task</tt> interface. */
    private abstract class AbstractTask implements Task {

        private final int order;
        private Task task;

        public AbstractTask() {
            order = nextOrdering();
        }

        public int getOrder() {
            return order;
        }

        public Task getNext() {
            return task;
        }

        public void setNext(Task task) {
            this.task = task;
        }
    }

    /* ---------------- Serialization Support -------------- */

    private static final long serialVersionUID = 1;

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}

