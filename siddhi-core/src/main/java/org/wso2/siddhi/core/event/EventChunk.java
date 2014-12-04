/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.event;

import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.EventConverter;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverterFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EventChunk implements Iterator<StreamEvent> {

    private StreamEvent first;
    //    private StreamEvent current;
    private StreamEvent previousToLastReturned;
    private StreamEvent lastReturned;
    private StreamEvent last;

    private EventConverter eventConverter;
    private StreamEventPool streamEventPool;

    public EventChunk(MetaStreamEvent metaStreamEvent) {
        streamEventPool = new StreamEventPool(metaStreamEvent, 5);
        eventConverter = StreamEventConverterFactory.constructEventConvertor(metaStreamEvent);
    }

    public EventChunk(int beforeWindowDataSize, int onAfterWindowDataSize, int outputDataSize, EventConverter eventConverter) {
        this.eventConverter = eventConverter;
        streamEventPool = new StreamEventPool(beforeWindowDataSize, onAfterWindowDataSize, outputDataSize, 5);
    }

    public EventChunk(StreamEventPool eventPool, EventConverter eventConverter) {
        streamEventPool = eventPool;
        this.eventConverter = eventConverter;
    }

    public void assign(Event event) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        eventConverter.convertEvent(event, borrowedEvent);
        first = borrowedEvent;
        last = first;
    }

    public void assign(long timeStamp, Object[] data) {
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        eventConverter.convertData(timeStamp, data, borrowedEvent);
        first = borrowedEvent;
        last = first;
    }

    public void assign(StreamEvent streamEvents) {
        first = streamEventPool.borrowEvent();
        last = convertAllStreamEvents(streamEvents, first);
    }

    public void assign(Event[] events) {
        StreamEvent firstEvent = streamEventPool.borrowEvent();
        eventConverter.convertEvent(events[0], firstEvent);
        StreamEvent currentEvent = firstEvent;
        for (int i = 1, eventsLength = events.length; i < eventsLength; i++) {
            StreamEvent nextEvent = streamEventPool.borrowEvent();
            eventConverter.convertEvent(events[i], nextEvent);
            currentEvent.setNext(nextEvent);
            currentEvent = nextEvent;
        }
        first = firstEvent;
        last = currentEvent;
    }

    public void insertBeforeCurrent(Event event) {

        if (lastReturned == null) {
            throw new IllegalStateException();
        }
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        eventConverter.convertEvent(event, borrowedEvent);

        if (previousToLastReturned != null) {
            previousToLastReturned.setNext(borrowedEvent);
        } else {
            first = borrowedEvent;
        }

        borrowedEvent.setNext(lastReturned);
    }

    public void insertAfterCurrent(Event event) {

        if (lastReturned == null) {
            throw new IllegalStateException();
        }
        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        eventConverter.convertEvent(event, borrowedEvent);

        StreamEvent nextEvent = lastReturned.getNext();
        lastReturned.setNext(borrowedEvent);
        borrowedEvent.setNext(nextEvent);

    }

    public void insertBeforeCurrent(StreamEvent streamEvents) {

        if (lastReturned == null) {
            throw new IllegalStateException();
        }

        StreamEvent firstEvent = streamEventPool.borrowEvent();
        StreamEvent currentEvent = convertAllStreamEvents(streamEvents, firstEvent);

        if (previousToLastReturned != null) {
            previousToLastReturned.setNext(firstEvent);
        } else {
            first = firstEvent;
        }

        currentEvent.setNext(lastReturned);
    }

    public void insertAfterCurrent(StreamEvent streamEvents) {

        if (lastReturned == null) {
            throw new IllegalStateException();
        }

        StreamEvent firstEvent = streamEventPool.borrowEvent();
        StreamEvent currentEvent = convertAllStreamEvents(streamEvents, firstEvent);

        StreamEvent nextEvent = lastReturned.getNext();
        lastReturned.setNext(firstEvent);
        currentEvent.setNext(nextEvent);

    }

    public void add(StreamEvent streamEvents) {

        StreamEvent firstEvent = streamEventPool.borrowEvent();
        StreamEvent currentEvent = convertAllStreamEvents(streamEvents, firstEvent);

        last.setNext(firstEvent);
        last = currentEvent;

    }

    public void add(Event event) {

        StreamEvent borrowedEvent = streamEventPool.borrowEvent();
        eventConverter.convertEvent(event, borrowedEvent);

        last.setNext(borrowedEvent);
        last = borrowedEvent;

    }

    private StreamEvent convertAllStreamEvents(StreamEvent streamEvents, StreamEvent firstEvent) {
        eventConverter.convertStreamEvent(streamEvents, firstEvent);
        StreamEvent currentEvent = firstEvent;
        streamEvents = streamEvents.getNext();
        while (streamEvents != null) {
            StreamEvent nextEvent = streamEventPool.borrowEvent();
            eventConverter.convertStreamEvent(streamEvents, nextEvent);
            currentEvent.setNext(nextEvent);
            currentEvent = nextEvent;
            streamEvents = streamEvents.getNext();
        }
        return currentEvent;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        if (lastReturned != null) {
            return lastReturned.getNext() != null;
        } else if (previousToLastReturned != null) {
            return previousToLastReturned.getNext() != null;
        } else {
            return first != null;
        }
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException iteration has no more elements.
     */
    public StreamEvent next() {
        StreamEvent returnEvent;
        if (lastReturned != null) {
            returnEvent = lastReturned.getNext();
            previousToLastReturned = lastReturned;
        } else if (previousToLastReturned != null) {
            returnEvent = previousToLastReturned.getNext();
        } else {
            returnEvent = first;
        }
        if (returnEvent == null) {
            throw new NoSuchElementException();
        }
        lastReturned = returnEvent;
        return returnEvent;
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).  This method can be called only once per
     * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
     * the underlying collection is modified while the iteration is in
     * progress in any way other than by calling this method.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *                                       operation is not supported by this Iterator.
     * @throws IllegalStateException         if the <tt>next</tt> method has not
     *                                       yet been called, or the <tt>remove</tt> method has already
     *                                       been called after the last call to the <tt>next</tt>
     *                                       method.
     */
    public void remove() {
        if (lastReturned == null) {
            throw new IllegalStateException();
        }
        if (previousToLastReturned != null) {
            previousToLastReturned.setNext(lastReturned.getNext());
        } else {
            first = lastReturned.getNext();
        }
        lastReturned.setNext(null);
        streamEventPool.returnEvents(lastReturned);
        lastReturned = null;
    }

    public void detach() {
        if (lastReturned == null) {
            throw new IllegalStateException();
        }
        if (previousToLastReturned != null) {
            previousToLastReturned.setNext(null);
        } else {
            clear();
        }
        lastReturned = null;
    }

    public StreamEvent detachAllBeforeCurrent() {

        if (lastReturned == null) {
            throw new IllegalStateException();
        }

        StreamEvent firstEvent = null;
        if (previousToLastReturned != null) {
            previousToLastReturned.setNext(null);
            firstEvent = first;
            first = lastReturned;
            previousToLastReturned = null;
        }
        return firstEvent;
    }

    public void clear() {
        streamEventPool.returnEvents(first);
        previousToLastReturned = null;
        lastReturned = null;
        first = null;
    }

    public StreamEvent getFirst() {
        return first;
    }

    public StreamEvent getLast() {
        return last;
    }

}
