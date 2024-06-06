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

import io.ballerina.runtime.internal.types.semtype.AllOrNothing;
import io.ballerina.runtime.internal.types.semtype.DelegatedSubType;
import io.ballerina.runtime.internal.types.semtype.SubTypeData;
import io.ballerina.runtime.internal.types.semtype.SubtypePair;
import io.ballerina.runtime.internal.types.semtype.SubtypePairs;

import java.util.Optional;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_B_TYPE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_CELL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_INT;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_LIST;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_UNDEF;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_MASK;
import static io.ballerina.runtime.api.types.semtype.Builder.cellContaining;
import static io.ballerina.runtime.api.types.semtype.Builder.listType;
import static io.ballerina.runtime.api.types.semtype.Builder.undef;
import static io.ballerina.runtime.api.types.semtype.Builder.valType;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.intersectCellAtomicType;
import static io.ballerina.runtime.internal.types.semtype.BCellSubType.cellAtomType;
import static io.ballerina.runtime.internal.types.semtype.BListSubType.bddListMemberTypeInnerVal;

/**
 * Contain functions defined in `core.bal` file.
 *
 * @since 2201.10.0
 */
public final class Core {

    public static final SemType SEMTYPE_TOP = SemType.from((1 << (CODE_UNDEF + 1)) - 1);
    public static final SemType B_TYPE_TOP = SemType.from(1 << BT_B_TYPE.code());

    // TODO: move to builder
    private static final CellAtomicType CELL_ATOMIC_VAL = new CellAtomicType(
            valType(), CellAtomicType.CellMutability.CELL_MUT_LIMITED
    );

    private Core() {
    }

