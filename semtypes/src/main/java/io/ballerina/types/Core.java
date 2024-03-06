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
import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;
import io.ballerina.types.typeops.SubtypePair;
import io.ballerina.types.typeops.SubtypePairs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.types.Common.isListBitsSet;
import static io.ballerina.types.Common.isNothingSubtype;
import static io.ballerina.types.MappingAtomicType.MAPPING_ATOMIC_TOP;
import static io.ballerina.types.PredefinedType.MAPPING;
import static io.ballerina.types.PredefinedType.MAPPING_RW;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.TOP;
import static io.ballerina.types.UniformTypeCode.UT_BOOLEAN;
import static io.ballerina.types.UniformTypeCode.UT_DECIMAL;
import static io.ballerina.types.UniformTypeCode.UT_FLOAT;
import static io.ballerina.types.UniformTypeCode.UT_INT;
import static io.ballerina.types.UniformTypeCode.UT_LIST_RO;
import static io.ballerina.types.UniformTypeCode.UT_LIST_RW;
import static io.ballerina.types.UniformTypeCode.UT_MAPPING_RO;
import static io.ballerina.types.UniformTypeCode.UT_MAPPING_RW;
import static io.ballerina.types.UniformTypeCode.UT_STRING;
import static io.ballerina.types.typeops.ListCommonOps.bddListMemberType;
import static io.ballerina.types.typeops.MappingCommonOps.bddMappingMemberRequired;
import static io.ballerina.types.typeops.MappingCommonOps.bddMappingMemberType;

/**
 * Contain functions defined in `core.bal` file.
 *
 * @since 2201.8.0
 */
public final class Core {
    // subtypeList must be ordered

    public static List<UniformSubtype> unpackComplexSemType(ComplexSemType t) {
        int some = t.some.bitset;
        List<UniformSubtype> subtypeList = new ArrayList<>();
        for (ProperSubtypeData data : t.subtypeDataList) {
            UniformTypeCode code = UniformTypeCode.from(Integer.numberOfTrailingZeros(some));
            subtypeList.add(UniformSubtype.from(code, data));
            int c = code.code;
            some ^= (1 << c);
        }
        return subtypeList;
    }

    public static SubtypeData getComplexSubtypeData(ComplexSemType t, UniformTypeCode code) {
        int c = code.code;
        c = 1 << c;
        if ((t.all.bitset & c) != 0) {
            return AllOrNothingSubtype.createAll();
        }
        if ((t.some.bitset & c) == 0) {
            return AllOrNothingSubtype.createNothing();
        }
        int loBits = t.some.bitset & (c - 1);
        return t.subtypeDataList[loBits == 0 ? 0 : Integer.bitCount(loBits)];
    }

    public static SemType union(SemType t1, SemType t2) {
        UniformTypeBitSet all1;
        UniformTypeBitSet all2;
        UniformTypeBitSet some1;
        UniformTypeBitSet some2;

        if (t1 instanceof UniformTypeBitSet) {
            if (t2 instanceof UniformTypeBitSet) {
                return UniformTypeBitSet.from(((UniformTypeBitSet) t1).bitset | ((UniformTypeBitSet) t2).bitset);
            } else {
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all;
                some2 = complexT2.some;
            }
            all1 = (UniformTypeBitSet) t1;
            some1 = UniformTypeBitSet.from(0);
        } else {
            ComplexSemType complexT1 = (ComplexSemType) t1;
            all1 = complexT1.all;
            some1 = complexT1.some;
            if (t2 instanceof UniformTypeBitSet) {
                all2 = ((UniformTypeBitSet) t2);
                some2 = UniformTypeBitSet.from(0);
            } else {
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all;
                some2 = complexT2.some;
            }
        }

        UniformTypeBitSet all = UniformTypeBitSet.from(all1.bitset | all2.bitset);
        UniformTypeBitSet some = UniformTypeBitSet.from((some1.bitset | some2.bitset) & ~all.bitset);
        if (some.bitset == 0) {
            return PredefinedType.uniformTypeUnion(all.bitset);
        }

        List<UniformSubtype> subtypes = new ArrayList<>();

        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            UniformTypeCode code = pair.uniformTypeCode;
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

            if (data instanceof AllOrNothingSubtype && ((AllOrNothingSubtype) data).isAllSubtype()) {
                int c = code.code;
                all = UniformTypeBitSet.from(all.bitset | 1 << c);
            } else {
                // data cannot be false since data1 and data2 are not both false
                subtypes.add(UniformSubtype.from(code, (ProperSubtypeData) data));
            }
        }

