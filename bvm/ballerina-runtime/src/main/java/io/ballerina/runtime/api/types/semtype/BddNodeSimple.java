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
 * Represent a Bdd node that contains a single atom as positive. This is used to reduce the memory overhead of
 * BddNodeImpl in representing such nodes
 *
 * @since 2201.12.0
 */
final class BddNodeSimple extends BddNode {

    private final Atom atom;

    BddNodeSimple(Atom atom) {
        super(false, false);
        this.atom = atom;
    }

    @Override
    public Atom atom() {
        return atom;
    }

    @Override
    public Bdd left() {
        return BddAllOrNothing.ALL;
    }

    @Override
    public Bdd middle() {
        return BddAllOrNothing.NOTHING;
    }

    @Override
    public Bdd right() {
        return BddAllOrNothing.NOTHING;
    }
}
