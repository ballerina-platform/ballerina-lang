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

import io.ballerina.types.definition.ListDefinition;
import io.ballerina.types.definition.MappingDefinition;
import io.ballerina.types.definition.ObjectDefinition;
import io.ballerina.types.definition.ObjectQualifiers;
import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.subtypedata.BddNodeImpl;
import io.ballerina.types.subtypedata.BddNodeSimple;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.Range;
import io.ballerina.types.subtypedata.StringSubtype;
import io.ballerina.types.subtypedata.TableSubtype;
import io.ballerina.types.typeops.SubtypePair;
import io.ballerina.types.typeops.SubtypePairs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.ballerina.types.BasicTypeCode.BT_BOOLEAN;
import static io.ballerina.types.BasicTypeCode.BT_CELL;
import static io.ballerina.types.BasicTypeCode.BT_DECIMAL;
import static io.ballerina.types.BasicTypeCode.BT_FLOAT;
import static io.ballerina.types.BasicTypeCode.BT_INT;
import static io.ballerina.types.BasicTypeCode.BT_LIST;
import static io.ballerina.types.BasicTypeCode.BT_MAPPING;
import static io.ballerina.types.BasicTypeCode.BT_NIL;
import static io.ballerina.types.BasicTypeCode.BT_STRING;
import static io.ballerina.types.BasicTypeCode.VT_MASK;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.types.Common.isNothingSubtype;
import static io.ballerina.types.PredefinedType.CELL_ATOMIC_VAL;
import static io.ballerina.types.PredefinedType.INNER;
import static io.ballerina.types.PredefinedType.LIST;
import static io.ballerina.types.PredefinedType.LIST_ATOMIC_INNER;
import static io.ballerina.types.PredefinedType.MAPPING;
import static io.ballerina.types.PredefinedType.MAPPING_ATOMIC_INNER;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.REGEXP;
import static io.ballerina.types.PredefinedType.SIMPLE_OR_STRING;
import static io.ballerina.types.PredefinedType.UNDEF;
import static io.ballerina.types.PredefinedType.VAL;
import static io.ballerina.types.PredefinedType.VAL_READONLY;
import static io.ballerina.types.PredefinedType.XML;
import static io.ballerina.types.subtypedata.CellSubtype.cellContaining;
import static io.ballerina.types.typeops.CellOps.intersectCellAtomicType;
import static io.ballerina.types.typeops.ListOps.bddListMemberTypeInnerVal;
import static io.ballerina.types.typeops.MappingOps.bddMappingMemberTypeInner;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

/**
 * Contain functions defined in `core.bal` file.
 *
 * @since 2201.8.0
 */
public final class Core {

    public static CellAtomicType cellAtomType(Atom atom) {
        return (CellAtomicType) ((TypeAtom) atom).atomicType();
    }

