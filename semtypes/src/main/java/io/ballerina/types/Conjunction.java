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

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Represents the Conjunction record type.
 *
 * @since 2201.12.0
 */
public class Conjunction {

    private final Atom atom;
    private Conjunction next;

    private Conjunction(Atom atom, Conjunction next) {
        this.atom = atom;
        this.next(next);
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
    public static Conjunction reorderByTemperature(Context cx, Context.TypeAtomKind kind, Conjunction conjunction) {
        if (conjunction == null) {
            return null;
        }
        ArrayList<Atom> atoms = new ArrayList<>();

        Conjunction current = conjunction;
        while (current != null) {
            atoms.add(current.atom());
            current = current.next();
        }

        atoms.sort(Comparator.comparingInt((Atom atom) -> atom.temperature(cx, kind)).reversed());

        Conjunction head = new Conjunction(atoms.get(0), null);
        Conjunction tail = head;

        for (int i = 1; i < atoms.size(); i++) {
            tail.next(new Conjunction(atoms.get(i), null));
            tail = tail.next();
        }

        return head;
    }

    public Atom atom() {
        return atom;
    }

    public Conjunction next() {
        return next;
    }

    public void next(Conjunction next) {
        this.next = next;
    }
}