    public static SemType diff(SemType t1, SemType t2) {
        int all1 = t1.all;
        int all2 = t2.all;
        int some1 = t1.some;
        int some2 = t2.some;
        if (some1 == 0) {
            if (some2 == 0) {
                return Builder.basicTypeUnion(all1 & ~all2);
            } else {
                if (all1 == 0) {
                    return t1;
                }
            }
        } else {
            if (some2 == 0) {
                if (all2 == VT_MASK) {
                    return Builder.basicTypeUnion(0);
                }
            }
        }
        int all = all1 & ~(all2 | some2);
        int some = (all1 | some1) & ~all2;
        some = some & ~all;
        if (some == 0) {
            return SemType.from(all);
        }
        SubType[] subtypes = Builder.initializeSubtypeArray();
        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            SubType data1 = pair.subType1();
            SubType data2 = pair.subType2();
            int code = pair.typeCode();
            SubType data;
            if (data1 == null) {
                data = data2.complement();
            } else if (data2 == null) {
                data = data1;
            } else {
                data = data1.diff(data2);
            }
            if (data.isAll()) {
                all |= 1 << code;
                some &= ~(1 << code);
            } else if (data.isNothing()) {
                some &= ~(1 << code);
            } else {
                subtypes[code] = data;
            }
        }
        return SemType.from(all, some, subtypes);
    }

    public static SubType getComplexSubtypeData(SemType t, BasicTypeCode code) {
        assert (t.some() & (1 << code.code())) != 0;
        SubType subType = t.subTypeData()[code.code()];
        if (subType instanceof DelegatedSubType wrapper) {
            return wrapper.inner();
        }
        return subType;
    }

    // This computes the spec operation called "member type of K in T",
    // for the case when T is a subtype of list, and K is either `int` or a singleton int.
    // This is what Castagna calls projection.
    // We will extend this to allow `key` to be a SemType, which will turn into an IntSubtype.
    // If `t` is not a list, NEVER is returned
    public static SemType listMemberTypeInnerVal(Context cx, SemType t, SemType k) {
        if (t.some == 0) {
            return (t.all & listType().all) != 0 ? Builder.valType() : Builder.neverType();
        } else {
            SubTypeData keyData = intSubtype(k);
            if (isNothingSubtype(keyData)) {
                return Builder.neverType();
            }
            return bddListMemberTypeInnerVal(cx, (Bdd) getComplexSubtypeData(t, BT_LIST), keyData, Builder.valType());
        }
    }

    public static SemType union(SemType t1, SemType t2) {
        int all1 = t1.all();
        int some1 = t1.some();
        int all2 = t2.all();
        int some2 = t2.some();
        if (some1 == 0) {
            if (some2 == 0) {
                return Builder.basicTypeUnion(all1 | all2);
            }
        }

        int all = all1 | all2;
        int some = (some1 | some2) & ~all;
        if (some == 0) {
            return Builder.basicTypeUnion(all);
        }
        SubType[] subtypes = Builder.initializeSubtypeArray();
        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            int code = pair.typeCode();
            SubType data1 = pair.subType1();
            SubType data2 = pair.subType2();
            SubType data;
            if (data1 == null) {
                data = data2;
            } else if (data2 == null) {
                data = data1;
            } else {
                data = data1.union(data2);
            }
            if (data.isAll()) {
                all |= 1 << code;
                some &= ~(1 << code);
            } else {
                subtypes[code] = data;
            }
        }
        if (some == 0) {
            return SemType.from(all);
        }
        return SemType.from(all, some, subtypes);
    }

    public static SemType intersect(SemType t1, SemType t2) {
        int all1 = t1.all;
        int some1 = t1.some;
        int all2 = t2.all;
        int some2 = t2.some;
        if (some1 == 0) {
            if (some2 == 0) {
                return SemType.from(all1 & all2);
            } else {
                if (all1 == 0) {
                    return t1;
                }
                if (all1 == VT_MASK) {
                    return t2;
                }
            }
        } else if (some2 == 0) {
            if (all2 == 0) {
                return t2;
            }
            if (all2 == VT_MASK) {
                return t1;
            }
        }

        int all = all1 & all2;
        int some = (some1 | all1) & (some2 | all2);
        some = some & ~all;
        if (some == 0) {
            return SemType.from(all);
        }

        SubType[] subtypes = Builder.initializeSubtypeArray();
        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            int code = pair.typeCode();
            SubType data1 = pair.subType1();
            SubType data2 = pair.subType2();

            SubType data;
            if (data1 == null) {
                data = data2;
            } else if (data2 == null) {
                data = data1;
            } else {
                data = data1.intersect(data2);
            }

            if (!data.isNothing()) {
                subtypes[code] = data;
            } else {
                some &= ~(1 << code);
            }
        }
        if (some == 0) {
            return SemType.from(all);
        }
        return SemType.from(all, some, subtypes);
    }

    public static boolean isEmpty(Context cx, SemType t) {
        if (t.some == 0) {
            return t.all == 0;
        }
        if (t.all != 0) {
            return false;
        }
        for (SubType subType : t.subTypeData()) {
            if (subType == null) {
                continue;
            }
            if (!subType.isEmpty(cx)) {
                return false;
            }
        }
        return true;
    }

    public static SemType complement(SemType t1) {
        throw new IllegalStateException("Unimplemented");
    }

    public static boolean isNever(SemType t) {
        return t.all == 0 && t.some == 0;
    }

    public static boolean isSubType(Context cx, SemType t1, SemType t2) {
        // IF t1 and t2 are not pure semtypes calling this is an undefined
        return isEmpty(cx, diff(t1, t2));
    }

    public static boolean isSubtypeSimple(SemType t1, SemType t2) {
        int bits = t1.all | t1.some;
        return (bits & ~t2.all()) == 0;
    }

    public static boolean isNothingSubtype(SubTypeData data) {
        return data == AllOrNothing.NOTHING;
    }

    // Describes the subtype of int included in the type: true/false mean all or none of string
    public static SubTypeData intSubtype(SemType t) {
        return subTypeData(t, BT_INT);
    }

    public static SubTypeData subTypeData(SemType s, BasicTypeCode code) {
        if ((s.all & (1 << code.code())) != 0) {
            return AllOrNothing.ALL;
        }
        if (s.some == 0) {
            return AllOrNothing.NOTHING;
        }
        return s.subTypeData()[code.code()].data();
    }

    public static boolean containsBasicType(SemType t1, SemType t2) {
        int bits = t1.all | t1.some;
        return (bits & t2.all) != 0;
    }

    public static boolean isSameType(Context cx, SemType t1, SemType t2) {
        return isSubType(cx, t1, t2) && isSubType(cx, t2, t1);
    }

    public static BasicTypeBitSet widenToBasicTypes(SemType t) {
        int all = t.all | t.some;
        if (cardinality(all) > 1) {
            throw new IllegalStateException("Cannot widen to basic type for a type with multiple basic types");
        }
        return Builder.basicTypeUnion(all);
    }

    private static int cardinality(int bitset) {
        return Integer.bitCount(bitset);
    }

    public static SemType widenToBasicTypeUnion(SemType t) {
        if (t.some == 0) {
            return t;
        }
        int all = t.all | t.some;
        return Builder.basicTypeUnion(all);
    }

    public static SemType cellContainingInnerVal(Env env, SemType t) {
        CellAtomicType cat =
                cellAtomicType(t).orElseThrow(() -> new IllegalArgumentException("t is not a cell semtype"));
        return cellContaining(env, diff(cat.ty(), undef()), cat.mut());
    }

    public static SemType intersectMemberSemTypes(Env env, SemType t1, SemType t2) {
        CellAtomicType c1 =
                cellAtomicType(t1).orElseThrow(() -> new IllegalArgumentException("t1 is not a cell semtype"));
        CellAtomicType c2 =
                cellAtomicType(t2).orElseThrow(() -> new IllegalArgumentException("t2 is not a cell semtype"));

        CellAtomicType atomicType = intersectCellAtomicType(c1, c2);
        return cellContaining(env, atomicType.ty(), undef().equals(atomicType.ty()) ? CELL_MUT_NONE : atomicType.mut());
    }

    private static Optional<CellAtomicType> cellAtomicType(SemType t) {
        SemType cell = Builder.cell();
        if (t.some == 0) {
            return cell.equals(t) ? Optional.of(CELL_ATOMIC_VAL) : Optional.empty();
        } else {
            if (!isSubtypeSimple(t, cell)) {
                return Optional.empty();
            }
            return bddCellAtomicType((Bdd) getComplexSubtypeData(t, BT_CELL), CELL_ATOMIC_VAL);
        }
    }

    private static Optional<CellAtomicType> bddCellAtomicType(Bdd bdd, CellAtomicType top) {
        if (bdd instanceof BddAllOrNothing allOrNothing) {
            if (allOrNothing.isAll()) {
                return Optional.of(top);
            }
            return Optional.empty();
        }
        BddNode bddNode = (BddNode) bdd;
        return bddNode.isSimple() ? Optional.of(cellAtomType(bddNode.atom())) : Optional.empty();
    }

    public static SemType cellInnerVal(SemType t) {
        return diff(cellInner(t), undef());
    }

    public static SemType cellInner(SemType t) {
        CellAtomicType cat =
                cellAtomicType(t).orElseThrow(() -> new IllegalArgumentException("t is not a cell semtype"));
        return cat.ty();
    }

}
