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
 * Actual implementation of a generic Bdd node.
 *
 * @param atom   the atom that this node represents
 * @param left   path that include this node's atom positively
 * @param middle path that doesn't include this node's atom
 * @param right  path that include this node's atom negatively
 * @since 2201.10.0
 */
public record BddNodeImpl(Atom atom, Bdd left, Bdd middle, Bdd right) implements BddNode {

}
