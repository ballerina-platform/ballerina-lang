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

import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.BddNode;

import static io.ballerina.types.Core.getComplexSubtypeData;
import static io.ballerina.types.SemTypes.isSubtypeSimple;

/**
 * CellAtomicType node.
 *
 * @since 2201.10.0
 */
public final class CellAtomicType implements AtomicType {
    public final SemType ty;
    public final CellMutability mut;

    private CellAtomicType(SemType ty, CellMutability mut) {
        this.ty = ty;
        this.mut = mut;
    }

    public static CellAtomicType from(SemType ty) {
        if (ty instanceof BasicTypeBitSet bitSet) {
            return bitSet.bitset == PredefinedType.CELL.bitset ? PredefinedType.CELL_ATOMIC_VAL : null;
        }
        assert ty instanceof ComplexSemType;
        if (!isSubtypeSimple(ty, PredefinedType.CELL)) {
            return null;
        }
        return bddCellAtomicType((Bdd) getComplexSubtypeData((ComplexSemType) ty, BasicTypeCode.BT_CELL),
                PredefinedType.CELL_ATOMIC_VAL);
    }

    private static CellAtomicType bddCellAtomicType(Bdd bdd, CellAtomicType top) {
        if (bdd instanceof AllOrNothingSubtype allOrNothingSubtype) {
            if (allOrNothingSubtype.isAllSubtype()) {
                return top;
            }
            return null;
        }
        BddNode bddNode = (BddNode) bdd;
        if (bddNode.isSimpleBddNode()) {
            return cellAtom(bddNode.atom);
        }
        return null;
    }

    private static CellAtomicType cellAtom(Atom atom) {
        return (CellAtomicType) ((TypeAtom) atom).atomicType;
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

    @Override
    public String toString() {
        return "CellAtomicType{ty=" + ty + ", mut=" + mut + "}";
    }
}
