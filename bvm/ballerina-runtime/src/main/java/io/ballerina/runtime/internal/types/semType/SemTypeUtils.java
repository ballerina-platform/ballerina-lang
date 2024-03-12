/*
 *
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 * /
 *
 */

package io.ballerina.runtime.internal.types.semType;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BAnyType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BReadonlyType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BUnionType;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.TypeOperation.union;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.N_TYPES;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BTYPE;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_NIL;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_STRING;

public final class SemTypeUtils {

    public static final class UniformTypeCodes {

        // TODO: make sure the type codes matches
        public final static int UT_NEVER = 0;
        public final static int UT_NIL = 0x01;
        public final static int UT_BOOLEAN = 0x02;
        public final static int UT_STRING = 0x03;
        public final static int UT_BTYPE = 0x04;

        public final static int N_TYPES = 5;
    }

    public static final class SemTypeBuilder {

        public static final BSemType ALL_SEMTYPES = from(UT_NIL | UT_BOOLEAN | UT_STRING);
        public static final BSemType ALL_BTYPE = from(UT_BTYPE);

        public static BSemType from(int uniformTypeCode) {
            if (uniformTypeCode < 0 || uniformTypeCode >= N_TYPES) {
                throw new IllegalStateException("Invalid uniform type code");
            }
            BitSet all = new BitSet(N_TYPES);
            BitSet some = new BitSet(N_TYPES);
            SubType[] subTypeData = new SubType[N_TYPES];
            if (uniformTypeCode != 0) {
                all.set(uniformTypeCode);
            }
            return new BSemType(all, some, subTypeData);
        }

        public static Type booleanSubType(boolean value) {
            BitSet all = new BitSet(N_TYPES);
            BitSet some = new BitSet(N_TYPES);
            SubType[] subTypeData = new SubType[N_TYPES];
            some.set(UT_BOOLEAN);
            subTypeData[UT_BOOLEAN] = new BooleanSubType(value);
            return new BSemType(all, some, subTypeData);
        }

        public static Type stringSubType(boolean charsAllowed, String[] chars, boolean nonCharsAllowed,
                                         String[] nonChars) {
            BitSet all = new BitSet(N_TYPES);
            BitSet some = new BitSet(N_TYPES);
            SubType[] subTypeData = new SubType[N_TYPES];
            some.set(UT_STRING);
            subTypeData[UT_STRING] =
                    StringSubType.createStringSubTypeData(charsAllowed, chars, nonCharsAllowed, nonChars);
            return new BSemType(all, some, subTypeData);
        }

        public static Type stringSubType(String value) {
            return stringSubType(true, new String[0], true, new String[]{value});
        }

        public static Type charSubType(String value) {
            return stringSubType(true, new String[]{value}, true, new String[0]);
        }

        public static BSemType from(BType bType) {
            if (bType == null) {
                throw new IllegalStateException("BType cannot be null");
            }
            if (bType.isEmpty()) {
                return new BSemType();
            }
            if (bType instanceof BFiniteType finiteType) {
                return from(finiteType);
            }
            if (bType instanceof BUnionType unionType && !unionType.getMemberTypes().isEmpty()) {
                return from(unionType);
            }
            BitSet all = new BitSet(N_TYPES);
            BitSet some = new BitSet(N_TYPES);
            some.set(UniformTypeCodes.UT_BTYPE);
            if ((bType instanceof BAnyType) || (bType instanceof BReadonlyType)) {
                all.set(UT_NIL);
                all.set(UT_BOOLEAN);
                all.set(UT_STRING);
            }
            if (bType instanceof BIntersectionType intersectionType) {
                // NOTE: we need to take the effective type break up into bType and semtypes (this can be done by converting
                // effective type to semtype) and intersect semtype and bTypes seperatly
                // This is a hack to make intersection work. If we directly take the effective type we get a stack overflow
                Type effectiveType = intersectionType.getEffectiveType();
                BSemType effectiveSemType = effectiveType instanceof BSemType ? (BSemType) effectiveType : from(
                        (BType) effectiveType);
                all.or(effectiveSemType.all);
                some.or(effectiveSemType.some);
                effectiveSemType = TypeOperation.diff(effectiveSemType, ALL_SEMTYPES);
                effectiveSemType.setIdentifiers(effectiveType.getName(), effectiveType.getPackage());
                String name = intersectionType.getName();
//                if (name != null) {
                bType = new BIntersectionType(name, intersectionType.getPkg(),
                        intersectionType.constituentTypes.toArray(new Type[0]), effectiveSemType,
                        intersectionType.getTypeFlags(), intersectionType.isReadOnly());
//                } else {
//                    bType = new BIntersectionType(intersectionType.getPkg(),
//                            intersectionType.constituentTypes.toArray(new Type[0]), effectiveSemType,
//                            intersectionType.getTypeFlags(), intersectionType.isReadOnly());
//                }
            }
            // TODO: this means we always have an extra null at the beginning
            SubType[] subTypeData = new SubType[N_TYPES];
            subTypeData[UniformTypeCodes.UT_BTYPE] = bType;
            return new BSemType(all, some, subTypeData);
        }

        private static BSemType from(BUnionType unionType) {
            List<Type> members = unionType.getMemberTypes();
            Iterator<Type> it = members.iterator();
            if (!it.hasNext()) {
                // TODO: return never
                throw new IllegalStateException("Union type with no members");
            }
            Type first = it.next();
            BSemType firstSemType = first instanceof BSemType semType ? semType : from((BType) first);
            if (!it.hasNext()) {
                if (first instanceof BSemType semType) {
                    return semType;
                }
                return from((BType) first);
            }
            Type second = it.next();
            BSemType secondSemType = second instanceof BSemType semType ? semType : from((BType) second);
            BSemType result = union(firstSemType, secondSemType);
            while (it.hasNext()) {
                Type other = it.next();
                BSemType otherSemType = other instanceof BSemType semType ? semType : from((BType) other);
                result = union(result, otherSemType);
            }
            return result;
        }

