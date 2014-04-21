package org.wso2.siddhi.core.util.collection.queue.scheduler.timestamp;


import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.remove.RemoveStream;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerElement;
import org.wso2.siddhi.core.util.collection.Pair;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerSiddhiQueue;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class SchedulerTimestampSiddhiQueue<T extends StreamEvent> extends SchedulerSiddhiQueue<T> implements ISchedulerTimestampSiddhiQueue<T> {

    private LinkedBlockingQueue<Pair<Long, LinkedBlockingQueue<T>>> pairQueue = new LinkedBlockingQueue<Pair<Long, LinkedBlockingQueue<T>>>();
    private Long lastTimeStamp;
    private Pair<Long, LinkedBlockingQueue<T>> lastPair;

    public SchedulerTimestampSiddhiQueue(SchedulerElement schedulerElement) {
        super(schedulerElement);
    }

    public synchronized void put(T t) {
        process(t);
        if (isScheduledForDispatching.compareAndSet(false, true)) {
            this.schedulerElement.schedule();
        }
    }

    public synchronized T poll() {
        T t = null;
        if (pairQueue.size() > 0) {
            LinkedBlockingQueue<T> streamEvents = pairQueue.peek().getTwo();
            t = streamEvents.poll();
            if (streamEvents.size() == 0) {
                pairQueue.poll();
            }
        }
        if (t == null) {
            isScheduledForDispatching.set(false);
            if (pairQueue.size() > 0) {
                LinkedBlockingQueue<T> streamEvents = pairQueue.peek().getTwo();
                t = streamEvents.poll();
                if (streamEvents.size() == 0) {
                    pairQueue.poll();
                }
            }
            if (t == null) {
                return null;
            } else {
                isScheduledForDispatching.set(true);
                return t;
            }
        }
        return t;
    }

    public synchronized T peek() {
        T t;
        if (pairQueue.size() > 0) {
            t = pairQueue.peek().getTwo().peek();
        } else {
            t = null;
        }

        if (t == null) {
            isScheduledForDispatching.set(false);
            if (pairQueue.size() > 0) {
                t = pairQueue.peek().getTwo().peek();
            }
            if (t == null) {
                return null;
            } else {
                isScheduledForDispatching.set(true);
                return t;
            }
        }
        return t;
    }

    public Iterator<T> iterator() {
        return (Iterator<T>) new TimestampQueueIterator(pairQueue.iterator());
    }


    public synchronized Collection<T> poll(long expiryTime) {
        if (!pairQueue.isEmpty()) {
            Pair<Long, LinkedBlockingQueue<T>> pair = pairQueue.peek();
            if (pair != null && pair.getTwo().peek() != null && ((RemoveStream) pair.getTwo().peek()).getExpiryTime() <= expiryTime) {
                return pairQueue.poll().getTwo();
            } else {
                return null;
            }
        }
        return null;

    }

    private synchronized void process(T streamEvent) {
        long timestamp = streamEvent.getTimeStamp();
        if (pairQueue.size() != 0 && lastTimeStamp != null && lastTimeStamp >= timestamp) {
            lastPair.getTwo().add(streamEvent);
        } else {
            lastTimeStamp = timestamp;
            LinkedBlockingQueue<T> eventsList = new LinkedBlockingQueue<T>();
            eventsList.add(streamEvent);
            Pair<Long, LinkedBlockingQueue<T>> eventPair = new Pair<Long, LinkedBlockingQueue<T>>(timestamp, eventsList);
            pairQueue.add(eventPair);
            lastPair = eventPair;
        }

    }

    public Object[] currentState() {
        return new Object[]{pairQueue};
    }

    public void restoreState(Object[] objects) {
        pairQueue = (LinkedBlockingQueue<Pair<Long, LinkedBlockingQueue<T>>>) objects[0];
    }

    /**
     * This operation is expensive try to avoid
     *
     * @return size of the window
     */
    public int size() {
        int size = 0;
        for (Pair<Long, LinkedBlockingQueue<T>> pair : pairQueue) {
            size += pair.getTwo().size();
        }
        return size;
    }


}
