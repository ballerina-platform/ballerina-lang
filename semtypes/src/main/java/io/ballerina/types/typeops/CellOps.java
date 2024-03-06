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
package io.ballerina.types.typeops;

import io.ballerina.types.Atom;
import io.ballerina.types.Bdd;
import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Common;
import io.ballerina.types.Conjunction;
import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeOps;
import io.ballerina.types.subtypedata.AllOrNothingSubtype;

import static io.ballerina.types.Common.bddSubtypeComplement;
import static io.ballerina.types.Common.bddSubtypeDiff;
import static io.ballerina.types.Common.bddSubtypeIntersect;
import static io.ballerina.types.Common.bddSubtypeUnion;
import static io.ballerina.types.Env.cellAtomType;
import static io.ballerina.types.TypeAtom.ATOM_CELL_NEVER;
import static io.ballerina.types.TypeAtom.ATOM_CELL_VAL;
import static io.ballerina.types.typeops.BddCommonOps.bddAtom;

/**
 * Basic type ops for cell type.
 *
 * @since 2201.10.0
 */
public class CellOps extends CommonOps implements UniformTypeOps {

    private static boolean cellFormulaIsEmpty(Context cx, SubtypeData t) {
        return Common.bddEvery(cx, (Bdd) t, null, null, CellOps::cellFormulaIsEmpty);
    }

    private static boolean cellFormulaIsEmpty(Context cx, Conjunction posList, Conjunction negList) {
        CellAtomicType combined;
        if (posList == null) {
            combined = CellAtomicType.from(PredefinedType.TOP, CellAtomicType.CellMutability.CELL_MUT_UNLIMITED);
        } else {
            combined = cellAtomType(posList.atom);
            Conjunction p = posList.next;
            while (p != null) {
                combined = intersectCellAtomicType(combined, cellAtomType(p.atom));
                p = p.next;
            }
        }
        return !cellInhabited(cx, combined, negList);
    }

    private static boolean cellInhabited(Context cx, CellAtomicType posCell, Conjunction negList) {
        SemType pos = posCell.ty;
        if (Core.isEmpty(cx, pos)) {
            return false;
        }
        switch (posCell.mut) {
            case CELL_MUT_NONE -> {
                return cellMutNoneInhabited(cx, pos, negList);
            }
            case CELL_MUT_LIMITED -> {
                return cellMutLimitedInhabited(cx, pos, negList);
            }
            default -> {
                return cellMutUnlimitedInhabited(cx, pos, negList);
            }
        }
    }

    private static boolean cellMutNoneInhabited(Context cx, SemType pos, Conjunction negList) {
        SemType negListUnionResult = cellNegListUnion(negList);
        // We expect `isNever` condition to be `true` when there are no negative atoms.
        // Otherwise, we do `isEmpty` to conclude on the inhabitance.
        return negListUnionResult == PredefinedType.NEVER || !Core.isEmpty(cx, Core.diff(pos, negListUnionResult));
    }

    private static SemType cellNegListUnion(Conjunction negList) {
        SemType negUnion = PredefinedType.NEVER;
        Conjunction neg = negList;
        while (neg != null) {
            negUnion = Core.union(negUnion, cellAtomType(neg.atom).ty);
            neg = neg.next;
        }
        return negUnion;
    }

    private static boolean cellMutLimitedInhabited(Context cx, SemType pos, Conjunction negList) {
        if (negList == null) {
            return true;
        }
        CellAtomicType negAtomicCell = cellAtomType(negList.atom);
        if (negAtomicCell.mut.getValue() >= CellAtomicType.CellMutability.CELL_MUT_LIMITED.getValue() &&
                Core.isEmpty(cx, Core.diff(pos, negAtomicCell.ty))) {
            return false;
        }
        return cellMutLimitedInhabited(cx, pos, negList.next);
    }

