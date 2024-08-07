/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
 *
 */

package io.ballerina.runtime.api.types.semtype;

/**
 * Represents the Conjunction in the BDD.
 *
 * @param atom Atom of this node
 * @param next Next node in the conjunction, will be {@code null} if this is the last node
 * @since 2201.10.0
 */
public record Conjunction(Atom atom, Conjunction next) {

    public static Conjunction and(Atom atom, Conjunction next) {
        return new Conjunction(atom, next);
    }
}
