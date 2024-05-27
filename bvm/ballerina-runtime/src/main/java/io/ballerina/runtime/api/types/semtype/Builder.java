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

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.semtype.BBooleanSubType;
import io.ballerina.runtime.internal.types.semtype.BDecimalSubType;
import io.ballerina.runtime.internal.types.semtype.BFloatSubType;
import io.ballerina.runtime.internal.types.semtype.BIntSubType;
import io.ballerina.runtime.internal.types.semtype.BStringSubType;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_B_TYPE;

/**
 * Utility class for creating semtypes.
 *
 * @since 2201.10.0
 */
public final class Builder {

    private static final String[] EMPTY_STRING_ARR = new String[0];

    private Builder() {
    }

    public static SemType from(BasicTypeCode typeCode) {
        if (BasicTypeCache.isCached(typeCode)) {
            return BasicTypeCache.cache[typeCode.code()];
        }
        return SemType.from(1 << typeCode.code());
    }

    public static SemType from(Type type) {
        if (type instanceof SemType semType) {
            return semType;
        } else if (type instanceof BType bType) {
            return from(bType);
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    public static SemType from(BType innerType) {
        return innerType.get();
    }

    public static SemType neverType() {
        return basicTypeUnion(0);
    }

    public static SemType nilType() {
        return from(BasicTypeCode.BT_NIL);
    }

    public static SemType intType() {
        return from(BasicTypeCode.BT_INT);
    }

    public static SemType bType() {
        return from(BasicTypeCode.BT_B_TYPE);
    }

    public static SemType decimalType() {
        return from(BasicTypeCode.BT_DECIMAL);
    }

    public static SemType floatType() {
        return from(BasicTypeCode.BT_FLOAT);
    }

    public static SemType booleanType() {
        return from(BasicTypeCode.BT_BOOLEAN);
    }

    public static SemType stringType() {
        return from(BasicTypeCode.BT_STRING);
    }

    public static SemType charType() {
        return StringTypeCache.charType;
    }

    private static final SemType NEVER = SemType.from(0);

    public static SemType basicTypeUnion(int bitset) {
        // TODO: may be cache single type bit sets as well
        if (bitset == 0) {
            return NEVER;
        } else if (Integer.bitCount(bitset) == 1) {
            int code = Integer.numberOfTrailingZeros(bitset);
            if (BasicTypeCache.isCached(code)) {
                return BasicTypeCache.cache[code];
            }
        }
        return SemType.from(bitset);
    }

    public static SemType basicSubType(BasicTypeCode basicTypeCode, SubType subType) {
        SubType[] subTypes = initializeSubtypeArray();
        subTypes[basicTypeCode.code()] = subType;
        return SemType.from(0, 1 << basicTypeCode.code(), subTypes);
    }

    public static SemType intConst(long value) {
        if (value >= IntTypeCache.CACHE_MIN_VALUE && value <= IntTypeCache.CACHE_MAX_VALUE) {
            return IntTypeCache.cache[(int) value - IntTypeCache.CACHE_MIN_VALUE];
        }
        return createIntSingletonType(value);
    }

    private static SemType createIntSingletonType(long value) {
        List<Long> values = new ArrayList<>(1);
        values.add(value);
        return basicSubType(BasicTypeCode.BT_INT, BIntSubType.createIntSubType(values));
    }

    public static SemType booleanConst(boolean value) {
        return value ? BooleanTypeCache.TRUE : BooleanTypeCache.FALSE;
    }

    public static SemType intRange(long min, long max) {
        return basicSubType(BasicTypeCode.BT_INT, BIntSubType.createIntSubType(min, max));
    }

    public static SemType decimalConst(BigDecimal value) {
        BigDecimal[] values = {value};
        return basicSubType(BasicTypeCode.BT_DECIMAL, BDecimalSubType.createDecimalSubType(true, values));
    }

    public static SemType floatConst(double value) {
        Double[] values = {value};
        return basicSubType(BasicTypeCode.BT_FLOAT, BFloatSubType.createFloatSubType(true, values));
    }

    public static SemType stringConst(String value) {
        BStringSubType subType;
        String[] values = {value};
        String[] empty = EMPTY_STRING_ARR;
        if (value.length() == 1 || value.codePointCount(0, value.length()) == 1) {
            subType = BStringSubType.createStringSubType(true, values, true, empty);
        } else {
            subType = BStringSubType.createStringSubType(true, empty, true, values);
        }
        return basicSubType(BasicTypeCode.BT_STRING, subType);
    }

    static SubType[] initializeSubtypeArray() {
        return new SubType[CODE_B_TYPE + 2];
    }

    public static Optional<SemType> typeOf(Object object) {
        if (object == null) {
            return Optional.of(nilType());
        } else if (object instanceof DecimalValue decimalValue) {
            return Optional.of(decimalConst(decimalValue.value()));
        } else if (object instanceof Double doubleValue) {
            return Optional.of(floatConst(doubleValue));
        } else if (object instanceof Number intValue) {
            long value =
                    intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
            return Optional.of(intConst(value));
        } else if (object instanceof Boolean booleanValue) {
            return Optional.of(booleanConst(booleanValue));
        } else if (object instanceof BString stringValue) {
            return Optional.of(stringConst(stringValue.getValue()));
        }
        return Optional.empty();
    }

    private static final class IntTypeCache {

        private static final int CACHE_MAX_VALUE = 127;
        private static final int CACHE_MIN_VALUE = -128;
        private static final SemType[] cache;
        static {
            cache = new SemType[CACHE_MAX_VALUE - CACHE_MIN_VALUE + 1];
            for (int i = CACHE_MIN_VALUE; i <= CACHE_MAX_VALUE; i++) {
                cache[i - CACHE_MIN_VALUE] = createIntSingletonType(i);
            }
        }
    }

    private static final class BooleanTypeCache {

        private static final SemType TRUE = createBooleanSingletonType(true);
        private static final SemType FALSE = createBooleanSingletonType(false);

        private static SemType createBooleanSingletonType(boolean value) {
            return basicSubType(BasicTypeCode.BT_BOOLEAN, BBooleanSubType.from(value));
        }
    }

    private static final class StringTypeCache {

        private static final SemType charType;
        static {
            BStringSubType subTypeData = BStringSubType.createStringSubType(false, Builder.EMPTY_STRING_ARR, true,
                    Builder.EMPTY_STRING_ARR);
            charType = basicSubType(BasicTypeCode.BT_STRING, subTypeData);
        }
    }

    private static final class BasicTypeCache {

        private static final SemType[] cache;
        static {
            cache = new SemType[CODE_B_TYPE + 2];
            for (int i = 0; i < CODE_B_TYPE + 1; i++) {
                cache[i] = SemType.from(1 << i);
            }
        }

        private static boolean isCached(BasicTypeCode code) {
            int i = code.code();
            return 0 < i && i <= CODE_B_TYPE;
        }

        private static boolean isCached(int code) {
            return 0 < code && code <= CODE_B_TYPE;
        }
    }
}
