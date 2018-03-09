/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.siddhi.core.event;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Collection used to group and manage chunk or ComplexEvents.
 *
 * @param <E> sub types of ComplexEvent such as StreamEvent and StateEvent
 */
public class ComplexEventChunk<E extends ComplexEvent> implements Iterator<E>, Serializable {

    private static final long serialVersionUID = 3185987841726255019L;
    protected E first;
    protected E previousToLastReturned;
    protected E lastReturned;
    protected E last;
    protected boolean isBatch = false;

    public ComplexEventChunk(boolean isBatch) {
        this.isBatch = isBatch;
    }

    //Only to maintain backward compatibility
    @Deprecated
    public ComplexEventChunk() {
        this.isBatch = true;
    }

    //Only to maintain backward compatibility
    @Deprecated
    public ComplexEventChunk(E first, E last) {
        this.first = first;
        this.last = last;
        this.isBatch = true;
    }

    public ComplexEventChunk(E first, E last, boolean isBatch) {
        this.first = first;
        this.last = last;
        this.isBatch = isBatch;
    }

    public void insertBeforeCurrent(E events) {

        if (lastReturned == null) {
            throw new IllegalStateException();
        }

        E currentEvent = getLastEvent(events);

        if (previousToLastReturned != null) {
            previousToLastReturned.setNext(events);
        } else {
            first = events;
        }
        previousToLastReturned = currentEvent;

        currentEvent.setNext(lastReturned);
    }


    public void insertAfterCurrent(E streamEvents) {

        if (lastReturned == null) {
            throw new IllegalStateException();
        }

        E currentEvent = getLastEvent(streamEvents);

        ComplexEvent nextEvent = lastReturned.getNext();
        lastReturned.setNext(streamEvents);
        currentEvent.setNext(nextEvent);

    }

    public void add(E complexEvents) {
        if (first == null) {
            first = complexEvents;
        } else {
            last.setNext(complexEvents);
        }
        last = getLastEvent(complexEvents);

    }

    private E getLastEvent(E complexEvents) {
        E lastEvent = complexEvents;
        while (lastEvent != null && lastEvent.getNext() != null) {
            lastEvent = (E) lastEvent.getNext();
        }
        return lastEvent;
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
    public E next() {
        E returnEvent;
        if (lastReturned != null) {
            returnEvent = (E) lastReturned.getNext();
            previousToLastReturned = lastReturned;
        } else if (previousToLastReturned != null) {
            returnEvent = (E) previousToLastReturned.getNext();
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
            first = (E) lastReturned.getNext();
            if (first == null) {
                last = null;
            }
        }
        lastReturned.setNext(null);
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

    public E detachAllBeforeCurrent() {

        if (lastReturned == null) {
            throw new IllegalStateException();
        }

        E firstEvent = null;
        if (previousToLastReturned != null) {
            previousToLastReturned.setNext(null);
            firstEvent = first;
            first = lastReturned;
            previousToLastReturned = null;
        }
        return firstEvent;
    }

    public void clear() {
        previousToLastReturned = null;
        lastReturned = null;
        first = null;
        last = null;
    }

    public void reset() {
        previousToLastReturned = null;
        lastReturned = null;
    }

    public E getFirst() {
        return first;
    }

    public E getLast() {
        return last;
    }

    public E poll() {
        if (first != null) {
            E firstEvent = first;
            first = (E) first.getNext();
            firstEvent.setNext(null);
            return firstEvent;
        } else {
            return null;
        }
    }

    public boolean isBatch() {
        return isBatch;
    }

    public void setBatch(boolean batch) {
        isBatch = batch;
    }

    @Override
    public String toString() {
        return "EventChunk{" +
                "first=" + first +
                '}';
    }
}
