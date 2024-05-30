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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Atom;
import io.ballerina.runtime.api.types.semtype.Bdd;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.CellAtomicType;
import io.ballerina.runtime.api.types.semtype.Conjunction;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;
import io.ballerina.runtime.api.types.semtype.SubType;
import io.ballerina.runtime.api.types.semtype.TypeAtom;

import java.util.function.Predicate;

// TODO: would making this a child class of say BddNode be faster than making this a delegate
//   -- Problem with this is modeling type operations (union, intersect, complement) since parent must return a Cell
//   as well
public class BCellSubType extends SubType {

    private final Bdd inner;

    private BCellSubType(Bdd inner) {
        super(inner.isAll(), inner.isNothing());
        this.inner = inner;
    }

    public static BCellSubType createDelegate(SubType inner) {
        if (inner instanceof Bdd bdd) {
            return new BCellSubType(bdd);
        } else if (inner.isAll() || inner.isNothing()) {
            // FIXME: if all or nothing do the same thing as cellSubtypeDataEnsureProper
            throw new IllegalStateException("unimplemented");
        } else if (inner instanceof BCellSubType bCell) {
            return new BCellSubType(bCell.inner);
        }
        throw new IllegalArgumentException("Unexpected inner type");
    }

    public static CellAtomicType cellAtomType(Atom atom) {
        return (CellAtomicType) ((TypeAtom) atom).atomicType();
    }

    @Override
    public SubType union(SubType other) {
        if (!(other instanceof BCellSubType otherCell)) {
            throw new IllegalArgumentException("union of different subtypes");
        }
        return createDelegate(inner.union(otherCell.inner));
    }

    @Override
    public SubType intersect(SubType other) {
        if (!(other instanceof BCellSubType otherCell)) {
            throw new IllegalArgumentException("intersect of different subtypes");
        }
        return createDelegate(inner.intersect(otherCell.inner));
    }

    @Override
    public SubType complement() {
        return createDelegate(inner.complement());
    }

    @Override
    public boolean isEmpty(Context cx) {
        return Bdd.bddEvery(cx, inner, null, null, BCellSubType::cellFormulaIsEmpty);
    }

    @Override
    public SubTypeData data() {
        throw new IllegalStateException("unimplemented");
    }

    private static boolean cellFormulaIsEmpty(Context cx, Conjunction posList, Conjunction negList) {
        CellAtomicType combined;
        if (posList == null) {
            combined = new CellAtomicType(Builder.val(), CellAtomicType.CellMutability.CELL_MUT_UNLIMITED);
        } else {
            combined = cellAtomType(posList.atom());
            Conjunction p = posList.next();
            while (p != null) {
                combined = intersectCellAtomicType(combined, cellAtomType(p.atom()));
                p = p.next();
            }
        }
        return !cellInhabited(cx, combined, negList);
    }

    private static boolean cellInhabited(Context cx, CellAtomicType posCell, Conjunction negList) {
        SemType pos = posCell.ty();
        if (Core.isEmpty(cx, pos)) {
            return false;
        }
        return switch (posCell.mut()) {
            case CELL_MUT_NONE -> cellMutNoneInhabited(cx, pos, negList);
            case CELL_MUT_LIMITED -> cellMutLimitedInhabited(cx, pos, negList);
            default -> cellMutUnlimitedInhabited(cx, pos, negList);
        };
    }

    private static boolean cellMutUnlimitedInhabited(Context cx, SemType pos, Conjunction negList) {
        Conjunction neg = negList;
        while (neg != null) {
            if (cellAtomType(neg.atom()).mut() == CellAtomicType.CellMutability.CELL_MUT_LIMITED &&
                    Core.isSameType(cx, Builder.val(), cellAtomType(neg.atom()).ty())) {
                return false;
            }
            neg = neg.next();
        }
        SemType negListUnionResult = filteredCellListUnion(negList,
                conjunction -> cellAtomType(conjunction.atom()).mut() ==
                        CellAtomicType.CellMutability.CELL_MUT_UNLIMITED);
        // We expect `isNever` condition to be `true` when there are no negative atoms with unlimited mutability.
        // Otherwise, we do `isEmpty` to conclude on the inhabitance.
        return Core.isNever(negListUnionResult) || !Core.isEmpty(cx, Core.diff(pos, negListUnionResult));
    }

    private static boolean cellMutLimitedInhabited(Context cx, SemType pos, Conjunction negList) {
        if (negList == null) {
            return true;
        }
        CellAtomicType negAtomicCell = cellAtomType(negList.atom());
        if ((negAtomicCell.mut().compareTo(CellAtomicType.CellMutability.CELL_MUT_LIMITED) >= 0) &&
                Core.isEmpty(cx, Core.diff(pos, negAtomicCell.ty()))) {
            return false;
        }
        return cellMutLimitedInhabited(cx, pos, negList.next());
    }

    private static boolean cellMutNoneInhabited(Context cx, SemType pos, Conjunction negList) {
        SemType negListUnionResult = cellListUnion(negList);
        // We expect `isNever` condition to be `true` when there are no negative atoms.
        // Otherwise, we do `isEmpty` to conclude on the inhabitance.
        return Core.isNever(negListUnionResult) || !Core.isEmpty(cx, Core.diff(pos, negListUnionResult));
    }

    private static SemType cellListUnion(Conjunction negList) {
        return filteredCellListUnion(negList, neg -> true);
    }

    private static SemType filteredCellListUnion(Conjunction negList, Predicate<Conjunction> predicate) {
        SemType negUnion = Builder.neverType();
        Conjunction neg = negList;
        while (neg != null) {
            if (predicate.test(neg)) {
                negUnion = Core.union(negUnion, cellAtomType(neg.atom()).ty());
            }
            neg = neg.next();
        }
        return negUnion;
    }

    private static CellAtomicType intersectCellAtomicType(CellAtomicType c1, CellAtomicType c2) {
        SemType ty = Core.intersect(c1.ty(), c2.ty());
        CellAtomicType.CellMutability mut = min(c1.mut(), c2.mut());
        return new CellAtomicType(ty, mut);
    }

    private static CellAtomicType.CellMutability min(CellAtomicType.CellMutability m1,
                                                     CellAtomicType.CellMutability m2) {
        return m1.compareTo(m2) <= 0 ? m1 : m2;
    }
}
