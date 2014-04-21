package org.wso2.siddhi.core.util.collection.queue.scheduler.timestamp;


import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.util.collection.Pair;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

public class TimestampQueueIterator<T extends StreamEvent> implements Iterator<StreamEvent> {
    private Iterator<Pair<Long, LinkedBlockingQueue<T>>> pairIterator;
    private Iterator<T> eventListIterator;

    public TimestampQueueIterator(Iterator<Pair<Long, LinkedBlockingQueue<T>>> pairIterator) {
        this.pairIterator = pairIterator;
        if (pairIterator.hasNext()) {
            Pair<Long, LinkedBlockingQueue<T>> pair = pairIterator.next();
            eventListIterator = pair.getTwo().iterator();
        }
    }

    @Override
    public boolean hasNext() {
        if (eventListIterator == null) {
            return false;
        }

        if (eventListIterator.hasNext()) {
            return true;
        }

        return pairIterator.hasNext();
    }

    @Override
    public T next() {
        if (eventListIterator != null) {
            T t = eventListIterator.next();

            if (!eventListIterator.hasNext()) {
                eventListIterator = null;

                if (pairIterator.hasNext()) {
                    Pair<Long, LinkedBlockingQueue<T>> pair = pairIterator.next();
                    eventListIterator = pair.getTwo().iterator();
                }

            }
            return t;
        } else {
            throw new NoSuchElementException();
        }

    }

    @Override
    public void remove() {
        
    }
}
