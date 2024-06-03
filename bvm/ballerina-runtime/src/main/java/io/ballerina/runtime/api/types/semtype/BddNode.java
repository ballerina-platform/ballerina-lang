/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.runtime.api.types.semtype;

/**
 * Internal node of a BDD, which represents a disjunction of conjunctions of atoms.
 *
 * @since 2201.10.0
 */
public final class BddNode extends Bdd {

    private final Atom atom;
    private final Bdd left;
    private final Bdd middle;
    private final Bdd right;

    BddNode(Atom atom, Bdd left, Bdd middle, Bdd right) {
        super(false, false);
        this.atom = atom;
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    static BddNode bddAtom(TypeAtom atom) {
        return new BddNode(atom, BddAllOrNothing.ALL, BddAllOrNothing.NOTHING, BddAllOrNothing.NOTHING);
    }

    public Atom atom() {
        return atom;
    }

    public Bdd left() {
        return left;
    }

    public Bdd middle() {
        return middle;
    }

    public Bdd right() {
        return right;
    }

}
