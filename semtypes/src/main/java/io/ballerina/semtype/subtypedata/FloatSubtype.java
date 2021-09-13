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
package io.ballerina.semtype.subtypedata;

import io.ballerina.semtype.EnumerableFloat;
import io.ballerina.semtype.EnumerableSubtype;
import io.ballerina.semtype.EnumerableType;
import io.ballerina.semtype.PredefinedType;
import io.ballerina.semtype.ProperSubtypeData;
import io.ballerina.semtype.SemType;
import io.ballerina.semtype.SubtypeData;
import io.ballerina.semtype.UniformTypeCode;

import java.util.Optional;

/**
 * Represent FloatSubtype.
 *
 * @since 2.0.0
 */
public class FloatSubtype extends EnumerableSubtype implements ProperSubtypeData {
    public boolean allowed;
    public EnumerableFloat[] values;

    private FloatSubtype(boolean allowed, EnumerableFloat value) {
        this(allowed, new EnumerableFloat[]{value});
    }

    private FloatSubtype(boolean allowed, EnumerableFloat[] values) {
        this.allowed = allowed;
        this.values = values;
    }

    public static SemType floatConst(double value) {
        return PredefinedType.uniformSubtype(UniformTypeCode.UT_FLOAT, new FloatSubtype(true,
                EnumerableFloat.from(value)));
    }

    public static Optional<Double> floatSubtypeSingleValue(SubtypeData d) {
        if (d instanceof AllOrNothingSubtype) {
            return Optional.empty();
        }

        FloatSubtype f = (FloatSubtype) d;
        if (f.allowed) {
            return Optional.empty();
        }

        EnumerableFloat[] values = f.values;
        if (values.length != 1) {
            return Optional.empty();
        }
        return Optional.of(values[0].value);
    }

    public static boolean floatSubtypeContains(SubtypeData d, EnumerableFloat f) {
        if (d instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d).isAllSubtype();
        }

        FloatSubtype v = (FloatSubtype) d;
        for (EnumerableType val : v.values) {
            if (val == f) {
                return v.allowed;
            }
        }
        return !v.allowed;
    }

    public static SubtypeData createFloatSubtype(boolean allowed, EnumerableFloat[] values) {
        if (values.length == 0) {
            return new AllOrNothingSubtype(!allowed);
        }
        return new FloatSubtype(allowed, values);
    }

    @Override
    public boolean allowed() {
        return allowed;
    }

    @Override
    public EnumerableType[] values() {
        return values;
    }
}
