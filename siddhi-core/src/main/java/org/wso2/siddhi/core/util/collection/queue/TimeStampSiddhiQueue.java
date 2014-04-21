package org.wso2.siddhi.core.util.collection.queue;

import org.wso2.siddhi.core.event.StreamEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;


import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.util.collection.Pair;
import org.wso2.siddhi.core.util.collection.queue.scheduler.timestamp.ISchedulerTimestampSiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.scheduler.timestamp.TimestampQueueIterator;

/**
 * This class implements a sliding window which slides once in every given constant time.
 * @param <T>
 */
public class TimeStampSiddhiQueue<T extends StreamEvent> extends SiddhiQueue<T> implements  ISchedulerTimestampSiddhiQueue<T> {
    private LinkedBlockingQueue<Pair<Long, LinkedBlockingQueue<T>>> pairQueue = new LinkedBlockingQueue<Pair<Long, LinkedBlockingQueue<T>>>();
    private Pair<Long, LinkedBlockingQueue<T>> lastPair;
    private long pollInterval = 0;
    private Long nextPollTime = 0l;

    public TimeStampSiddhiQueue(long pollInterval) {
        this.pollInterval = pollInterval;
        updateNextPollTime(System.currentTimeMillis());
    }

    private void updateNextPollTime(long lastPollTime){
        nextPollTime = (lastPollTime + pollInterval);
    }

    /**
     * Every event that expires before next polling time will be put into a single group.
     * If an event receives which expires after the next polling time, a new group will be
     * created and subsequent messages will be put into that group and update next polling
     * time. In this way separate groups will be created for messages which expires before
     * a particular polling time. And since this is a FIFO queue the group that has to be
     * pop-ed out in the next pol will always at the peek of the queue
     * @param streamEvent
     */
    @Override
    public synchronized void put(T streamEvent) {
       long expiryTime = ((RemoveEvent)streamEvent).getExpiryTime();

        if (!pairQueue.isEmpty() && nextPollTime >= expiryTime) {
            lastPair.getTwo().add(streamEvent);
        } else {
            LinkedBlockingQueue<T> eventsList = new LinkedBlockingQueue<T>();
            eventsList.add(streamEvent);
            Pair<Long, LinkedBlockingQueue<T>> eventPair = new Pair<Long, LinkedBlockingQueue<T>>(nextPollTime, eventsList);
            pairQueue.add(eventPair);
            lastPair = eventPair;

            updateNextPollTime(nextPollTime);
        }
    }

    @Override
    public synchronized T poll() {
        T t = null;
        if (pairQueue.size() > 0) {
            LinkedBlockingQueue<T> streamEvents = pairQueue.peek().getTwo();
            t = streamEvents.poll();
        }
        return t;
    }

    @Override
    public synchronized T peek() {
        T t = null;
        if (pairQueue.size() > 0) {
            t = pairQueue.peek().getTwo().peek();
        }

        return t;
    }

    @Override
    public Iterator<T> iterator() {
        return (Iterator<T>) new TimestampQueueIterator(pairQueue.iterator());
    }

    /**
     * Pops up the the peek of the message group queue. And it contains messages
     * which have an expiry time less than the expiryTime argument. Messages are
     * grouped in put() method in a way such that, all messages that are expiring
     * before a given polling time are put into a single queue. And the group that
     * has to be pop-ed in the next poll is always kept in the peek of the queue
     * @param expiryTime current epoch time in mili seconds
     * @return Messages that are expiring before the expiryTime
     */
    @Override
    public Collection<T> poll(long expiryTime) {
        LinkedBlockingQueue<T> returnValue = null;
        if (!pairQueue.isEmpty()) {
            Pair<Long, LinkedBlockingQueue<T>> pair = pairQueue.peek();
            if (!pair.getTwo().isEmpty() && (pair.getOne() <= expiryTime)) {
                returnValue = pairQueue.poll().getTwo();
            }
        }
        return returnValue;
    }

    @Override
    public Object[] currentState() {
        return new Object[]{pairQueue};
    }

    @Override
    public void restoreState(Object[] objects) {
        pairQueue = (LinkedBlockingQueue<Pair<Long, LinkedBlockingQueue<T>>>) objects[0];
    }

    @Override
    public int size() {
        int size = 0;
        for (Pair<Long, LinkedBlockingQueue<T>> pair : pairQueue) {
            size += pair.getTwo().size();
        }
        return size;
    }

    @Override
    public void reSchedule() {
    }

    @Override
    public void schedule() {
    }

}
