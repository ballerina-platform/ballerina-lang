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

package io.ballerina.runtime.api.types.semtype;

/**
 * Internal node of a BDD, which represents a disjunction of conjunctions of atoms.
 *
 * @since 2201.12.0
 */
public abstract sealed class BddNode extends Bdd permits BddNodeImpl, BddNodeSimple {

    private volatile Integer hashCode = null;

    protected BddNode(boolean all, boolean nothing) {
        super(all, nothing);
    }

    public static BddNode bddAtom(Atom atom) {
        return new BddNodeSimple(atom);
    }

    public boolean isSimple() {
        return this instanceof BddNodeSimple;
    }

    public abstract Atom atom();

    public abstract Bdd left();

    public abstract Bdd middle();

    public abstract Bdd right();

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BddNode other)) {
            return false;
        }
        return atom().equals(other.atom()) && left().equals(other.left()) && middle().equals(other.middle()) &&
                right().equals(other.right());
    }

    @Override
    public final int hashCode() {
        Integer result = hashCode;
        if (result == null) {
            // No need to synchronize this since {@code computeHashCode} is idempotent
            hashCode = result = computeHashCode();
        }
        return result;
    }

    private int computeHashCode() {
        int result = atom().hashCode();
        result = 31 * result + left().hashCode();
        result = 31 * result + middle().hashCode();
        result = 31 * result + right().hashCode();
        return result;
    }

    @Override
    public boolean posMaybeEmpty() {
        return middle().posMaybeEmpty() || right().posMaybeEmpty();
    }
}
