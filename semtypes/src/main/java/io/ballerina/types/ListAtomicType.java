/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
package io.ballerina.types;

/**
 * ListAtomicType node.
 *
 * @param members for a given list type this represents the required members
 * @param rest    for a given list type this represents the rest type. This is NEVER if the list don't have a rest type
 * @since 2201.8.0
 */
public record ListAtomicType(FixedLengthArray members, CellSemType rest) implements AtomicType {

    public ListAtomicType {
        assert members != null;
        assert rest != null;
    }

    public static ListAtomicType from(FixedLengthArray members, CellSemType rest) {
        return new ListAtomicType(members, rest);
    }

    @Override
    public Atom.Kind atomKind() {
        return Atom.Kind.LIST_ATOM;
    }
}