        if (subtypes.isEmpty()) {
            return all;
        }
        return ComplexSemType.createComplexSemType(all.bitset, subtypes);
    }

    public static SemType intersect(SemType t1, SemType t2) {
        UniformTypeBitSet all1;
        UniformTypeBitSet all2;
        UniformTypeBitSet some1;
        UniformTypeBitSet some2;

        if (t1 instanceof UniformTypeBitSet) {
            if (t2 instanceof UniformTypeBitSet) {
                return UniformTypeBitSet.from(((UniformTypeBitSet) t1).bitset & ((UniformTypeBitSet) t2).bitset);
            } else {
                if (((UniformTypeBitSet) t1).bitset == 0) {
                    return t1;
                }
                if (((UniformTypeBitSet) t1).bitset == UniformTypeCode.UT_MASK) {
                    return t2;
                }
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all;
                some2 = complexT2.some;
            }
            all1 = (UniformTypeBitSet) t1;
            some1 = UniformTypeBitSet.from(0);
        } else {
            ComplexSemType complexT1 = (ComplexSemType) t1;
            all1 = complexT1.all;
            some1 = complexT1.some;
            if (t2 instanceof UniformTypeBitSet) {
                if (((UniformTypeBitSet) t2).bitset == 0) {
                    return t2;
                }
                if (((UniformTypeBitSet) t2).bitset == UniformTypeCode.UT_MASK) {
                    return t1;
                }
                all2 = (UniformTypeBitSet) t2;
                some2 = UniformTypeBitSet.from(0);
            } else {
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all;
                some2 = complexT2.some;
            }
        }

        UniformTypeBitSet all = UniformTypeBitSet.from(all1.bitset & all2.bitset);
        UniformTypeBitSet some = UniformTypeBitSet.from((some1.bitset | all1.bitset) & (some2.bitset | all2.bitset));
        some = UniformTypeBitSet.from(some.bitset & ~all.bitset);
        if (some.bitset == 0) {
            return PredefinedType.uniformTypeUnion(all.bitset);
        }

        List<UniformSubtype> subtypes = new ArrayList<>();

        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            UniformTypeCode code = pair.uniformTypeCode;
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
            if (!(data instanceof AllOrNothingSubtype) || ((AllOrNothingSubtype) data).isAllSubtype()) {
                subtypes.add(UniformSubtype.from(code, (ProperSubtypeData) data));
            }
        }
        if (subtypes.isEmpty()) {
            return all;
        }
        return ComplexSemType.createComplexSemType(all.bitset, subtypes);
    }

    public static SemType roDiff(Context cx, SemType t1, SemType t2) {
        return maybeRoDiff(t1, t2, cx);
    }

    public static SemType diff(SemType t1, SemType t2) {
        return maybeRoDiff(t1, t2, null);
    }

    public static SemType maybeRoDiff(SemType t1, SemType t2, Context cx) {
        UniformTypeBitSet all1;
        UniformTypeBitSet all2;
        UniformTypeBitSet some1;
        UniformTypeBitSet some2;

        if (t1 instanceof UniformTypeBitSet) {
            if (t2 instanceof UniformTypeBitSet) {
                return UniformTypeBitSet.from(((UniformTypeBitSet) t1).bitset & ~((UniformTypeBitSet) t2).bitset);
            } else {
                if (((UniformTypeBitSet) t1).bitset == 0) {
                    return t1;
                }
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all;
                some2 = complexT2.some;
            }
            all1 = (UniformTypeBitSet) t1;
            some1 = UniformTypeBitSet.from(0);
        } else {
            ComplexSemType complexT1 = (ComplexSemType) t1;
            all1 = complexT1.all;
            some1 = complexT1.some;
            if (t2 instanceof UniformTypeBitSet) {
                if (((UniformTypeBitSet) t2).bitset == UniformTypeCode.UT_MASK) {
                    return UniformTypeBitSet.from(0);
                }
                all2 = (UniformTypeBitSet) t2;
                some2 = UniformTypeBitSet.from(0);
            } else {
                ComplexSemType complexT2 = (ComplexSemType) t2;
                all2 = complexT2.all;
                some2 = complexT2.some;
            }
        }

        UniformTypeBitSet all = UniformTypeBitSet.from(all1.bitset & ~(all2.bitset | some2.bitset));
        UniformTypeBitSet some = UniformTypeBitSet.from((all1.bitset | some1.bitset) & ~all2.bitset);
        some = UniformTypeBitSet.from(some.bitset & ~all.bitset);
        if (some.bitset == 0) {
            return PredefinedType.uniformTypeUnion(all.bitset);
        }
        List<UniformSubtype> subtypes = new ArrayList<>();
        for (SubtypePair pair : new SubtypePairs(t1, t2, some)) {
            UniformTypeCode code = pair.uniformTypeCode;
            SubtypeData data1 = pair.subtypeData1;
            SubtypeData data2 = pair.subtypeData2;

            SubtypeData data;
            if (cx == null || code.code < UniformTypeCode.UT_COUNT_RO) {
                // normal diff or read-only uniform type
                if (data1 == null) {
                    data = OpsTable.OPS[code.code].complement(data2);
                } else if (data2 == null) {
                    data = data1;
                } else {
                    data = OpsTable.OPS[code.code].diff(data1, data2);
                }
            } else {
                // read-only diff for mutable uniform type
                if (data1 == null) {
                    // data1 was all
                    data = AllOrNothingSubtype.createAll();
                } else if (data2 == null) {
                    // data was none
                    data = data1;
                } else {
                    if (OpsTable.OPS[code.code].isEmpty(cx, OpsTable.OPS[code.code].diff(data1, data2))) {
                        data = AllOrNothingSubtype.createNothing();
                    } else {
                        data = data1;
                    }
                }
            }
            // JBUG [in nballerina] `data` is not narrowed properly if you swap the order by doing
            // `if data == true {} else if data != false {}`
            if (!(data instanceof AllOrNothingSubtype)) {
                subtypes.add(UniformSubtype.from(code, (ProperSubtypeData) data));
            } else if (((AllOrNothingSubtype) data).isAllSubtype()) {
                int c = code.code;
                all = UniformTypeBitSet.from(all.bitset | (1 << c));
            }
        }
        if (subtypes.isEmpty()) {
            return all;
        }
        return ComplexSemType.createComplexSemType(all.bitset, subtypes);
    }

    public static SemType complement(SemType t) {
        return diff(TOP, t);
    }

    public static boolean isNever(SemType t) {
        return (t instanceof UniformTypeBitSet) && (((UniformTypeBitSet) t).bitset == 0);
    }

    public static boolean isEmpty(Context cx, SemType t) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset == 0);
        } else {
            ComplexSemType ct = (ComplexSemType) t;
            if (ct.all.bitset != 0) {
                // includes all of, one or more uniform types
                return false;
            }
            for (var st : unpackComplexSemType(ct)) {
                if (!OpsTable.OPS[st.uniformTypeCode.code].isEmpty(cx, st.subtypeData)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isSubtype(Context cx, SemType t1, SemType t2) {
        return isEmpty(cx, diff(t1, t2));
    }

    public static boolean isSubtypeSimple(SemType t1, UniformTypeBitSet t2) {
        int bits;
        if (t1 instanceof UniformTypeBitSet) {
            bits = ((UniformTypeBitSet) t1).bitset;
        } else {
            ComplexSemType complexT1 = (ComplexSemType) t1;
            bits = complexT1.all.bitset | complexT1.some.bitset;
        }
        return (bits & ~t2.bitset) == 0;
    }

    public static boolean isSameType(Context context, SemType t1, SemType t2) {
        return isSubtype(context, t1, t2) && isSubtype(context, t2, t1);
    }

    public static UniformTypeBitSet widenToBasicTypes(SemType t) {
        if (t instanceof UniformTypeBitSet uniformTypeBitSet) {
            return uniformTypeBitSet;
        } else {
            ComplexSemType complexSemType = (ComplexSemType) t;
            return UniformTypeBitSet.from(complexSemType.all.bitset | complexSemType.some.bitset);
        }
    }

    // If t is a non-empty subtype of a built-in unsigned int subtype (Unsigned8/16/32),
    // then return the smallest such subtype. Otherwise, return t.
    public static SemType wideUnsigned(SemType t) {
        if (t instanceof UniformTypeBitSet) {
            return t;
        } else {
            if (!isSubtypeSimple(t, PredefinedType.INT)) {
                return t;
            }
            SubtypeData data = IntSubtype.intSubtypeWidenUnsigned(subtypeData(t, UT_INT));
            if (data instanceof AllOrNothingSubtype) {
                return PredefinedType.INT;
            } else {
                return PredefinedType.uniformSubtype(UT_INT, (ProperSubtypeData) data);
            }
        }
    }

    public static SubtypeData booleanSubtype(SemType t) {
        return subtypeData(t, UT_BOOLEAN);
    }

    // Describes the subtype of int included in the type: true/false mean all or none of string
    public static SubtypeData intSubtype(SemType t) {
        return subtypeData(t, UT_INT);
    }

    public static SubtypeData floatSubtype(SemType t) {
        return subtypeData(t, UT_FLOAT);
    }

    public static SubtypeData decimalSubtype(SemType t) {
        return subtypeData(t, UT_DECIMAL);
    }

    // Describes the subtype of string included in the type: true/false mean all or none of string
    public static SubtypeData stringSubtype(SemType t) {
        return subtypeData(t, UT_STRING);
    }

    // default strict = false
    public static Optional<UniformTypeBitSet> simpleArrayMemberType(Env env, SemType t) {
        return simpleArrayMemberType(env, t, false);
    }

    // This is a temporary API that identifies when a SemType corresponds to a type T[]
    // where T is a union of complete basic types.
    // When `strict`, require ro and rw to be consistent; otherwise just consider rw.
    public static Optional<UniformTypeBitSet> simpleArrayMemberType(Env env, SemType t, boolean strict) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset == PredefinedType.LIST.bitset ||
                    (((UniformTypeBitSet) t).bitset == PredefinedType.LIST_RW.bitset && !strict)) ?
                    Optional.of(TOP) : Optional.empty();
        } else {
            if (!isSubtypeSimple(t, PredefinedType.LIST)) {
                return Optional.empty();
            }
            Optional<UniformTypeBitSet> rw = bddListSimpleMemberType(env,
                    (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_LIST_RW));
            if (rw.isPresent() && strict) {
                Optional<UniformTypeBitSet> ro = bddListSimpleMemberType(env,
                        (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_LIST_RO));
                if (ro.isEmpty() || ro.get().bitset != (rw.get().bitset & UniformTypeCode.UT_READONLY)) {
                    return Optional.empty();
                }
            }
            return rw;
        }
    }
    public static Optional<UniformTypeBitSet> bddListSimpleMemberType(Env env, Bdd bdd) {
        if (bdd instanceof BddAllOrNothing) {
            if (((BddAllOrNothing) bdd).isAll()) {
                return Optional.of(TOP);
            }
        } else {
            BddNode bn = (BddNode) bdd;
            if ((bn.left instanceof BddAllOrNothing && ((BddAllOrNothing) bn.left).isAll())
                    && (bn.middle instanceof BddAllOrNothing && !((BddAllOrNothing) bn.middle).isAll())
                    && (bn.right instanceof BddAllOrNothing && !((BddAllOrNothing) ((BddNode) bdd).right).isAll())) {
                ListAtomicType atomic = env.listAtomType(bn.atom);
                if (atomic.members.initial.size() == 0) {
                    SemType memberType = atomic.rest;
                    if (memberType instanceof UniformTypeBitSet) {
                        return Optional.of((UniformTypeBitSet) memberType);
                    }
                }
            }
        }
        return Optional.empty();
    }

    // This computes the spec operation called "member type of K in T",
    // for the case when T is a subtype of list, and K is either `int` or a singleton int.
    // This is what Castagna calls projection.
    // We will extend this to allow `key` to be a SemType, which will turn into an IntSubtype.
    // If `t` is not a list, NEVER is returned
    public static SemType listMemberType(Context cx, SemType t, SemType k) {
        if (t instanceof UniformTypeBitSet) {
            return isListBitsSet((UniformTypeBitSet) t) ? TOP : NEVER;
        } else {
            SubtypeData keyData = intSubtype(k);
            if (isNothingSubtype(keyData)) {
                return NEVER;
            }
            return union(bddListMemberType(cx,
                                           (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_LIST_RO),
                                           keyData,
                                           TOP),
                         bddListMemberType(cx,
                                           (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_LIST_RW),
                                           keyData,
                                           TOP));
        }
    }

    public static MappingAtomicType mappingAtomicTypeRw(Context cx, SemType t) {
        if (t instanceof UniformTypeBitSet) {
            if (((UniformTypeBitSet) t).bitset == MAPPING.bitset
                    || ((UniformTypeBitSet) t).bitset == MAPPING_RW.bitset) {
                return MAPPING_ATOMIC_TOP;
            }
            return null;
        } else {
            Env env = cx.env;
            if (!isSubtypeSimple(t, MAPPING)) {
                return null;
            }
            return bddMappingAtomicType(env,
                                       (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_MAPPING_RW),
                                       MAPPING_ATOMIC_TOP);
        }
    }

    private static MappingAtomicType bddMappingAtomicType(Env env, Bdd bdd, MappingAtomicType top) {
        if (bdd instanceof BddAllOrNothing) {
            if (((BddAllOrNothing) bdd).isAll()) {
                return top;
            }
            return null;
        }
        BddNode bddNode = (BddNode) bdd;
        if (bddNode.left.equals(BddAllOrNothing.bddAll())
                && bddNode.middle.equals(BddAllOrNothing.bddNothing())
                && bddNode.right.equals(BddAllOrNothing.bddNothing())) {
            return env.mappingAtomType(bddNode.atom);
        }
        return null;
    }

    // This computes the spec operation called "member type of K in T",
    // for when T is a subtype of mapping, and K is either `string` or a singleton string.
    // This is what Castagna calls projection.
    public static SemType mappingMemberType(Context cx, SemType t, SemType k) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & MAPPING.bitset) != 0 ? TOP : NEVER;
        } else {
            SubtypeData keyData = stringSubtype(k);
            if (isNothingSubtype(keyData)) {
                return NEVER;
            }
            return union(bddMappingMemberType(cx,
                                             (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_MAPPING_RO),
                                             keyData, TOP),
                         bddMappingMemberType(cx,
                                             (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_MAPPING_RW),
                                              keyData, TOP));
        }
    }

    public static boolean mappingMemberRequired(Context cx, SemType t, SemType k) {
        if (t instanceof UniformTypeBitSet || !(k instanceof ComplexSemType)) {
            return false;
        } else {
            StringSubtype stringSubType = (StringSubtype) getComplexSubtypeData((ComplexSemType) k, UT_STRING);
            return bddMappingMemberRequired(cx,
                                            (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_MAPPING_RW),
                                            stringSubType, false)
                    && bddMappingMemberRequired(cx,
                                                (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_MAPPING_RO),
                                                stringSubType, false);
        }
    }

    // default strict = false
    public static Optional<UniformTypeBitSet> simpleMapMemberType(Env env, SemType t) {
        return simpleMapMemberType(env, t, false);
    }

    // This is a temporary API that identifies when a SemType corresponds to a type T[]
    // where T is a union of complete basic types.
    public static Optional<UniformTypeBitSet> simpleMapMemberType(Env env, SemType t, boolean strict) {
        if (t instanceof UniformTypeBitSet) {
            int bitset = ((UniformTypeBitSet) t).bitset;
            return (bitset == MAPPING.bitset || (bitset == MAPPING_RW.bitset && !strict))
                    ? Optional.of(TOP)
                    : Optional.empty();
        } else {
            if (!isSubtypeSimple(t, MAPPING)) {
                return Optional.empty();
            }
            Optional<UniformTypeBitSet> rw =
                    bddMappingSimpleMemberType(env, (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_MAPPING_RW));
            if (rw.isPresent() && strict) {
                Optional<UniformTypeBitSet> ro =
                        bddMappingSimpleMemberType(env, (Bdd) getComplexSubtypeData((ComplexSemType) t, UT_MAPPING_RO));
                if (ro.isEmpty() || ro.get().bitset != (rw.get().bitset & UniformTypeCode.UT_READONLY)) {
                    return Optional.empty();
                }
            }
            return rw;
        }
    }

    public static Optional<UniformTypeBitSet> bddMappingSimpleMemberType(Env env, Bdd bdd) {
        if (bdd instanceof BddAllOrNothing) {
            if (((BddAllOrNothing) bdd).isAll()) {
                return Optional.of(TOP);
            }
        } else {
            BddNode bn = (BddNode) bdd;
            if ((bn.left instanceof BddAllOrNothing && ((BddAllOrNothing) bn.left).isAll())
                    && (bn.middle instanceof BddAllOrNothing && !((BddAllOrNothing) bn.middle).isAll())
                    && (bn.right instanceof BddAllOrNothing && !((BddAllOrNothing) ((BddNode) bdd).right).isAll())) {
                MappingAtomicType atomic = env.mappingAtomType(bn.atom);
                if (atomic.names.length == 0) {
                    SemType memberType = atomic.rest;
                    if (memberType instanceof UniformTypeBitSet) {
                        return Optional.of((UniformTypeBitSet) memberType);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<Value> singleShape(SemType t) {
        if (PredefinedType.NIL.equals(t)) {
            return Optional.of(Value.from(null));
        } else if (t instanceof UniformTypeBitSet) {
            return Optional.empty();
        } else if (isSubtypeSimple(t, PredefinedType.INT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_INT);
            Optional<Long> value = IntSubtype.intSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get()));
        } else if (isSubtypeSimple(t, PredefinedType.FLOAT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_FLOAT);
            Optional<Double> value = FloatSubtype.floatSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get()));
        } else if (isSubtypeSimple(t, PredefinedType.STRING)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_STRING);
            Optional<String> value = StringSubtype.stringSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get()));
        } else if (isSubtypeSimple(t, PredefinedType.BOOLEAN)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_BOOLEAN);
            Optional<Boolean> value = BooleanSubtype.booleanSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get()));
        } else if (isSubtypeSimple(t, PredefinedType.DECIMAL)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_DECIMAL);
            Optional<BigDecimal> value = DecimalSubtype.decimalSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get().toString()));
        }
        return Optional.empty();
    }

    public static SemType singleton(Object v) {
        if (v == null) {
            return PredefinedType.NIL;
        }

        if (v instanceof Long) {
            return IntSubtype.intConst((Long) v);
        } else if (v instanceof Double) {
            return FloatSubtype.floatConst((Double) v);
        } else if (v instanceof String) {
            return StringSubtype.stringConst((String) v);
        } else if (v instanceof Boolean) {
            return BooleanSubtype.booleanConst((Boolean) v);
        } else {
            throw new IllegalStateException("Unsupported type: " + v.getClass().getName());
        }
    }

    public static boolean isReadOnly(SemType t) {
        UniformTypeBitSet bits;
        if (t instanceof UniformTypeBitSet) {
            bits = (UniformTypeBitSet) t;
        } else {
            bits = UniformTypeBitSet.from(((ComplexSemType) t).all.bitset | ((ComplexSemType) t).some.bitset);
        }
        return ((bits.bitset & UniformTypeCode.UT_RW_MASK) == 0);
    }

    public static boolean containsConst(SemType t, Object v) {
        if (v == null) {
            return containsNil(t);
        } else if (v instanceof Long) {
            return containsConstInt(t, (Long) v);
        } else if (v instanceof Double) {
            return containsConstFloat(t, (Double) v);
        } else if (v instanceof String) {
            return containsConstString(t, (String) v);
        } else if (v instanceof Boolean) {
            return containsConstBoolean(t, (Boolean) v);
        } else {
            return containsConstDecimal(t, (BigDecimal) v);
        }
    }

    public static boolean containsNil(SemType t) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & (1 << UniformTypeCode.UT_NIL.code)) != 0;
        } else {
            // todo: Need to verify this behavior
            AllOrNothingSubtype complexSubtypeData =
                    (AllOrNothingSubtype) getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_NIL);
            return complexSubtypeData.isAllSubtype();
        }
    }


    public static boolean containsConstString(SemType t, String s) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & (1 << UT_STRING.code)) != 0;
        } else {
            return StringSubtype.stringSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, UT_STRING), s);
        }
    }

    public static boolean containsConstInt(SemType t, long n) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & (1 << UT_INT.code)) != 0;
        } else {
            return IntSubtype.intSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, UT_INT), n);
        }
    }

    public static boolean containsConstFloat(SemType t, double n) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & (1 << UT_FLOAT.code)) != 0;
        } else {
            return FloatSubtype.floatSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, UT_FLOAT), EnumerableFloat.from(n));
        }
    }

    public static boolean containsConstDecimal(SemType t, BigDecimal n) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & (1 << UT_DECIMAL.code)) != 0;
        } else {
            return DecimalSubtype.decimalSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, UT_DECIMAL), EnumerableDecimal.from(n));
        }
    }

    public static boolean containsConstBoolean(SemType t, boolean b) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & (1 << UT_BOOLEAN.code)) != 0;
        } else {
            return BooleanSubtype.booleanSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, UT_BOOLEAN), b);
        }
    }

    public static Optional<UniformTypeBitSet> singleNumericType(SemType semType) {
        SemType numType = intersect(semType, PredefinedType.NUMBER);
        if (numType instanceof UniformTypeBitSet) {
            if (((UniformTypeBitSet) numType).bitset == NEVER.bitset) {
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

    public static SubtypeData subtypeData(SemType s, UniformTypeCode code) {
        if (s instanceof UniformTypeBitSet) {
            int bitset = ((UniformTypeBitSet) s).bitset;
            if ((bitset & (1 << code.code)) != 0) {
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

    public static SemType createJson(Env env) {
        ListDefinition listDef = new ListDefinition();
        MappingDefinition mapDef = new MappingDefinition();
        SemType j = union(PredefinedType.SIMPLE_OR_STRING, union(listDef.getSemType(env), mapDef.getSemType(env)));
        listDef.define(env, new ArrayList<>(), 0,  j);
        mapDef.define(env, new ArrayList<>(), j);
        return j;
    }

    public static SemType createUniformSemType(UniformTypeCode typeCode, SubtypeData subtypeData) {
        if (subtypeData instanceof AllOrNothingSubtype) {
            if (Common.isAllSubtype(subtypeData)) {
                return UniformTypeBitSet.from(1 << typeCode.code);
            } else {
                return UniformTypeBitSet.from(0);
            }
        } else {
            return ComplexSemType.createComplexSemType(0,
                    UniformSubtype.from(typeCode, (ProperSubtypeData) subtypeData));
        }
    }
}
