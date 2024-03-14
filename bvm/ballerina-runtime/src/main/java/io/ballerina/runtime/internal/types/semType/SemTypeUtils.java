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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.RuntimeConstants;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BAnyType;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BReadonlyType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static io.ballerina.runtime.internal.types.semType.Core.belongToBasicType;
import static io.ballerina.runtime.internal.types.semType.Core.intersect;
import static io.ballerina.runtime.internal.types.semType.Core.union;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.BT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.BT_BTYPE;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.BT_DECIMAL;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.BT_FLOAT;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.BT_INT;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.BT_NEVER;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.BT_NIL;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.BT_STRING;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.BasicTypeCodes.N_TYPES;

public final class SemTypeUtils {

    public static final BSemType ALL_BTYPE = SemTypeBuilder.from(BT_BTYPE);
    public static final BSemType ALL_SEMTYPE =
            SemTypeBuilder.from(BT_NIL, BT_BOOLEAN, BT_STRING, BT_DECIMAL, BT_FLOAT, BT_INT);

    public static final class BasicTypeCodes {

        // TODO: eventually these codes need to match with the compiler side
        public final static int BT_NEVER = 0;
        public final static int BT_NIL = 0x01;
        public final static int BT_BOOLEAN = 0x02;
        public final static int BT_STRING = 0x03;
        public final static int BT_DECIMAL = 0x04;
        public final static int BT_FLOAT = 0x05;
        public final static int BT_INT = 0x06;
        public final static int BT_BTYPE = 0x07;
        public final static int N_TYPES = 8;
    }

    public static final class SemTypeBuilder {

        public static BSemType from(int... basicTypeCodes) {
            int all = 0;
            int some = 0;
            for (int code : basicTypeCodes) {
                if (code == BT_NEVER) {
                    continue;
                }
                all |= 1 << code;
            }
            SubType[] subTypeData = new SubType[N_TYPES];
            return new BSemType(all, some, subTypeData);
        }

        public static BSemType from(int basicTypeCode) {
            if (basicTypeCode < 0 || basicTypeCode > (1 << N_TYPES)) {
                throw new IllegalStateException("Invalid type code");
            }
            int all = basicTypeCode != BT_NEVER ? 1 << basicTypeCode : 0;
            int some = 0;
            SubType[] subTypeData = new SubType[N_TYPES];
            return new BSemType(all, some, subTypeData);
        }

        public static BSemType booleanSubType(boolean value) {
            int all = 0;
            int some = 1 << BT_BOOLEAN;
            SubType[] subTypeData = new SubType[N_TYPES];
            subTypeData[BT_BOOLEAN] = new BooleanSubType(value);
            return new BSemType(all, some, subTypeData);
        }

        public static BSemType stringSubType(boolean charsAllowed, String[] chars, boolean nonCharsAllowed,
                                             String[] nonChars) {
            int all = 0;
            int some = 1 << BT_STRING;
            SubType[] subTypeData = new SubType[N_TYPES];
            subTypeData[BT_STRING] =
                    StringSubType.createStringSubType(charsAllowed, chars, nonCharsAllowed, nonChars);
            return new BSemType(all, some, subTypeData);
        }

        public static BSemType stringSubType(String value) {
            return stringSubType(true, new String[0], true, new String[]{value});
        }

        public static BSemType charSubType(String value) {
            return stringSubType(true, new String[]{value}, true, new String[0]);
        }

        public static BSemType intSubType(List<Long> values) {
            List<Long> sortedValues = new ArrayList<>(values);
            Collections.sort(sortedValues);
            List<IntSubType.Range> ranges = getRanges(sortedValues);
            int all = 0;
            int some = 1 << BT_INT;
            SubType[] subTypeData = new SubType[N_TYPES];
            subTypeData[BT_INT] = IntSubType.createIntSubType(ranges.toArray(new IntSubType.Range[0]));
            return new BSemType(all, some, subTypeData);
        }

        private static List<IntSubType.Range> getRanges(List<Long> sortedValues) {
            List<IntSubType.Range> ranges = new ArrayList<>();
            long start = sortedValues.get(0);
            long end = start;
            for (int i = 1; i < sortedValues.size(); i++) {
                long value = sortedValues.get(i);
                if (value != end + 1) {
                    ranges.add(new IntSubType.Range(start, end));
                    start = value;
                }
                end = value;
            }
            ranges.add(new IntSubType.Range(start, end));
            return ranges;
        }

