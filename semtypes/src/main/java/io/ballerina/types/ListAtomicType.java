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

import static io.ballerina.types.PredefinedType.CELL_SEMTYPE_INNER;
import static io.ballerina.types.PredefinedType.CELL_SEMTYPE_INNER_RO;

/**
 * ListAtomicType node.
 *
 * @since 2201.8.0
 */
public final class ListAtomicType implements AtomicType {

    public static final ListAtomicType LIST_ATOMIC_INNER = from(FixedLengthArray.empty(), CELL_SEMTYPE_INNER);
    static final ListAtomicType LIST_ATOMIC_RO = from(FixedLengthArray.empty(), CELL_SEMTYPE_INNER_RO);
    public final FixedLengthArray members;
    public final CellSemType rest;

    private ListAtomicType(FixedLengthArray members, CellSemType rest) {
        this.members = members;
        this.rest = rest;
    }

    public static ListAtomicType from(FixedLengthArray members, CellSemType rest) {
        return new ListAtomicType(members, rest);
    }

    @Override
    public String toString() {
        return "ListAtomicType{members=" + members + ", rest=" + rest + '}';
    }
}
