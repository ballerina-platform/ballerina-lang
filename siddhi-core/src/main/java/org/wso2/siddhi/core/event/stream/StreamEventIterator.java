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

package org.wso2.siddhi.core.event.stream;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class StreamEventIterator implements Iterator<StreamEvent> {
    StreamEvent previousToLastReturned;
    StreamEvent lastReturned;
    StreamEvent first;

    public void assignEvent(StreamEvent streamEvent) {
        first = streamEvent;
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
        lastReturned = null;
    }

    public StreamEvent getFirst() {
        return first;
    }

    public void clear() {
        previousToLastReturned = null;
        lastReturned = null;
        first = null;
    }

    public void detach() {
        if (lastReturned == null) {
            throw new IllegalStateException();
        }
        if (previousToLastReturned != null) {
            previousToLastReturned.setNext(null);
        }
        lastReturned = null;
    }
}