        public static BSemType intSubType(Long start, Long end) {
            IntSubType.Range range = new IntSubType.Range(start, end);
            int all = 0;
            int some = 1 << BT_INT;
            SubType[] subTypeData = new SubType[N_TYPES];
            subTypeData[BT_INT] = IntSubType.createIntSubType(new IntSubType.Range[]{range});
            return new BSemType(all, some, subTypeData);
        }

        public static BSemType intSubType(Long value) {
            return intSubType(Collections.singletonList(value));
        }

        public static BSemType decimalSubType(BigDecimal value) {
            int all = 0;
            int some = 1 << BT_DECIMAL;
            SubType[] subTypeData = new SubType[N_TYPES];
            subTypeData[BT_DECIMAL] = DecimalSubType.createDecimalSubType(true, new BigDecimal[]{value});
            return new BSemType(all, some, subTypeData);
        }

        public static BSemType floatSubType(double value) {
            int all = 0;
            int some = 1 << BT_FLOAT;
            SubType[] subTypeData = new SubType[N_TYPES];
            subTypeData[BT_FLOAT] = FloatSubType.createFloatSubType(true, new Double[]{value});
            return new BSemType(all, some, subTypeData);
        }

        private static final int SEMTYPE_MASK =
                (1 << BT_NIL) | (1 << BT_BOOLEAN) | (1 << BT_STRING) | (1 << BT_DECIMAL) | (1 << BT_FLOAT) |
                        (1 << BT_INT);
        // NOTE: this is part of a temperory solution so we don't have to write builders for each type, until a given
        //  BType has been implemented in semtype (at which point you anyway can't create an instance because we remove
        //  the class) we can use this to convert the BType to a SemType.
        public static BSemType from(BType bType) {
            if (bType == null) {
                throw new IllegalStateException("BType cannot be null");
            }
            if (bType.isEmpty()) {
                return from(BT_NEVER);
            }
            if (bType instanceof BFiniteType finiteType) {
                return from(finiteType);
            }
            if (bType instanceof BUnionType unionType && !unionType.getMemberTypes().isEmpty()) {
                return from(unionType);
            }
            int all = 0;
            int some = 0;
            some |= 1 << BT_BTYPE;
            if ((bType instanceof BAnyType) || (bType instanceof BReadonlyType)) {
                all |= SEMTYPE_MASK;
            }
            boolean poisoned = false;
            if (bType instanceof BIntersectionType intersectionType) {
                // NOTE: at this stage conversion between BIntersection type and BSemType is not perfect, since
                //   intersection is an operation (not a type) in SemType world. Currently, this translation is best
                //   effort and as more and more types are converted to SemType, need for this should disappear.
                //   One problem is with types whose effective type has still not ready when we get to this point,
                //   (typically this is shown by an union type without members). If we encounter such a type we consider
                //   such types to be poisoned and user must recalculate the SemType (can be done easily by calling
                //   wrap method in TypeBuilder)
                Type effectiveType = intersectionType.getEffectiveType();
                BSemType effectiveSemType;
                if (effectiveType instanceof BSemType semtype) {
                    if (semtype.poisoned) {
                        BType effectiveBType = semtype.getBType();
                        effectiveSemType = from(effectiveBType);
                    } else {
                        effectiveSemType = semtype;
                    }
                } else {
                    effectiveSemType = from((BType) effectiveType);
                }
                if (effectiveType instanceof BUnionType unionType && unionType.getMemberTypes().isEmpty()) {
                    poisoned = true;
                }
                all |= effectiveSemType.all;
                some |= effectiveSemType.some;
                effectiveSemType = intersect(effectiveSemType, ALL_BTYPE);
                effectiveSemType.setIdentifiers(effectiveType.getName(), effectiveType.getPackage());
                if (poisoned) {
                    effectiveSemType.poisoned = true;
                }
                String name = intersectionType.getName();
                bType = new BIntersectionType(name, intersectionType.getPkg(),
                        intersectionType.constituentTypes.toArray(new Type[0]), effectiveSemType,
                        intersectionType.getTypeFlags(), intersectionType.isReadOnly());
            }
            SubType[] subTypeData = new SubType[N_TYPES];
            subTypeData[BasicTypeCodes.BT_BTYPE] = bType;
            BSemType result = new BSemType(all, some, subTypeData);
            if (poisoned) {
                result.poisoned = true;
            }
            return result;
        }