    private static boolean cellMutUnlimitedInhabited(Context cx, SemType pos, Conjunction negList) {
        Conjunction neg = negList;
        while (neg != null) {
            if (cellAtomType(neg.atom).mut == CellAtomicType.CellMutability.CELL_MUT_LIMITED &&
                    Core.isSameType(cx, PredefinedType.TOP, cellAtomType(neg.atom).ty)) {
                return false;
            }
            neg = neg.next;
        }
        SemType negListUnionResult = cellNegListUnlimitedUnion(negList);
        // We expect `isNever` condition to be `true` when there are no negative atoms with unlimited mutability.
        // Otherwise, we do `isEmpty` to conclude on the inhabitance.
        return PredefinedType.NEVER.equals(negListUnionResult) || !Core.isEmpty(cx, Core.diff(pos, negListUnionResult));
    }

    private static SemType cellNegListUnlimitedUnion(Conjunction negList) {
        SemType negUnion = PredefinedType.NEVER;
        Conjunction neg = negList;
        while (neg != null) {
            if (cellAtomType(neg.atom).mut == CellAtomicType.CellMutability.CELL_MUT_UNLIMITED) {
                negUnion = Core.union(negUnion, cellAtomType(neg.atom).ty);
            }
            neg = neg.next;
        }
        return negUnion;
    }

    private static CellAtomicType intersectCellAtomicType(CellAtomicType c1, CellAtomicType c2) {
        SemType ty = Core.intersect(c1.ty, c2.ty);
        CellAtomicType.CellMutability mut = CellAtomicType.CellMutability.fromValue(
                Integer.min(c1.mut.getValue(), c2.mut.getValue())
        );
        return CellAtomicType.from(ty, mut);
    }

    private static ProperSubtypeData cellSubtypeUnion(ProperSubtypeData t1, ProperSubtypeData t2) {
        return cellSubtypeDataEnsureProper(bddSubtypeUnion(t1, t2));
    }

    private static ProperSubtypeData cellSubtypeIntersect(ProperSubtypeData t1, ProperSubtypeData t2) {
        return cellSubtypeDataEnsureProper(bddSubtypeIntersect(t1, t2));
    }

    private static ProperSubtypeData cellSubtypeDiff(ProperSubtypeData t1, ProperSubtypeData t2) {
        return cellSubtypeDataEnsureProper(bddSubtypeDiff(t1, t2));
    }

    private static ProperSubtypeData cellSubtypeComplement(ProperSubtypeData t) {
        return cellSubtypeDataEnsureProper(bddSubtypeComplement(t));
    }

    /**
     * SubtypeData being a boolean would result in a BasicTypeBitSet which could either be 0 or 1 << BT_CELL.
     * This is to avoid cell type being a BasicTypeBitSet, as we don't want to lose the cell wrapper.
     */
    private static ProperSubtypeData cellSubtypeDataEnsureProper(SubtypeData subtypeData) {
        if (subtypeData instanceof AllOrNothingSubtype allOrNothingSubtype) {
            Atom atom;
            if (allOrNothingSubtype.isAllSubtype()) {
                atom = ATOM_CELL_VAL;
            } else {
                atom = ATOM_CELL_NEVER;
            }
            return bddAtom(atom);
        } else {
            return (ProperSubtypeData) subtypeData;
        }
    }

    @Override
    public SubtypeData union(SubtypeData t1, SubtypeData t2) {
        // TODO: Need to port ballerina-platform/nBallerina#1125 to avoid casting
        return cellSubtypeUnion((ProperSubtypeData) t1, (ProperSubtypeData) t2);
    }

    @Override
    public SubtypeData intersect(SubtypeData t1, SubtypeData t2) {
        return cellSubtypeIntersect((ProperSubtypeData) t1, (ProperSubtypeData) t2);
    }

    @Override
    public SubtypeData diff(SubtypeData t1, SubtypeData t2) {
        return cellSubtypeDiff((ProperSubtypeData) t1, (ProperSubtypeData) t2);
    }

    @Override
    public SubtypeData complement(SubtypeData t) {
        return cellSubtypeComplement((ProperSubtypeData) t);
    }

    @Override
    public boolean isEmpty(Context cx, SubtypeData t) {
        return cellFormulaIsEmpty(cx, t);
    }
}
