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

/**
 *  This class provides a doubly-linked list that is optimized for the virtual
 *  machine. The first and last elements are manipulated instead of a slightly
 *  more convenient sentinel element to avoid the insertion of null checks with
 *  NullPointerException throws in the byte code. The links to a removed
 *  element are cleared to help a generational garbage collector if the
 *  discarded elements inhabit more than one generation.
 *
 * @param <E> the type of elements held in this collection
 */
public class LinkedDeque<E extends Linked<E>> {

    /**
     * Pointer to first node. Invariant: (first == null && last == null) || (first.prev ==
     * null)
     */
    E first = null;

    /**
     * Pointer to last node. Invariant: (first == null && last == null) || (last.next ==
     * null)
     */
    E last = null;

    public  LinkedDeque(){}

    /**
     * Links the element to the back of the deque so that it becomes the last element.
     *
     * @param e the unlinked element
     */
    private void linkLast(final E e) {
        final E l = last;
        last = e;

        if (l == null) {
            first = e;
        } else {
            l.setNext(e);
            e.setPrevious(l);
        }
    }

    /** Unlinks the non-null first element. */
    E unlinkFirst() {
        final E f = first;
        final E next = f.getNext();
        f.setNext(null);

        first = next;
        if (next == null) {
            last = null;
        } else {
            next.setPrevious(null);
        }
        return f;
    }

    /** Unlinks the non-null element. */
    private void unlink(E e) {
        final E prev = e.getPrevious();
        final E next = e.getNext();

        if (prev == null) {
            first = next;
        } else {
            prev.setNext(next);
            e.setPrevious(null);
        }

        if (next == null) {
            last = prev;
        } else {
            next.setPrevious(prev);
            e.setNext(null);
        }
    }

    public boolean isEmpty() {
        return (first == null);
    }

    public boolean contains(Object o) {
        return (o instanceof Linked<?>) && contains((Linked<?>) o);
    }

    // A fast-path containment check
    boolean contains(Linked<?> e) {
        return (e.getPrevious() != null) || (e.getNext() != null) || (e == first);
    }

    /**
     * Moves the element to the back of the deque so that it becomes the last element.
     *
     * @param e the linked element
     */
    public void moveToBack(E e) {
        if (e != last) {
            unlink(e);
            linkLast(e);
        }
    }

    public boolean offerLast(E e) {
        if (contains(e)) {
            return false;
        }
        linkLast(e);
        return true;
    }

    public boolean add(E e) {
        return offerLast(e);
    }

    public E poll() {
        return pollFirst();
    }

    public E pollFirst() {
        if (isEmpty()) {
            return null;
        }
        return unlinkFirst();
    }

    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (contains(o)) {
            unlink((E) o);
            return true;
        }
        return false;
    }
}