        private static BSemType from(BUnionType unionType) {
            List<Type> members = unionType.getMemberTypes();
            Iterator<Type> it = members.iterator();
            if (!it.hasNext()) {
                // Can happen with "poisoned" unions described above
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

        private static class ValueAccumulator<E> {

            final Set<E> values = new HashSet<>();
            final int BASIC_TYPE_CODE;
            final Function<ValueAccumulator<E>, SubType> semTypeBuilder;

            ValueAccumulator(int basicTypeCode, Function<ValueAccumulator<E>, SubType> semTypeBuilder) {
                BASIC_TYPE_CODE = basicTypeCode;
                this.semTypeBuilder = semTypeBuilder;
            }

            void addValue(E value) {
                values.add(value);
            }

            boolean applyTo(SubType[] subTypeData) {
                if (values.isEmpty()) {
                    return false;
                }
                subTypeData[BASIC_TYPE_CODE] = semTypeBuilder.apply(this);
                return true;
            }
        }

        private static BSemType from(BFiniteType finiteType) {
            int all = 0;
            int some = 0;
            SubType[] subTypeData = new SubType[N_TYPES];
            ValueAccumulator<Object> bTypeAcc = new ValueAccumulator<>(BT_BTYPE,
                    (acc) -> new BFiniteType(finiteType.getName(), acc.values, finiteType.getTypeFlags()));
            ValueAccumulator<BigDecimal> decimalAcc = new ValueAccumulator<>(BT_DECIMAL,
                    (acc) -> DecimalSubType.createDecimalSubType(true, acc.values.toArray(new BigDecimal[0])));
            ValueAccumulator<Double> floatAcc = new ValueAccumulator<>(BT_FLOAT,
                    (acc) -> FloatSubType.createFloatSubType(true, acc.values.toArray(new Double[0])));
            ValueAccumulator<Long> intAcc = new ValueAccumulator<>(BT_INT,
                    (acc) -> IntSubType.createIntSubType(acc.values.stream().toList()));

            ValueAccumulator[] accumulators = {bTypeAcc, decimalAcc, floatAcc, intAcc};

            Set<String> charValues = new HashSet<>();
            Set<String> nonCharValues = new HashSet<>();
            for (Object value : finiteType.valueSpace) {
                if (value == null) {
                    all |= 1 << BT_NIL;
                } else if (value instanceof Boolean) {
                    if (isSet(all, BT_BOOLEAN)) {
                        continue;
                    }
                    boolean val = (Boolean) value;
                    if (isSet(some, BT_BOOLEAN)) {
                        BooleanSubType current = (BooleanSubType) subTypeData[BT_BOOLEAN];
                        if (current.data instanceof BooleanSubType.BooleanSubTypeData currentData) {
                            if (currentData.value() != val) {
                                some ^= (1 << BT_BOOLEAN);
                                all |= (1 << BT_BOOLEAN);
                            }
                        }
                    } else {
                        some |= 1 << BT_BOOLEAN;
                        subTypeData[BT_BOOLEAN] = new BooleanSubType(val);
                    }
                } else if (value instanceof BString bString) {
                    String stringValue = bString.getValue();
                    if (stringValue.length() > 1) {
                        nonCharValues.add(stringValue);
                    } else {
                        charValues.add(stringValue);
                    }
                } else if (value instanceof DecimalValue decimal) {
                    decimalAcc.addValue(decimal.decimalValue());
                } else if (value instanceof Double floatValue) {
                    floatAcc.addValue(floatValue);
                } else if (value instanceof Number intValue) {
                    intAcc.addValue(intValue.longValue());
                } else {
                    bTypeAcc.addValue(value);
                }
            }
            if (!nonCharValues.isEmpty() || !charValues.isEmpty()) {
                some |= 1 << BT_STRING;
                subTypeData[BT_STRING] =
                        StringSubType.createStringSubType(true, charValues.toArray(new String[0]), true,
                                nonCharValues.toArray(new String[0]));
            }
            for (var acc : accumulators) {
                if (acc.applyTo(subTypeData)) {
                    some |= 1 << acc.BASIC_TYPE_CODE;
                }
            }
            return new BSemType(all, some, subTypeData);
        }
    }

    static <V extends SubType> V getSubType(BSemType semType, int basicTypeCode) {
        return (V) semType.subTypeData[basicTypeCode];
    }

    static <V> V calculateDefaultValue(BSemType semType) {
        // TODO: for string type if we don't consider subtyping we get and error but if we do consider subtyping for
        //  float we get an error. Need to check if this is the expected behavior
        if (belongToBasicType(semType, BT_BTYPE)) {
            return semType.getBType().getZeroValue();
        }
        if (belongToBasicType(semType, BT_BOOLEAN)) {
            return (V) Boolean.valueOf(false);
        } else if (belongToBasicType(semType, BT_STRING)) {
            if (isSet(semType.all, BT_STRING)) {
                return (V) RuntimeConstants.STRING_EMPTY_VALUE;
            } else {
                StringSubType stringSubType = getSubType(semType, BT_STRING);
                return (V) StringUtils.fromString(stringSubType.defaultValue());
            }
        } else if (belongToBasicType(semType, BT_DECIMAL)) {
            return (V) new DecimalValue(BigDecimal.ZERO);
        } else if (belongToBasicType(semType, BT_FLOAT)) {
            return (V) new Double(0);
        } else if (belongToBasicType(semType, BT_INT)) {
            if (isSet(semType.some, BT_INT)) {
                IntSubType intSubType = getSubType(semType, BT_INT);
                if (intSubType.isByte) {
                    return (V) new Integer(0);
                }
            }
            return (V) new Long(0);
        }
        return null;
    }

    static int calculateTag(BSemType semType) {
        // If we have BType
        if (isSet(semType.some, BT_BTYPE)) {
            BTypeComponent bTypeComponent = getSubType(semType, BT_BTYPE);
            if (bTypeComponent instanceof BSubType subTypeData) {
                switch (subTypeData.getTypeClass()) {
                    case BAnyData -> {
                        return TypeTags.ANYDATA_TAG;
                    }
                    case BJson -> {
                        return TypeTags.JSON_TAG;
                    }
                }
            } else if (bTypeComponent instanceof BAnyType) {
                return TypeTags.ANY_TAG;
            } else if (bTypeComponent instanceof BIntersectionType) {
                return TypeTags.INTERSECTION_TAG;
            } else if (bTypeComponent instanceof BJsonType) {
                return TypeTags.JSON_TAG;
            }
            if (cardinality(semType.some) + cardinality(semType.all) > 1) {
                return TypeTags.UNION_TAG;
            } else {
                return bTypeComponent.getBTypeComponent().getTag();
            }
        }
        // Pure SemType
        int nBasicTypes = cardinality(semType.some) + cardinality(semType.all);
        if (nBasicTypes == 0) {
            return TypeTags.NEVER_TAG;
        } else if (nBasicTypes > 1) {
            return TypeTags.UNION_TAG;
        }

        // Single type
        if (belongToBasicType(semType, BT_NIL)) {
            return TypeTags.NULL_TAG;
        } else if (belongToBasicType(semType, BT_BOOLEAN)) {
            return TypeTags.BOOLEAN_TAG;
        } else if (belongToBasicType(semType, BT_STRING)) {
            if (isSet(semType.some, BT_STRING)) {
                StringSubType stringSubType = getSubType(semType, BT_STRING);
                if (stringSubType.data instanceof StringSubType.StringSubTypeData stringSubTypeData) {
                    var chars = stringSubTypeData.chars();
                    var nonChars = stringSubTypeData.nonChars();
                    if (!chars.allowed() && chars.values().length == 0 && nonChars.allowed() &&
                            nonChars.values().length == 0) {
                        return TypeTags.CHAR_STRING_TAG;
                    }
                }
            }
            return TypeTags.STRING_TAG;
        } else if (belongToBasicType(semType, BT_DECIMAL)) {
            return TypeTags.DECIMAL_TAG;
        } else if (belongToBasicType(semType, BT_FLOAT)) {
            return TypeTags.FLOAT_TAG;
        } else if (belongToBasicType(semType, BT_INT)) {
            if (isSet(semType.some, BT_INT)) {
                IntSubType intSubType = getSubType(semType, BT_INT);
                Optional<Integer> subtypeTag = intSubType.getTag();
                if (subtypeTag.isPresent()) {
                    return subtypeTag.get();
                }
            }
            return TypeTags.INT_TAG;
        }
        throw new IllegalStateException("Unable to calculate tag for the given SemType: " + semType);
    }

    public static boolean isSet(int bitset, int index) {
        return (bitset & (1 << index)) != 0;
    }

    public static int cardinality(int bitset) {
        int count = 0;
        while (bitset != 0) {
            count += bitset & 1;
            bitset >>= 1;
        }
        return count;
    }
}
