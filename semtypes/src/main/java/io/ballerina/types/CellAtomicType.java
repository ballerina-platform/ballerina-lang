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
package io.ballerina.types;

import static io.ballerina.types.Core.getComplexSubtypeData;
import static io.ballerina.types.SemTypes.isSubtypeSimple;

/**
 * CellAtomicType node.
 *
 * @param ty  Type "wrapped" by this cell
 * @param mut Mutability of the cell
 * @since 2201.10.0
 */
public record CellAtomicType(SemType ty, CellMutability mut) implements AtomicType {

    public static CellAtomicType from(SemType ty) {
        if (ty instanceof BasicTypeBitSet bitSet) {
            return bitSet.bitset == PredefinedType.CELL.bitset ? PredefinedType.CELL_ATOMIC_VAL : null;
        }
        assert ty instanceof ComplexSemType;
        if (!isSubtypeSimple(ty, PredefinedType.CELL)) {
            return null;
        }
        return Core.bddCellAtomicType((Bdd) getComplexSubtypeData((ComplexSemType) ty, BasicTypeCode.BT_CELL),
                PredefinedType.CELL_ATOMIC_VAL);
    }

    public static CellAtomicType from(SemType ty, CellMutability mut) {
        // TODO: return final fields where applicable
        return new CellAtomicType(ty, mut);
    }

    public enum CellMutability {
        CELL_MUT_NONE,
        CELL_MUT_LIMITED,
        CELL_MUT_UNLIMITED
    }
}
