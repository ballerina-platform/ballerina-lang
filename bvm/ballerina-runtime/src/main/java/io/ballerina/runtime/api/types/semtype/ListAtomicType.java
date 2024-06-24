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

import io.ballerina.runtime.internal.types.semtype.FixedLengthArray;

import static io.ballerina.runtime.api.types.semtype.Builder.CELL_SEMTYPE_INNER;
import static io.ballerina.runtime.api.types.semtype.Builder.CELL_SEMTYPE_INNER_RO;

// TODO: move this to internal along with cell atomic type
public record ListAtomicType(FixedLengthArray members, SemType rest) implements AtomicType {

    public static final ListAtomicType LIST_ATOMIC_INNER = new ListAtomicType(
            FixedLengthArray.empty(), CELL_SEMTYPE_INNER);

    public static final ListAtomicType LIST_ATOMIC_RO = new ListAtomicType(
            FixedLengthArray.empty(), CELL_SEMTYPE_INNER_RO);
}
