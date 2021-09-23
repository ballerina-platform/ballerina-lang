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
package io.ballerina.types.subtypedata;

import io.ballerina.types.EnumerableDecimal;
import io.ballerina.types.EnumerableSubtype;
import io.ballerina.types.EnumerableType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeCode;

import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represent DecimalSubtype.
 *
 * @since 2.0.0
 */
public class DecimalSubtype extends EnumerableSubtype implements ProperSubtypeData {
    public boolean allowed;
    public EnumerableDecimal[] values;

    private DecimalSubtype(boolean allowed, EnumerableDecimal value) {
        this(allowed, new EnumerableDecimal[]{value});
    }

    private DecimalSubtype(boolean allowed, EnumerableDecimal[] values) {
        this.allowed = allowed;
        this.values = values;
    }

    public static SemType decimalConst(double value) {
        return PredefinedType.uniformSubtype(UniformTypeCode.UT_DECIMAL, new DecimalSubtype(true,
                EnumerableDecimal.from(value)));
    }

    public static Optional<Double> decimalSubtypeSingleValue(SubtypeData d) {
        if (d instanceof AllOrNothingSubtype) {
            return Optional.empty();
        }

        DecimalSubtype f = (DecimalSubtype) d;
        if (f.allowed) {
            return Optional.empty();
        }

        EnumerableDecimal[] values = f.values;
        if (values.length != 1) {
            return Optional.empty();
        }
        return Optional.of(values[0].value);
    }

    public static boolean decimalSubtypeContains(SubtypeData d, EnumerableDecimal f) {
        if (d instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d).isAllSubtype();
        }

        DecimalSubtype v = (DecimalSubtype) d;
        for (EnumerableType val : v.values) {
            if (val == f) {
                return v.allowed;
            }
        }
        return !v.allowed;
    }

    public static SubtypeData createDecimalSubtype(boolean allowed, EnumerableDecimal[] values) {
        if (values.length == 0) {
            return new AllOrNothingSubtype(!allowed);
        }
        return new DecimalSubtype(allowed, values);
    }

    @Override
    public boolean allowed() {
        return allowed;
    }

    @Override
    public EnumerableType[] values() {
        return values;
    }

    @Override
    public String toString() {
        StringJoiner j = new StringJoiner(", ", "DecimalSubtype:" + (allowed ? "allowed[" : "disallowed["), "]");
        for (EnumerableDecimal value : values) {
            j.add(String.valueOf(value.value));
        }
        return j.toString();
    }
}
