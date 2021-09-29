/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.types.subtypedata;

import io.ballerina.types.Atom;
import io.ballerina.types.Bdd;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bdd node.
 *
 * @since 3.0.0
 */
public class BddNode implements Bdd {
    public final Atom atom;
    public final Bdd left;
    public final Bdd middle;
    public final Bdd right;

    private static final AtomicInteger bddCount = new AtomicInteger();

    private BddNode(Atom atom, Bdd left, Bdd middle, Bdd right) {
        this.atom = atom;
        this.left = left;
        this.middle = middle;
        this.right = right;
        bddCount.incrementAndGet();
    }

    public static BddNode create(Atom atom, Bdd left, Bdd middle, Bdd right) {
        return new BddNode(atom, left, middle, right);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof BddNode) {
            BddNode that = (BddNode) obj;
            return Objects.equals(this.atom, that.atom)
                    && Objects.equals(this.left, that.left)
                    && Objects.equals(this.middle, that.middle)
                    && Objects.equals(this.right, that.right);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = atom.hashCode();
        result = 31 * result + left.hashCode();
        result = 31 * result + middle.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

    public int bddGetCount() {
        return bddCount.get();
    }
}