        private static BSemType from(BFiniteType finiteType) {
            BitSet all = new BitSet();
            BitSet some = new BitSet();
            SubType[] subTypeData = new SubType[N_TYPES];
            Set<Object> remainingValues = new HashSet<>();
            Set<String> charValues = new HashSet<>();
            Set<String> nonCharValues = new HashSet<>();
            for (Object value : finiteType.valueSpace) {
                if (value == null) {
                    all.set(UniformTypeCodes.UT_NIL);
                } else if (value instanceof Boolean) {
                    if (all.get(UT_BOOLEAN)) {
                        continue;
                    }
                    boolean val = (Boolean) value;
                    if (some.get(UT_BOOLEAN)) {
                        BooleanSubType current = (BooleanSubType) subTypeData[UT_BOOLEAN];
                        if (current.data instanceof BooleanSubType.BooleanSubTypeData currentData) {
                            if (currentData.value() != val) {
                                some.clear(UT_BOOLEAN);
                                all.set(UT_BOOLEAN);
                            }
                        }
                    } else {
                        some.set(UT_BOOLEAN);
                        subTypeData[UT_BOOLEAN] = new BooleanSubType(val);
                    }
                } else if (value instanceof BString bString) {
                    String stringValue = bString.getValue();
                    if (stringValue.length() > 1) {
                        nonCharValues.add(stringValue);
                    } else {
                        charValues.add(stringValue);
                    }
                } else {
                    remainingValues.add(value);
                }
            }
            if (!remainingValues.isEmpty()) {
                some.set(UT_BTYPE);
                subTypeData[UT_BTYPE] =
                        new BFiniteType(finiteType.getName(), remainingValues, finiteType.getTypeFlags());
            }
            if (!nonCharValues.isEmpty() || !charValues.isEmpty()) {
                some.set(UT_STRING);
                subTypeData[UT_STRING] =
                        StringSubType.createStringSubTypeData(true, charValues.toArray(new String[0]), true,
                                nonCharValues.toArray(new String[0]));
            }
            return new BSemType(all, some, subTypeData);
        }
    }

    public static final class TypeOperation {

        public static BSemType union(BSemType t1, BSemType t2) {
            BSemType semtype = new BSemType();

            semtype.all.or(t1.all);
            semtype.all.or(t2.all);

            semtype.some.or(t1.some);
            semtype.some.or(t2.some);
            semtype.some.andNot(semtype.all);

            SubType[] subTypeData = semtype.subTypeData;
            for (int i = 0; i < N_TYPES; i++) {
                boolean t1Has = t1.some.get(i);
                boolean t2Has = t2.some.get(i);
                if (t1Has && t2Has) {
                    subTypeData[i] = t1.subTypeData[i].union(t2.subTypeData[i]);
                } else if (t1Has) {
                    subTypeData[i] = t1.subTypeData[i];
                } else if (t2Has) {
                    subTypeData[i] = t2.subTypeData[i];
                }
            }
            return semtype;
        }

        public static boolean isNever(Type type) {
            return type instanceof BSemType semType && semType.all.isEmpty() && semType.some.isEmpty();
        }

        public static boolean isEmpty(BSemType t) {
            // FIXME:
            if (!t.all.isEmpty()) {
                return false;
            }
            return t.some.isEmpty();
        }

        public static BSemType intersection(BSemType t1, BSemType t2) {
            if (t2.some.cardinality() != 0 && t2.all.cardinality() != 1 && !t2.all.get(UT_BTYPE)) {
                throw new RuntimeException("proper intersection is not implemented");
            }
            // Currently this can only be used to extract the BType part from a given semtype
            BSemType result = new BSemType();
            for (int i = 0; i < N_TYPES; i++) {
                if (t2.all.get(i) && t1.some.get(i)) {
                    result.some.set(i);
                    result.subTypeData[i] = t1.subTypeData[i];
                }
            }
            return result;
        }

        // t1 / t2
        public static BSemType diff(BSemType t1, BSemType t2) {
            if (t2.some.cardinality() != 0 && t2.all.cardinality() != 1 && !t2.all.get(UT_BTYPE)) {
                throw new RuntimeException("proper diff is not implemented");
            }
            // Currently this can only be used to extract the non BType part from a given semtype

            BSemType result = new BSemType();
            for (int i = 0; i < N_TYPES; i++) {
                if (t2.all.get(i)) {
                    continue;
                }
                if (t1.some.get(i)) {
                    result.some.set(i);
                    result.subTypeData[i] = t1.subTypeData[i];
                }
                if (t1.all.get(i)) {
                    result.all.set(i);
                }
            }
            return result;
        }
    }

    public static boolean belongToSingleUniformType(BSemType semType, int uniformTypeCode) {
        return belongToSingleUniformType(semType) &&
                (semType.some.get(uniformTypeCode) || semType.all.get(uniformTypeCode));
    }

    public static boolean belongToSingleUniformType(BSemType semType) {
        boolean foundType = false;
        for (int i = 0; i < N_TYPES; i++) {
            if (semType.some.get(i) || semType.all.get(i)) {
                if (foundType) {
                    return false;
                }
                foundType = true;
            }
        }
        return true;
    }
}