    public static SemType diff(SemType t1, SemType t2) {
        int all1, all2, some1, some2;
        if (t1 instanceof BasicTypeBitSet b1) {
            if (t2 instanceof BasicTypeBitSet b2) {
                return BasicTypeBitSet.from(b1.bitset & ~b2.bitset);
            } else {
                if (b1.bitset == 0) {
                    return t1;
                }
                ComplexSemType c2 = (ComplexSemType) t2;
                all2 = c2.all();
                some2 = c2.some();
            }
            all1 = b1.bitset;
            some1 = 0;
        } else {
            ComplexSemType c1 = (ComplexSemType) t1;
            all1 = c1.all();
            some1 = c1.some();
            if (t2 instanceof BasicTypeBitSet b2) {
                if (b2.bitset == BasicTypeCode.VT_MASK) {
                    return BasicTypeBitSet.from(0);
                }
                all2 = b2.bitset;
                some2 = 0;
            } else {
                ComplexSemType c2 = (ComplexSemType) t2;
                all2 = c2.all();
                some2 = c2.some();
            }
        }
        BasicTypeBitSet all = BasicTypeBitSet.from(all1 & ~(all2 | some2));

        int someBitset = (all1 | some1) & ~all2;
        someBitset = someBitset & ~all.bitset;
        BasicTypeBitSet some = BasicTypeBitSet.from(someBitset);

        if (some.bitset == 0) {
            return PredefinedType.basicTypeUnion(all.bitset);
        }
        List<BasicSubtype> subtypes = new ArrayList<>();

        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            BasicTypeCode code = pair.basicTypeCode;
            SubtypeData data1 = pair.subtypeData1;
            SubtypeData data2 = pair.subtypeData2;
            SubtypeData data;
            if (data1 == null) {
                data = OpsTable.OPS[code.code].complement(data2);
            } else if (data2 == null) {
                data = data1;
            } else {
                data = OpsTable.OPS[code.code].diff(data1, data2);
            }
            if (!(data instanceof AllOrNothingSubtype allOrNothingSubtype)) {
                subtypes.add(BasicSubtype.from(code, (ProperSubtypeData) data));
            } else if (allOrNothingSubtype.isAllSubtype()) {
                int c = code.code;
                all = BasicTypeBitSet.from(all.bitset | (1 << c));
            }
            // No need to consider `data == false` case. The `some` variable above is not used to create the SemType
        }
        if (subtypes.isEmpty()) {
            return all;
        }
        return ComplexSemType.createComplexSemType(all.bitset, subtypes);
    }

    public static List<BasicSubtype> unpackComplexSemType(ComplexSemType t) {
        int some = t.some();
        List<BasicSubtype> subtypeList = new ArrayList<>();
        for (ProperSubtypeData data : t.subtypeDataList()) {
            BasicTypeCode code = BasicTypeCode.from(Integer.numberOfTrailingZeros(some));
            subtypeList.add(BasicSubtype.from(code, data));
            int c = code.code;
            some ^= (1 << c);
        }
        return subtypeList;
    }

    public static SubtypeData getComplexSubtypeData(ComplexSemType t, BasicTypeCode code) {
        int c = code.code;
        c = 1 << c;
        if ((t.all() & c) != 0) {
            return AllOrNothingSubtype.createAll();
        }
        if ((t.some() & c) == 0) {
            return AllOrNothingSubtype.createNothing();
        }
        int loBits = t.some() & (c - 1);
        return t.subtypeDataList()[loBits == 0 ? 0 : Integer.bitCount(loBits)];
    }

    public static SemType union(SemType t1, SemType t2) {
        assert t1 != null && t2 != null;
        int all1, all2, some1, some2;

        if (t1 instanceof BasicTypeBitSet b1) {
            if (t2 instanceof BasicTypeBitSet b2) {
                return BasicTypeBitSet.from(b1.bitset | b2.bitset);
            } else {
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all();
                some2 = complexT2.some();
            }
            all1 = b1.bitset;
            some1 = 0;
        } else {
            ComplexSemType complexT1 = (ComplexSemType) t1;
            all1 = complexT1.all();
            some1 = complexT1.some();
            if (t2 instanceof BasicTypeBitSet b2) {
                all2 = b2.bitset;
                some2 = 0;
            } else {
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all();
                some2 = complexT2.some();
            }
        }

        BasicTypeBitSet all = BasicTypeBitSet.from(all1 | all2);
        BasicTypeBitSet some = BasicTypeBitSet.from((some1 | some2) & ~all.bitset);
        if (some.bitset == 0) {
            return PredefinedType.basicTypeUnion(all.bitset);
        }

        List<BasicSubtype> subtypes = new ArrayList<>();

        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            BasicTypeCode code = pair.basicTypeCode;
            SubtypeData data1 = pair.subtypeData1;
            SubtypeData data2 = pair.subtypeData2;

            SubtypeData data;
            if (data1 == null) {
                data = data2; // // [from original impl] if they are both null, something's gone wrong
            } else if (data2 == null) {
                data = data1;
            } else {
                data = OpsTable.OPS[code.code].union(data1, data2);
            }

            if (data instanceof AllOrNothingSubtype  allOrNothingSubtype && allOrNothingSubtype.isAllSubtype()) {
                int c = code.code;
                all = BasicTypeBitSet.from(all.bitset | 1 << c);
            } else {
                // data cannot be false since data1 and data2 are not both false
                subtypes.add(BasicSubtype.from(code, (ProperSubtypeData) data));
            }
        }

        if (subtypes.isEmpty()) {
            return all;
        }
        return ComplexSemType.createComplexSemType(all.bitset, subtypes);
    }

    public static SemType intersect(SemType t1, SemType t2) {
        int all1, all2, some1, some2;

        if (t1 instanceof BasicTypeBitSet b1) {
            if (t2 instanceof BasicTypeBitSet b2) {
                return BasicTypeBitSet.from(b1.bitset & b2.bitset);
            } else {
                if (b1.bitset == 0) {
                    return t1;
                }
                if (b1.bitset == VT_MASK) {
                    return t2;
                }
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all();
                some2 = complexT2.some();
            }
            all1 = b1.bitset;
            some1 = 0;
        } else {
            ComplexSemType complexT1 = (ComplexSemType) t1;
            all1 = complexT1.all();
            some1 = complexT1.some();
            if (t2 instanceof BasicTypeBitSet b2) {
                if (b2.bitset == 0) {
                    return t2;
                }
                if (b2.bitset == VT_MASK) {
                    return t1;
                }
                all2 = b2.bitset;
                some2 = 0;
            } else {
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all();
                some2 = complexT2.some();
            }
        }

        BasicTypeBitSet all = BasicTypeBitSet.from(all1 & all2);
        BasicTypeBitSet some = BasicTypeBitSet.from((some1 | all1) & (some2 | all2));
        some = BasicTypeBitSet.from(some.bitset & ~all.bitset);
        if (some.bitset == 0) {
            return PredefinedType.basicTypeUnion(all.bitset);
        }

        List<BasicSubtype> subtypes = new ArrayList<>();

        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            BasicTypeCode code = pair.basicTypeCode;
            SubtypeData data1 = pair.subtypeData1;
            SubtypeData data2 = pair.subtypeData2;

            SubtypeData data;
            if (data1 == null) {
                data = data2;
            } else if (data2 == null) {
                data = data1;
            } else {
                data = OpsTable.OPS[code.code].intersect(data1, data2);
            }
            if (!(data instanceof AllOrNothingSubtype allOrNothingSubtype) || allOrNothingSubtype.isAllSubtype()) {
                subtypes.add(BasicSubtype.from(code, (ProperSubtypeData) data));
            }
        }
        if (subtypes.isEmpty()) {
            return all;
        }
        return ComplexSemType.createComplexSemType(all.bitset, subtypes);
    }

    public static CellSemType intersectMemberSemTypes(Env env, CellSemType t1, CellSemType t2) {
        CellAtomicType c1 = cellAtomicType(t1);
        CellAtomicType c2 = cellAtomicType(t2);
        assert c1 != null && c2 != null;
        CellAtomicType atomicType = intersectCellAtomicType(c1, c2);
        return cellContaining(env, atomicType.ty(), UNDEF.equals(atomicType.ty()) ? CELL_MUT_NONE : atomicType.mut());
    }

    public static SemType complement(SemType t) {
        return diff(VAL, t);
    }

    public static boolean isNever(SemType t) {
        return (t instanceof BasicTypeBitSet b) && b.bitset == 0;
    }

    public static boolean isEmpty(Context cx, SemType t) {
        assert t != null && cx != null;
        if (t instanceof BasicTypeBitSet b) {
            return b.bitset == 0;
        } else {
            ComplexSemType ct = (ComplexSemType) t;
            if (ct.all() != 0) {
                // includes all of, one or more basic types
                return false;
            }
            for (var st : unpackComplexSemType(ct)) {
                if (!OpsTable.OPS[st.basicTypeCode.code].isEmpty(cx, st.subtypeData)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isSubtype(Context cx, SemType t1, SemType t2) {
        return isEmpty(cx, diff(t1, t2));
    }

    public static boolean isSubtypeSimple(SemType t1, BasicTypeBitSet t2) {
        int bits;
        if (t1 instanceof BasicTypeBitSet b1) {
            bits = b1.bitset;
        } else {
            ComplexSemType complexT1 = (ComplexSemType) t1;
            bits = complexT1.all() | complexT1.some();
        }
        return (bits & ~t2.bitset) == 0;
    }

    public static boolean isSameType(Context context, SemType t1, SemType t2) {
        return isSubtype(context, t1, t2) && isSubtype(context, t2, t1);
    }

    public static BasicTypeBitSet widenToBasicTypes(SemType t) {
        if (t instanceof BasicTypeBitSet b) {
            return b;
        } else {
            ComplexSemType complexSemType = (ComplexSemType) t;
            return BasicTypeBitSet.from(complexSemType.all() | complexSemType.some());
        }
    }

    // If t is a non-empty subtype of a built-in unsigned int subtype (Unsigned8/16/32),
    // then return the smallest such subtype. Otherwise, return t.
    public static SemType wideUnsigned(SemType t) {
        if (t instanceof BasicTypeBitSet) {
            return t;
        } else {
            if (!isSubtypeSimple(t, PredefinedType.INT)) {
                return t;
            }
            SubtypeData data = IntSubtype.intSubtypeWidenUnsigned(subtypeData(t, BT_INT));
            if (data instanceof AllOrNothingSubtype) {
                return PredefinedType.INT;
            } else {
                return PredefinedType.basicSubtype(BT_INT, (ProperSubtypeData) data);
            }
        }
    }

    public static SubtypeData booleanSubtype(SemType t) {
        return subtypeData(t, BT_BOOLEAN);
    }

    // Describes the subtype of int included in the type: true/false mean all or none of string
    public static SubtypeData intSubtype(SemType t) {
        return subtypeData(t, BT_INT);
    }

    public static SubtypeData floatSubtype(SemType t) {
        return subtypeData(t, BT_FLOAT);
    }

    public static SubtypeData decimalSubtype(SemType t) {
        return subtypeData(t, BT_DECIMAL);
    }

    // Describes the subtype of string included in the type: true/false mean all or none of string
    public static SubtypeData stringSubtype(SemType t) {
        return subtypeData(t, BT_STRING);
    }

    // This computes the spec operation called "member type of K in T",
    // for the case when T is a subtype of list, and K is either `int` or a singleton int.
    // This is what Castagna calls projection.
    // We will extend this to allow `key` to be a SemType, which will turn into an IntSubtype.
    // If `t` is not a list, NEVER is returned
    public static SemType listMemberTypeInnerVal(Context cx, SemType t, SemType k) {
        if (t instanceof BasicTypeBitSet b) {
            return (b.bitset & LIST.bitset) != 0 ? VAL : NEVER;
        } else {
            SubtypeData keyData = intSubtype(k);
            if (isNothingSubtype(keyData)) {
                return NEVER;
            }
            return bddListMemberTypeInnerVal(cx, (Bdd) getComplexSubtypeData((ComplexSemType) t, BT_LIST), keyData,
                    VAL);
        }
    }

    static final ListMemberTypes LIST_MEMBER_TYPES_ALL = ListMemberTypes.from(
            List.of(Range.from(0, MAX_VALUE)),
            List.of(VAL)
    );

    static final ListMemberTypes LIST_MEMBER_TYPES_NONE = ListMemberTypes.from(List.of(), List.of());

    public static ListMemberTypes listAllMemberTypesInner(Context cx, SemType t) {
        if (t instanceof BasicTypeBitSet b) {
            return (b.bitset & LIST.bitset) != 0 ? LIST_MEMBER_TYPES_ALL : LIST_MEMBER_TYPES_NONE;
        }

        ComplexSemType ct = (ComplexSemType) t;
        List<Range> ranges = new ArrayList<>();
        List<SemType> types = new ArrayList<>();


        Range[] allRanges = bddListAllRanges(cx, (Bdd) getComplexSubtypeData(ct, BT_LIST), new Range[]{});
        for (Range r : allRanges) {
            SemType m = listMemberTypeInnerVal(cx, t, IntSubtype.intConst(r.min));
            if (!NEVER.equals(m)) {
                ranges.add(r);
                types.add(m);
            }
        }
        return ListMemberTypes.from(ranges, types);
    }

    static Range[] bddListAllRanges(Context cx, Bdd b, Range[] accum) {
        if (b instanceof BddAllOrNothing allOrNothing) {
            return allOrNothing.isAll() ? accum : new Range[0];
        } else {
            BddNode bddNode = (BddNode) b;
            ListMemberTypes listMemberTypes = listAtomicTypeAllMemberTypesInnerVal(cx.listAtomType(bddNode.atom()));
            return distinctRanges(bddListAllRanges(cx, bddNode.left(),
                            distinctRanges(listMemberTypes.ranges().toArray(Range[]::new), accum)),
                    distinctRanges(bddListAllRanges(cx, bddNode.middle(), accum),
                            bddListAllRanges(cx, bddNode.right(), accum)));
        }
    }

    static Range[] distinctRanges(Range[] range1, Range[] range2) {
        CombinedRange[] combined = combineRanges(range1, range2);
        Range[] range = new Range[combined.length];
        for (int i = 0; i < combined.length; i++) {
            range[i] = combined[i].range();
        }
        return range;
    }

    // If [r, i1, i2] is included in the result, then
    //    at least one of i1 and i2 are not ()
    //    if i1 is not (), then r is completely included in ranges1[i1]
    //    if i2 is not (), then r is completely included in ranges2[i2]
    // The ranges in the result are ordered and non-overlapping.
    public static CombinedRange[] combineRanges(Range[] ranges1, Range[] ranges2) {
        List<CombinedRange> combined = new ArrayList<>();
        int i1 = 0;
        int i2 = 0;
        int len1 = ranges1.length;
        int len2 = ranges2.length;
        long cur = MIN_VALUE;
        // This iterates over the boundaries between ranges
        while (true) {
            while (i1 < len1 && cur > ranges1[i1].max) {
                i1 += 1;
            }
            while (i2 < len2 && cur > ranges2[i2].max) {
                i2 += 1;
            }

            Long next = null;
            if (i1 < len1) {
                next = nextBoundary(cur, ranges1[i1], next);
            }
            if (i2 < len2) {
                next = nextBoundary(cur, ranges2[i2], next);
            }
            long max = next == null ? MAX_VALUE : next - 1;
            Long in1 = null;
            if (i1 < len1) {
                Range r = ranges1[i1];
                if (cur >= r.min && max <= r.max) {
                    in1 = (long) i1;
                }
            }
            Long in2 = null;
            if (i2 < len2) {
                Range r = ranges2[i2];
                if (cur >= r.min && max <= r.max) {
                    in2 = (long) i2;
                }
            }
            if (in1 != null || in2 != null) {
                combined.add(CombinedRange.from(Range.from(cur, max), in1, in2));
            }
            if (next == null) {
                break;
            }
            cur = next;
        }
        return combined.toArray(CombinedRange[]::new);
    }

    // Helper function for combineRanges
    // Return smallest range boundary that is > cur and <= next
    // null represents int:MAX_VALUE + 1
    static Long nextBoundary(long cur, Range r, Long next) {
        if ((r.min > cur) && (next == null || r.min < next)) {
            return r.min;
        }
        if (r.max != MAX_VALUE) {
            long i = r.max + 1;
            if (i > cur && (next == null || i < next)) {
                return i;
            }
        }
        return next;
    }

    public static ListMemberTypes listAtomicTypeAllMemberTypesInnerVal(ListAtomicType atomicType) {
        List<Range> ranges = new ArrayList<>();
        List<SemType> types = new ArrayList<>();

        List<CellSemType> cellInitial = atomicType.members().initial();
        int initialLength = cellInitial.size();

        List<SemType> initial = new ArrayList<>(initialLength);
        for (CellSemType c : cellInitial) {
            initial.add(cellInnerVal(c));
        }

        int fixedLength = atomicType.members().fixedLength();
        if (initialLength != 0) {
            types.addAll(initial);
            for (int i = 0; i < initialLength; i++) {
                ranges.add(Range.from(i, i));
            }
            if (initialLength < fixedLength) {
                ranges.set(initialLength - 1, Range.from(initialLength - 1, fixedLength - 1));
            }
        }

        SemType rest = cellInnerVal(atomicType.rest());
        if (!Core.isNever(rest)) {
            types.add(rest);
            ranges.add(Range.from(fixedLength, MAX_VALUE));
        }

        return ListMemberTypes.from(ranges, types);
    }

    public static MappingAtomicType mappingAtomicType(Context cx, SemType t) {
        MappingAtomicType mappingAtomicInner = MAPPING_ATOMIC_INNER;
        if (t instanceof BasicTypeBitSet b) {
            return b.bitset == MAPPING.bitset ? mappingAtomicInner : null;
        } else {
            Env env = cx.env;
            if (!isSubtypeSimple(t, MAPPING)) {
                return null;
            }
            return bddMappingAtomicType(env,
                                       (Bdd) getComplexSubtypeData((ComplexSemType) t, BT_MAPPING),
                                       mappingAtomicInner);
        }
    }

    private static MappingAtomicType bddMappingAtomicType(Env env, Bdd bdd, MappingAtomicType top) {
        if (bdd instanceof BddAllOrNothing allOrNothing) {
            if (allOrNothing.isAll()) {
                return top;
            }
            return null;
        }
        BddNode bddNode = (BddNode) bdd;
        if (bddNode instanceof BddNodeSimple bddNodeSimple) {
            return env.mappingAtomType(bddNodeSimple.atom());
        }
        return null;
    }

    public static SemType mappingMemberTypeInnerVal(Context cx, SemType t, SemType k) {
        return diff(mappingMemberTypeInner(cx, t, k), UNDEF);
    }

    // This computes the spec operation called "member type of K in T",
    // for when T is a subtype of mapping, and K is either `string` or a singleton string.
    // This is what Castagna calls projection.
    public static SemType mappingMemberTypeInner(Context cx, SemType t, SemType k) {
        if (t instanceof BasicTypeBitSet b) {
            return (b.bitset & MAPPING.bitset) != 0 ? VAL : UNDEF;
        } else {
            SubtypeData keyData = stringSubtype(k);
            if (isNothingSubtype(keyData)) {
                return UNDEF;
            }
            return bddMappingMemberTypeInner(cx, (Bdd) getComplexSubtypeData((ComplexSemType) t, BT_MAPPING), keyData,
                                            INNER);
        }
    }

    public static ListAtomicType listAtomicType(Context cx, SemType t) {
        ListAtomicType listAtomicInner = LIST_ATOMIC_INNER;
        if (t instanceof BasicTypeBitSet b) {
            return b.bitset == LIST.bitset ? listAtomicInner : null;
        } else {
            Env env = cx.env;
            if (!isSubtypeSimple(t, LIST)) {
                return null;
            }
            return bddListAtomicType(env,
                    (Bdd) getComplexSubtypeData((ComplexSemType) t, BT_LIST),
                    listAtomicInner);
        }
    }

    private static ListAtomicType bddListAtomicType(Env env, Bdd bdd, ListAtomicType top) {
        if (bdd instanceof BddAllOrNothing allOrNothing) {
            if (allOrNothing.isAll()) {
                return top;
            }
            return null;
        }
        BddNode bddNode = (BddNode) bdd;
        if (bddNode instanceof BddNodeSimple bddNodeSimple) {
            return env.listAtomType(bddNodeSimple.atom());
        }
        return null;
    }

    public static SemType cellInnerVal(CellSemType t) {
        return diff(cellInner(t), UNDEF);
    }

    public static SemType cellInner(CellSemType t) {
        CellAtomicType cat = cellAtomicType(t);
        assert cat != null;
        return cat.ty();
    }

    public static CellSemType cellContainingInnerVal(Env env, CellSemType t) {
        CellAtomicType cat = cellAtomicType(t);
        assert cat != null;
        return cellContaining(env, diff(cat.ty(), UNDEF), cat.mut());
    }

    public static CellAtomicType cellAtomicType(SemType t) {
        if (t instanceof BasicTypeBitSet) {
            return PredefinedType.CELL.equals(t) ? CELL_ATOMIC_VAL : null;
        } else {
            if (!isSubtypeSimple(t, PredefinedType.CELL)) {
                return null;
            }
            return bddCellAtomicType((Bdd) getComplexSubtypeData((ComplexSemType) t, BT_CELL), CELL_ATOMIC_VAL);
        }
    }

    static CellAtomicType bddCellAtomicType(Bdd bdd, CellAtomicType top) {
        if (bdd instanceof BddAllOrNothing allOrNothing) {
            if (allOrNothing.isAll()) {
                return top;
            }
            return null;
        }
        BddNode bddNode = (BddNode) bdd;
        if (bddNode.left().equals(BddAllOrNothing.bddAll()) &&
                bddNode.middle().equals(BddAllOrNothing.bddNothing()) &&
                bddNode.right().equals(BddAllOrNothing.bddNothing())) {
            return cellAtomType(bddNode.atom());
        }
        return null;
    }

    public static Optional<Value> singleShape(SemType t) {
        if (PredefinedType.NIL.equals(t)) {
            return Optional.of(Value.from(null));
        } else if (t instanceof BasicTypeBitSet) {
            return Optional.empty();
        } else if (isSubtypeSimple(t, PredefinedType.INT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_INT);
            Optional<Long> value = IntSubtype.intSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get()));
        } else if (isSubtypeSimple(t, PredefinedType.FLOAT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_FLOAT);
            Optional<Double> value = FloatSubtype.floatSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get()));
        } else if (isSubtypeSimple(t, PredefinedType.STRING)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_STRING);
            Optional<String> value = StringSubtype.stringSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get()));
        } else if (isSubtypeSimple(t, PredefinedType.BOOLEAN)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_BOOLEAN);
            Optional<Boolean> value = BooleanSubtype.booleanSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get()));
        } else if (isSubtypeSimple(t, PredefinedType.DECIMAL)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_DECIMAL);
            Optional<BigDecimal> value = DecimalSubtype.decimalSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get().toString()));
        }
        return Optional.empty();
    }

    public static SemType singleton(Object v) {
        if (v == null) {
            return PredefinedType.NIL;
        }

        if (v instanceof Long lng) {
            return IntSubtype.intConst(lng);
        } else if (v instanceof Double d) {
            return FloatSubtype.floatConst(d);
        } else if (v instanceof String s) {
            return StringSubtype.stringConst(s);
        } else if (v instanceof Boolean b) {
            return BooleanSubtype.booleanConst(b);
        } else {
            throw new IllegalStateException("Unsupported type: " + v.getClass().getName());
        }
    }

    public static boolean containsConst(SemType t, Object v) {
        if (v == null) {
            return containsNil(t);
        } else if (v instanceof Long lng) {
            return containsConstInt(t, lng);
        } else if (v instanceof Double d) {
            return containsConstFloat(t, d);
        } else if (v instanceof String s) {
            return containsConstString(t, s);
        } else if (v instanceof Boolean b) {
            return containsConstBoolean(t, b);
        } else {
            return containsConstDecimal(t, (BigDecimal) v);
        }
    }

    public static boolean containsNil(SemType t) {
        if (t instanceof BasicTypeBitSet b) {
            return (b.bitset & (1 << BT_NIL.code)) != 0;
        } else {
            // todo: Need to verify this behavior
            AllOrNothingSubtype complexSubtypeData =
                    (AllOrNothingSubtype) getComplexSubtypeData((ComplexSemType) t, BT_NIL);
            return complexSubtypeData.isAllSubtype();
        }
    }


    public static boolean containsConstString(SemType t, String s) {
        if (t instanceof BasicTypeBitSet b) {
            return (b.bitset & (1 << BT_STRING.code)) != 0;
        } else {
            return StringSubtype.stringSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, BT_STRING), s);
        }
    }

    public static boolean containsConstInt(SemType t, long n) {
        if (t instanceof BasicTypeBitSet b) {
            return (b.bitset & (1 << BT_INT.code)) != 0;
        } else {
            return IntSubtype.intSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, BT_INT), n);
        }
    }

    public static boolean containsConstFloat(SemType t, double n) {
        if (t instanceof BasicTypeBitSet b) {
            return (b.bitset & (1 << BT_FLOAT.code)) != 0;
        } else {
            return FloatSubtype.floatSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, BT_FLOAT), EnumerableFloat.from(n));
        }
    }

    public static boolean containsConstDecimal(SemType t, BigDecimal n) {
        if (t instanceof BasicTypeBitSet b) {
            return (b.bitset & (1 << BT_DECIMAL.code)) != 0;
        } else {
            return DecimalSubtype.decimalSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, BT_DECIMAL), EnumerableDecimal.from(n));
        }
    }

    public static boolean containsConstBoolean(SemType t, boolean bool) {
        if (t instanceof BasicTypeBitSet b) {
            return (b.bitset & (1 << BT_BOOLEAN.code)) != 0;
        } else {
            return BooleanSubtype.booleanSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, BT_BOOLEAN), bool);
        }
    }

    public static Optional<BasicTypeBitSet> singleNumericType(SemType semType) {
        SemType numType = intersect(semType, PredefinedType.NUMBER);
        if (numType instanceof BasicTypeBitSet b) {
            if (b.bitset == NEVER.bitset) {
                return Optional.empty();
            }
        }
        if (isSubtypeSimple(numType, PredefinedType.INT)) {
            return Optional.of(PredefinedType.INT);
        }
        if (isSubtypeSimple(numType, PredefinedType.FLOAT)) {
            return Optional.of(PredefinedType.FLOAT);
        }
        if (isSubtypeSimple(numType, PredefinedType.DECIMAL)) {
            return Optional.of(PredefinedType.DECIMAL);
        }
        return Optional.empty();
    }

    public static SubtypeData subtypeData(SemType s, BasicTypeCode code) {
        if (s instanceof BasicTypeBitSet b) {
            if ((b.bitset & (1 << code.code)) != 0) {
                return AllOrNothingSubtype.createAll();
            }
            return AllOrNothingSubtype.createNothing();
        } else {
            return getComplexSubtypeData((ComplexSemType) s, code);
        }
    }

    public static Context typeCheckContext(Env env) {
        return Context.from(env);
    }

    public static SemType createJson(Context context) {
        SemType memo = context.jsonMemo;
        Env env = context.env;

        if (memo != null) {
            return memo;
        }
        ListDefinition listDef = new ListDefinition();
        MappingDefinition mapDef = new MappingDefinition();
        SemType j = union(PredefinedType.SIMPLE_OR_STRING, union(listDef.getSemType(env), mapDef.getSemType(env)));
        listDef.defineListTypeWrapped(env, j);
        mapDef.defineMappingTypeWrapped(env, new ArrayList<>(), j);
        context.jsonMemo = j;
        return j;
    }

    public static SemType createAnydata(Context context) {
        SemType memo = context.anydataMemo;
        Env env = context.env;

        if (memo != null) {
            return memo;
        }
        ListDefinition listDef = new ListDefinition();
        MappingDefinition mapDef = new MappingDefinition();
        SemType tableTy = TableSubtype.tableContaining(env, mapDef.getSemType(env));
        SemType ad = union(union(SIMPLE_OR_STRING, union(XML, union(REGEXP, tableTy))),
                union(listDef.getSemType(env), mapDef.getSemType(env)));
        listDef.defineListTypeWrapped(env, ad);
        mapDef.defineMappingTypeWrapped(env, new ArrayList<>(), ad);
        context.anydataMemo = ad;
        return ad;
    }

    public static SemType createCloneable(Context context) {
        SemType memo = context.cloneableMemo;
        Env env = context.env;

        if (memo != null) {
            return memo;
        }
        ListDefinition listDef = new ListDefinition();
        MappingDefinition mapDef = new MappingDefinition();
        SemType tableTy = TableSubtype.tableContaining(env, mapDef.getSemType(env));
        SemType ad = union(VAL_READONLY, union(XML, union(listDef.getSemType(env), union(tableTy,
                mapDef.getSemType(env)))));
        listDef.defineListTypeWrapped(env, ad);
        mapDef.defineMappingTypeWrapped(env, new ArrayList<>(), ad);
        context.cloneableMemo = ad;
        return ad;
    }

    public static SemType createIsolatedObject(Context context) {
        SemType memo = context.isolatedObjectMemo;
        if (memo != null) {
            return memo;
        }

        ObjectQualifiers quals = new ObjectQualifiers(true, false, ObjectQualifiers.NetworkQualifier.None);
        SemType isolatedObj = new ObjectDefinition().define(context.env, quals, Collections.emptyList());
        context.isolatedObjectMemo = isolatedObj;
        return isolatedObj;
    }

    public static SemType createServiceObject(Context context) {
        SemType memo = context.serviceObjectMemo;
        if (memo != null) {
            return memo;
        }

        ObjectQualifiers quals = new ObjectQualifiers(false, false, ObjectQualifiers.NetworkQualifier.Service);
        SemType serviceObj = new ObjectDefinition().define(context.env, quals, Collections.emptyList());
        context.serviceObjectMemo = serviceObj;
        return serviceObj;
    }

    public static SemType createBasicSemType(BasicTypeCode typeCode, SubtypeData subtypeData) {
        if (subtypeData instanceof AllOrNothingSubtype) {
            if (Common.isAllSubtype(subtypeData)) {
                return BasicTypeBitSet.from(1 << typeCode.code);
            } else {
                return BasicTypeBitSet.from(0);
            }
        } else {
            return ComplexSemType.createComplexSemType(0,
                    BasicSubtype.from(typeCode, (ProperSubtypeData) subtypeData));
        }
    }

    // ------------------------- Newly Introduced APIs (Does not exist in nBallerina) --------------------------------

    // Consider map<T1>|map<T2>|...|map<Tn>. This API will return all MappingAtomicTypes in the union.
    public static Optional<List<MappingAtomicType>> mappingAtomicTypesInUnion(Context cx, SemType t) {
        ArrayList<MappingAtomicType> matList = new ArrayList<>();
        MappingAtomicType mappingAtomicInner = MAPPING_ATOMIC_INNER;
        if (t instanceof BasicTypeBitSet b) {
            if (b.bitset == MAPPING.bitset) {
                matList.add(mappingAtomicInner);
                return Optional.of(matList);
            }
            return Optional.empty();
        } else {
            Env env = cx.env;
            if (!isSubtypeSimple(t, MAPPING)) {
                return Optional.empty();
            }
            return collectBddMappingAtomicTypesInUnion(env,
                    (Bdd) getComplexSubtypeData((ComplexSemType) t, BT_MAPPING),
                    mappingAtomicInner, matList) ? Optional.of(matList) : Optional.empty();
        }
    }

    private static boolean collectBddMappingAtomicTypesInUnion(Env env, Bdd bdd, MappingAtomicType top,
                                                               List<MappingAtomicType> matList) {
        if (bdd instanceof BddAllOrNothing allOrNothing) {
            if (allOrNothing.isAll()) {
                matList.add(top);
                return true;
            }
            return false;
        }
        BddNode bddNode = (BddNode) bdd;
        if (bddNode instanceof BddNodeSimple bddNodeSimple) {
            matList.add(env.mappingAtomType(bddNodeSimple.atom()));
            return true;
        }

        BddNodeImpl bddNodeImpl = (BddNodeImpl) bddNode;
        if (bddNodeImpl.left() instanceof BddAllOrNothing leftNode && leftNode.isAll() &&
                bddNodeImpl.right() instanceof BddAllOrNothing rightNode && rightNode.isNothing()) {
            matList.add(env.mappingAtomType(bddNodeImpl.atom()));
            return collectBddMappingAtomicTypesInUnion(env, bddNodeImpl.middle(), top, matList);
        }

        return false;
    }
}
