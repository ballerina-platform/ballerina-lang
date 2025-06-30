/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types;

import java.util.PriorityQueue;

/**
 * Represents the Conjunction record type.
 *
 * @since 2201.12.0
 */
public class Conjunction {
    public Atom atom;
    public Conjunction next;

    private Conjunction(Atom atom, Conjunction next) {
        this.atom = atom;
        this.next = next;
    }

    public static Conjunction and(Atom atom, Conjunction next) {
        return new Conjunction(atom, next);
    }

    /**
     * Reorders the conjunction linked list based on atom temperature. Hotter atoms (higher temperature values) will
     * come first.
     *
     * @param cx          the context
     * @param conjunction the conjunction to reorder
     * @return the head of the reordered linked list
     */
    public static Conjunction reorderByTemperature(Context cx, Conjunction conjunction) {
        if (conjunction == null) {
            return null;
        }
        PriorityQueue<Atom> atomQueue = new PriorityQueue<>(
                (a1, a2) -> Integer.compare(a2.temperature(cx), a1.temperature(cx)));

        Conjunction current = conjunction;
        while (current != null) {
            atomQueue.offer(current.atom);
            current = current.next;
        }

        assert !atomQueue.isEmpty();

        Conjunction head = new Conjunction(atomQueue.poll(), null);
        Conjunction tail = head;

        while (!atomQueue.isEmpty()) {
            tail.next = new Conjunction(atomQueue.poll(), null);
            tail = tail.next;
        }

        return head;
    }
}
