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

package io.ballerina.runtime.api.types.SemType;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.semtype.BBasicTypeBitSet;
import io.ballerina.runtime.internal.types.semtype.BDecimalSubType;
import io.ballerina.runtime.internal.types.semtype.BFloatSubType;
import io.ballerina.runtime.internal.types.semtype.BIntSubType;
import io.ballerina.runtime.internal.types.semtype.BSemType;
import io.ballerina.runtime.internal.types.semtype.BStringSubType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class Builder {

    private Builder() {
    }

    public static SemType from(BasicTypeCode typeCode) {
        return BBasicTypeBitSet.from(1 << typeCode.code());
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

    public static SemType decimalType() {
        return from(BasicTypeCode.BT_DECIMAL);
    }

    public static SemType basicTypeUnion(int bitset) {
        return BBasicTypeBitSet.from(bitset);
    }

    public static SemType basicSubType(BasicTypeCode basicTypeCode, SubType subType) {
        return BSemType.from(0, 1 << basicTypeCode.code(), List.of(subType));
    }

    public static SemType intConst(long value) {
        List<Long> values = new ArrayList<>(1);
        values.add(value);
        return basicSubType(BasicTypeCode.BT_INT, BIntSubType.createIntSubType(values));
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
        String[] empty = new String[0];
        if (value.codePoints().count() == 1) {
            subType = BStringSubType.createStringSubType(true, values, false, empty);
        } else {
            subType = BStringSubType.createStringSubType(false, empty, true, values);
        }
        return basicSubType(BasicTypeCode.BT_STRING, subType);
    }
}
