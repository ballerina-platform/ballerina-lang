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
import io.ballerina.runtime.internal.types.semtype.BFutureSubType;
import io.ballerina.runtime.internal.types.semtype.BIntSubType;
import io.ballerina.runtime.internal.types.semtype.BObjectSubType;
import io.ballerina.runtime.internal.types.semtype.BStreamSubType;
import io.ballerina.runtime.internal.types.semtype.BSubType;
import io.ballerina.runtime.internal.types.semtype.BTableSubType;
import io.ballerina.runtime.internal.types.semtype.BTypedescSubType;
import io.ballerina.runtime.internal.types.semtype.DelegatedSubType;
import io.ballerina.runtime.internal.types.semtype.EnumerableSubtypeData;
import io.ballerina.runtime.internal.types.semtype.SubTypeData;
import io.ballerina.runtime.internal.types.semtype.SubtypePair;
import io.ballerina.runtime.internal.types.semtype.SubtypePairs;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_B_TYPE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_CELL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_DECIMAL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_FLOAT;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_INT;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_LIST;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_STRING;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_FUTURE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_OBJECT;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_STREAM;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_TABLE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_TYPEDESC;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_UNDEF;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_MASK;
import static io.ballerina.runtime.api.types.semtype.Builder.cellContaining;
import static io.ballerina.runtime.api.types.semtype.Builder.listType;
import static io.ballerina.runtime.api.types.semtype.Builder.undef;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.cellAtomType;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.intersectCellAtomicType;
import static io.ballerina.runtime.internal.types.semtype.BListSubType.bddListMemberTypeInnerVal;
import static io.ballerina.runtime.internal.types.semtype.BMappingProj.mappingMemberTypeInner;

/**
 * Contain functions defined in `core.bal` file.
 *
 * @since 2201.10.0
 */
public final class Core {

    public static final SemType SEMTYPE_TOP = SemType.from((1 << (CODE_UNDEF + 1)) - 1);
    public static final SemType B_TYPE_TOP = SemType.from(1 << BT_B_TYPE.code());

    private Core() {
    }

