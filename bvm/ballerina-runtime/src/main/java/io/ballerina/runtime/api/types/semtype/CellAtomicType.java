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

import static io.ballerina.runtime.api.types.semtype.TypeAtom.createTypeAtom;

/**
 * CellAtomicType node.
 *
 * @param ty  Type "wrapped" by this cell
 * @param mut Mutability of the cell
 * @since 2201.10.0
 */
public record CellAtomicType(SemType ty, CellMutability mut) implements AtomicType {

    private static final AtomicType CELL_ATOMIC_VAL = new CellAtomicType(
            Builder.val(), CellAtomicType.CellMutability.CELL_MUT_LIMITED
    );
    public static final TypeAtom ATOM_CELL_VAL = createTypeAtom(0, CELL_ATOMIC_VAL);

    public static final CellAtomicType CELL_ATOMIC_NEVER = new CellAtomicType(
            Builder.neverType(), CellAtomicType.CellMutability.CELL_MUT_LIMITED
    );

    public enum CellMutability {
        CELL_MUT_NONE,
        CELL_MUT_LIMITED,
        CELL_MUT_UNLIMITED
    }
}
