/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.semtype;

import io.ballerina.semtype.definition.ListDefinition;
import io.ballerina.semtype.definition.MappingDefinition;
import io.ballerina.semtype.subtypedata.AllOrNothingSubtype;
import io.ballerina.semtype.subtypedata.BddAllOrNothing;
import io.ballerina.semtype.subtypedata.BddNode;
import io.ballerina.semtype.subtypedata.BooleanSubtype;
import io.ballerina.semtype.subtypedata.FloatSubtype;
import io.ballerina.semtype.subtypedata.IntSubtype;
import io.ballerina.semtype.subtypedata.StringSubtype;
import io.ballerina.semtype.typeops.SubtypePair;
import io.ballerina.semtype.typeops.SubtypePairs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Contain functions defined in `core.bal` file.
 */
public class Core {
    // subtypeList must be ordered

    public static List<UniformSubtype> unpackComplexSemType(ComplexSemType t) {
        int some = t.some.bitset;
        List<UniformSubtype> subtypeList = new ArrayList<>();
        for (SubtypeData data : t.subtypeDataList) {
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
            some1 = complexT1.all;
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
                subtypes.add(UniformSubtype.from(code, data));
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
                subtypes.add(UniformSubtype.from(code, data));
            }
        }
        if (subtypes.isEmpty()) {
            return all;
        }
        return ComplexSemType.createComplexSemType(all.bitset, subtypes);
    }

    public static SemType roDiff(TypeCheckContext tc, SemType t1, SemType t2) {
        return maybeRoDiff(t1, t2, tc);
    }

    public static SemType diff(SemType t1, SemType t2) {
        return maybeRoDiff(t1, t2, null);
    }

    public static SemType maybeRoDiff(SemType t1, SemType t2, TypeCheckContext tc) {
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
            if (tc == null || code.code < UniformTypeCode.UT_COUNT_RO) {
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
                    if (OpsTable.OPS[code.code].isEmpty(tc, OpsTable.OPS[code.code].diff(data1, data2))) {
                        data = AllOrNothingSubtype.createNothing();
                    } else {
                        data = data1;
                    }
                }
            }
            if (data instanceof AllOrNothingSubtype && ((AllOrNothingSubtype) data).isAllSubtype()) {
                int c = code.code;
                all = UniformTypeBitSet.from(all.bitset | (1 << c));
            } else if (!(data instanceof AllOrNothingSubtype)) {
                subtypes.add(UniformSubtype.from(code, data));
            }
        }
        if (subtypes.isEmpty()) {
            return all;
        }
        return ComplexSemType.createComplexSemType(all.bitset, subtypes);
    }

    public static SemType complement(SemType t) {
        return diff(PredefinedType.TOP, t);
    }

    public static boolean isNever(SemType t) {
        return (t instanceof UniformTypeBitSet) && (((UniformTypeBitSet) t).bitset == 0);
    }

    public static boolean isEmpty(TypeCheckContext tc, SemType t) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset == 0);
        } else {
            ComplexSemType ct = (ComplexSemType) t;
            if (ct.all.bitset != 0) {
                return false;
            }
            for (var st : unpackComplexSemType(ct)) {
                if (!OpsTable.OPS[st.uniformTypeCode.code].isEmpty(tc, st.subtypeData)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean isSubtype(TypeCheckContext tc, SemType t1, SemType t2) {
        return isEmpty(tc, diff(t1, t2));
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

    // If t is a non-empty subtype of a built-in unsigned int subtype (Unsigned8/16/32),
    // then return the smallest such subtype. Otherwise, return t.
    public static SemType wideUnsigned(SemType t) {
        if (t instanceof UniformTypeBitSet) {
            return t;
        } else {
            if (!isSubtypeSimple(t, PredefinedType.INT)) {
                return t;
            }
            SubtypeData data = IntSubtype.intSubtypeWidenUnsigned(subtypeData(t, UniformTypeCode.UT_INT));
            if (data instanceof AllOrNothingSubtype) {
                return PredefinedType.INT;
            } else {
                return PredefinedType.uniformSubtype(UniformTypeCode.UT_INT, (ProperSubtypeData) data);
            }
        }
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
                    Optional.of(PredefinedType.TOP) : Optional.empty();
        } else {
            if (!isSubtypeSimple(t, PredefinedType.LIST)) {
                return Optional.empty();
            }
            Optional<UniformTypeBitSet> rw = bddListSimpleMemberType(env,
                    (Bdd) getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_LIST_RW));
            if (rw.isPresent() && strict) {
                Optional<UniformTypeBitSet> ro = bddListSimpleMemberType(env,
                        (Bdd) getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_LIST_RO));
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
                return Optional.of(PredefinedType.TOP);
            }
        } else {
            BddNode bn = (BddNode) bdd;
            if ((bn.left instanceof BddAllOrNothing && ((BddAllOrNothing) bn.left).isAll())
                    && (bn.middle instanceof BddAllOrNothing && !((BddAllOrNothing) bn.middle).isAll())
                    && (bn.right instanceof BddAllOrNothing && !((BddAllOrNothing) ((BddNode) bdd).right).isAll())) {
                ListAtomicType atomic = env.listAtomType(bn.atom);
                if (atomic.members.length == 0) {
                    SemType memberType = atomic.rest;
                    if (memberType instanceof UniformTypeBitSet) {
                        return Optional.of((UniformTypeBitSet) memberType);
                    }
                }
            }
        }
        return Optional.empty();
    }

    // default strict = false
    public static Optional<UniformTypeBitSet> simpleMapMemberType(Env env, SemType t) {
        return simpleMapMemberType(env, t, false);
    }

    // This is a temporary API that identifies when a SemType corresponds to a type T[]
    // where T is a union of complete basic types.
    public static Optional<UniformTypeBitSet> simpleMapMemberType(Env env, SemType t, boolean strict) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset == PredefinedType.MAPPING.bitset ||
                    (((UniformTypeBitSet) t).bitset == PredefinedType.MAPPING_RW.bitset && !strict)) ?
                    Optional.of(PredefinedType.TOP) : Optional.empty();
        } else {
            if (!isSubtypeSimple(t, PredefinedType.MAPPING)) {
                return Optional.empty();
            }
            Optional<UniformTypeBitSet> rw = bddMappingSimpleMemberType(env,
                    (Bdd) getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_MAPPING_RW));
            if (rw.isPresent() && strict) {
                Optional<UniformTypeBitSet> ro = bddMappingSimpleMemberType(env,
                        (Bdd) getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_MAPPING_RO));
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
                return Optional.of(PredefinedType.TOP);
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
        if (t == PredefinedType.NIL) {
            return Optional.of(Value.from(null));
        } else if (t instanceof UniformTypeBitSet) {
            return Optional.empty();
        } else if (isSubtypeSimple(t, PredefinedType.INT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_INT);
            Optional<Long> value = IntSubtype.intSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value));
        } else if (isSubtypeSimple(t, PredefinedType.FLOAT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_FLOAT);
            Optional<Double> value = FloatSubtype.floatSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value.get()));
        } else if (isSubtypeSimple(t, PredefinedType.STRING)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_STRING);
            Optional<String> value = StringSubtype.stringSubtypeSingleValues(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value));
        } else if (isSubtypeSimple(t, PredefinedType.BOOLEAN)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_BOOLEAN);
            Optional<Boolean> value = BooleanSubtype.booleanSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(Value.from(value));
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
        } else {
            return containsConstBoolean(t, (Boolean) v);
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
            return (((UniformTypeBitSet) t).bitset & (1 << UniformTypeCode.UT_STRING.code)) != 0;
        } else {
            return StringSubtype.stringSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_STRING), s);
        }
    }

    public static boolean containsConstInt(SemType t, long n) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & (1 << UniformTypeCode.UT_INT.code)) != 0;
        } else {
            return IntSubtype.intSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_INT), n);
        }
    }

    public static boolean containsConstFloat(SemType t, double n) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & (1 << UniformTypeCode.UT_FLOAT.code)) != 0;
        } else {
            return FloatSubtype.floatSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_FLOAT), EnumerableFloat.from(n));
        }
    }

    public static boolean containsConstBoolean(SemType t, boolean b) {
        if (t instanceof UniformTypeBitSet) {
            return (((UniformTypeBitSet) t).bitset & (1 << UniformTypeCode.UT_BOOLEAN.code)) != 0;
        } else {
            return BooleanSubtype.booleanSubtypeContains(
                    getComplexSubtypeData((ComplexSemType) t, UniformTypeCode.UT_BOOLEAN), b);
        }
    }

    public static Optional<UniformTypeBitSet> singleNumericType(SemType semType) {
        SemType numType = intersect(semType, PredefinedType.NUMBER);
        if (numType instanceof UniformTypeBitSet) {
            if (((UniformTypeBitSet) numType).bitset == PredefinedType.NEVER.bitset) {
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

    public static TypeCheckContext typeCheckContext(Env env) {
        return new TypeCheckContext(env);
    }

    public static SemType createJson(Env env) {
        ListDefinition listDef = new ListDefinition();
        MappingDefinition mapDef = new MappingDefinition();
        SemType j = union(PredefinedType.SIMPLE_OR_STRING, union(listDef.getSemType(env), mapDef.getSemType(env)));
        listDef.define(env, new ArrayList<>(), j);
        mapDef.define(env, new ArrayList<>(), j);
        return j;
    }
}