    public static SemType diff(SemType t1, SemType t2) {
        int all1 = t1.all();
        int all2 = t2.all();
        int some1 = t1.some();
        int some2 = t2.some();
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
        SubType[] subtypes = Builder.initializeSubtypeArray(some);
        int i = 0;
        boolean filterNulls = false;
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
                filterNulls = true;
            } else if (data.isNothing()) {
                some &= ~(1 << code);
                filterNulls = true;
            } else {
                subtypes[i] = data;
                i++;
            }
        }
        return SemType.from(all, some, filterNulls ? filterNulls(some, subtypes) : subtypes);
    }

    // TODO: this should return SubTypeData not subtype
    public static SubType getComplexSubtypeData(SemType t, BasicTypeCode code) {
        assert (t.some() & (1 << code.code())) != 0;
        SubType subType = t.subTypeByCode(code.code());
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
        if (t.some() == 0) {
            return (t.all() & listType().all()) != 0 ? Builder.valType() : Builder.neverType();
        } else {
            SubTypeData keyData = intSubtype(k);
            if (isNothingSubtype(keyData)) {
                return Builder.neverType();
            }
            return bddListMemberTypeInnerVal(cx, (Bdd) getComplexSubtypeData(t, BT_LIST), keyData, Builder.valType());
        }
    }

    public static SemType union(SemType t1, SemType t2) {
        assert t1 != null && t2 != null;
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
        SubType[] subtypes = Builder.initializeSubtypeArray(some);
        int i = 0;
        boolean filterNulls = false;
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
                filterNulls = true;
                all |= 1 << code;
                some &= ~(1 << code);
            } else {
                subtypes[i] = data;
                i++;
            }
        }
        if (some == 0) {
            return SemType.from(all);
        }
        return SemType.from(all, some, filterNulls ? filterNulls(some, subtypes) : subtypes);
    }

    private static SubType[] filterNulls(int some, SubType[] subtypes) {
        int newSize = cardinality(some);
        SubType[] filtered = new SubType[newSize];
        System.arraycopy(subtypes, 0, filtered, 0, newSize);
        return filtered;
    }

    public static SemType intersect(SemType t1, SemType t2) {
        assert t1 != null && t2 != null;
        int all1 = t1.all();
        int some1 = t1.some();
        int all2 = t2.all();
        int some2 = t2.some();
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

        SubType[] subtypes = Builder.initializeSubtypeArray(some);
        int i = 0;
        boolean filterNulls = false;
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
                subtypes[i] = data;
                i++;
            } else {
                some &= ~(1 << code);
                filterNulls = true;
            }
        }
        if (some == 0) {
            return SemType.from(all);
        }
        return SemType.from(all, some, filterNulls ? filterNulls(some, subtypes) : subtypes);
    }

    public static boolean isEmpty(Context cx, SemType t) {
        if (t.some() == 0) {
            return t.all() == 0;
        }
        if (t.all() != 0) {
            return false;
        }
        for (SubType subType : t.subTypeData()) {
            assert subType != null : "subtype array must not be sparse";
            assert !(subType instanceof BSubType) : "expect pure semtype";
            if (!subType.isEmpty(cx)) {
                return false;
            }
        }
        return true;
    }

    public static SemType complement(SemType t) {
        return diff(Builder.valType(), t);
    }

    public static boolean isNever(SemType t) {
        return t.all() == 0 && t.some() == 0;
    }

    public static boolean isSubType(Context cx, SemType t1, SemType t2) {
        SemType.CachedResult cached = t1.cachedSubTypeRelation(t2);
        if (cached != SemType.CachedResult.NOT_FOUND) {
            return cached == SemType.CachedResult.TRUE;
        }
        boolean result = isEmpty(cx, diff(t1, t2));
        t1.cacheSubTypeRelation(t2, result);
        return result;
    }

    public static boolean isSubtypeSimple(SemType t1, SemType t2) {
        assert t1 != null && t2 != null;
        int bits = t1.all() | t1.some();
        return (bits & ~t2.all()) == 0;
    }

    public static boolean isNothingSubtype(SubTypeData data) {
        return data == AllOrNothing.NOTHING;
    }

    // Describes the subtype of int included in the type: true/false mean all or none of string
    public static SubTypeData intSubtype(SemType t) {
        return subTypeData(t, BT_INT);
    }

    // Describes the subtype of string included in the type: true/false mean all or none of string
    public static SubTypeData stringSubtype(SemType t) {
        return subTypeData(t, BT_STRING);
    }

    public static SubTypeData subTypeData(SemType s, BasicTypeCode code) {
        if ((s.all() & (1 << code.code())) != 0) {
            return AllOrNothing.ALL;
        }
        if (s.some() == 0) {
            return AllOrNothing.NOTHING;
        }
        SubType subType = s.subTypeByCode(code.code());
        assert subType != null;
        return subType.data();
    }

    public static boolean containsBasicType(SemType t1, SemType t2) {
        int bits = t1.all() | t1.some();
        return (bits & t2.all()) != 0;
    }

    public static boolean isSameType(Context cx, SemType t1, SemType t2) {
        return isSubType(cx, t1, t2) && isSubType(cx, t2, t1);
    }

    public static SemType widenToBasicTypes(SemType t) {
        int all = t.all() | t.some();
        if (cardinality(all) > 1) {
            throw new IllegalStateException("Cannot widen to basic type for a type with multiple basic types");
        }
        return Builder.basicTypeUnion(all);
    }

    private static int cardinality(int bitset) {
        return Integer.bitCount(bitset);
    }

    public static SemType widenToBasicTypeUnion(SemType t) {
        if (t.some() == 0) {
            return t;
        }
        int all = t.all() | t.some();
        return SemType.from(all);
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

    public static Optional<CellAtomicType> cellAtomicType(SemType t) {
        SemType cell = Builder.cell();
        if (t.some() == 0) {
            return cell.equals(t) ? Optional.of(Builder.cellAtomicVal()) : Optional.empty();
        } else {
            if (!isSubtypeSimple(t, cell)) {
                return Optional.empty();
            }
            return bddCellAtomicType((Bdd) getComplexSubtypeData(t, BT_CELL), Builder.cellAtomicVal());
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

    public static SemType createBasicSemType(BasicTypeCode typeCode, Bdd bdd) {
        if (bdd instanceof BddAllOrNothing) {
            return bdd.isAll() ? Builder.from(typeCode) : Builder.neverType();
        }
        SubType subType = switch (typeCode.code()) {
            case CODE_OBJECT -> BObjectSubType.createDelegate(bdd);
            case CODE_FUTURE -> BFutureSubType.createDelegate(bdd);
            case CODE_TYPEDESC -> BTypedescSubType.createDelegate(bdd);
            case CODE_TABLE -> BTableSubType.createDelegate(bdd);
            case CODE_STREAM -> BStreamSubType.createDelegate(bdd);
            default -> throw new IllegalArgumentException("Unexpected type code: " + typeCode);
        };
        return SemType.from(0, 1 << typeCode.code(), new SubType[]{subType});
    }

    private static SemType unionOf(SemType... semTypes) {
        SemType result = Builder.neverType();
        for (SemType semType : semTypes) {
            result = union(result, semType);
        }
        return result;
    }

    public static SemType mappingMemberTypeInnerVal(Context cx, SemType t, SemType k) {
        return diff(mappingMemberTypeInner(cx, t, k), Builder.undef());
    }

    public static Optional<ListAtomicType> listAtomicType(Context cx, SemType t) {
        ListAtomicType listAtomicInner = Builder.listAtomicInner();
        if (t.some() == 0) {
            return Core.isSubtypeSimple(t, Builder.listType()) ? Optional.ofNullable(listAtomicInner) :
                    Optional.empty();
        }
        Env env = cx.env;
        if (!isSubtypeSimple(t, Builder.listType())) {
            return Optional.empty();
        }
        return bddListAtomicType(env, (Bdd) getComplexSubtypeData(t, BT_LIST), listAtomicInner);
    }

    public static SemType floatToInt(SemType t) {
        if (!containsBasicType(t, Builder.floatType())) {
            return Builder.neverType();
        }
        return convertEnumerableNumericType(t, BT_FLOAT, Builder.intType(),
                (floatValue) -> ((Double) floatValue).longValue(), Builder::intConst);
    }

    public static SemType floatToDecimal(SemType t) {
        if (!containsBasicType(t, Builder.floatType())) {
            return Builder.neverType();
        }
        return convertEnumerableNumericType(t, BT_FLOAT, Builder.decimalType(),
                (floatValue) -> BigDecimal.valueOf((Double) floatValue), Builder::decimalConst);
    }

    public static SemType decimalToInt(SemType t) {
        if (!containsBasicType(t, Builder.decimalType())) {
            return Builder.neverType();
        }
        return convertEnumerableNumericType(t, BT_DECIMAL, Builder.intType(),
                (decimalVal) -> ((BigDecimal) decimalVal).longValue(), Builder::intConst);
    }

    public static SemType decimalToFloat(SemType t) {
        if (!containsBasicType(t, Builder.decimalType())) {
            return Builder.neverType();
        }
        return convertEnumerableNumericType(t, BT_DECIMAL, Builder.floatType(),
                (decimalVal) -> ((BigDecimal) decimalVal).doubleValue(), Builder::floatConst);
    }

    public static SemType intToFloat(SemType t) {
        if (!containsBasicType(t, Builder.intType())) {
            return Builder.neverType();
        }
        SubTypeData subTypeData = subTypeData(t, BT_INT);
        if (subTypeData == AllOrNothing.NOTHING) {
            return Builder.neverType();
        }
        if (subTypeData == AllOrNothing.ALL) {
            return Builder.floatType();
        }
        BIntSubType.IntSubTypeData intSubTypeData = (BIntSubType.IntSubTypeData) subTypeData;
        return intSubTypeData.values().stream().map(Builder::floatConst).reduce(Builder.neverType(), Core::union);
    }

    public static SemType intToDecimal(SemType t) {
        if (!containsBasicType(t, Builder.intType())) {
            return Builder.neverType();
        }
        SubTypeData subTypeData = subTypeData(t, BT_INT);
        if (subTypeData == AllOrNothing.NOTHING) {
            return Builder.neverType();
        }
        if (subTypeData == AllOrNothing.ALL) {
            return Builder.decimalType();
        }
        BIntSubType.IntSubTypeData intSubTypeData = (BIntSubType.IntSubTypeData) subTypeData;
        return intSubTypeData.values().stream().map(BigDecimal::new).map(Builder::decimalConst)
                .reduce(Builder.neverType(), Core::union);
    }

    private static <E extends Comparable<E>, T extends Comparable<T>> SemType convertEnumerableNumericType(
            SemType source, BasicTypeCode targetTypeCode, SemType topType, Function<T, E> valueConverter,
            Function<E, SemType> semTypeCreator) {
        SubTypeData subTypeData = subTypeData(source, targetTypeCode);
        if (subTypeData == AllOrNothing.NOTHING) {
            return Builder.neverType();
        }
        if (subTypeData == AllOrNothing.ALL) {
            return topType;
        }
        //noinspection unchecked - it's a enumerable type
        EnumerableSubtypeData<T> enumerableSubtypeData = (EnumerableSubtypeData<T>) subTypeData;
        SemType posType =
                Arrays.stream(enumerableSubtypeData.values()).map(valueConverter).distinct().map(semTypeCreator)
                        .reduce(Builder.neverType(), Core::union);
        if (enumerableSubtypeData.allowed()) {
            return posType;
        }
        return diff(topType, posType);
    }

    private static Optional<ListAtomicType> bddListAtomicType(Env env, Bdd bdd, ListAtomicType top) {
        if (!(bdd instanceof BddNode bddNode)) {
            if (bdd.isAll()) {
                return Optional.ofNullable(top);
            } else {
                return Optional.empty();
            }
        }
        return bddNode.isSimple() ? Optional.of(env.listAtomType(bddNode.atom())) : Optional.empty();
    }
}
