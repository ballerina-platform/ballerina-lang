/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types.subtypedata;

import io.ballerina.types.Atom;
import io.ballerina.types.Bdd;

/**
 * Represent a Bdd node that contains a single atom as positive. This is used to reduce the memory overhead of
 * BddNodeImpl in representing such nodes
 *
 * @param atom Atom this node represents
 * @since 2201.10.0
 */
record BddNodeSimple(Atom atom) implements BddNode {

    @Override
    public Bdd left() {
        return BddAllOrNothing.bddAll();
    }

    @Override
    public Bdd middle() {
        return BddAllOrNothing.bddNothing();
    }

    @Override
    public Bdd right() {
        return BddAllOrNothing.bddNothing();
    }
}
