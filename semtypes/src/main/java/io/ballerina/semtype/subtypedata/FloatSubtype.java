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

import io.ballerina.semtype.PredefinedType;
import io.ballerina.semtype.ProperSubtypeData;
import io.ballerina.semtype.SemType;
import io.ballerina.semtype.SubtypeData;
import io.ballerina.semtype.UniformTypeCode;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Represent FloatSubtype.
 *
 * @since 2.0.0
 */
public class FloatSubtype implements ProperSubtypeData {

    boolean allowed;
    ArrayList<Double> values;

    public FloatSubtype(boolean allowed, double value) {
        this.allowed = allowed;
        this.values = new ArrayList<>();
        values.add(value);
    }

    public FloatSubtype(boolean allowed, ArrayList<Double> values) {
        this.allowed = allowed;
        this.values = new ArrayList<>(values);
    }

    public static SemType floatConst(double value) {
        return PredefinedType.uniformSubtype(UniformTypeCode.UT_FLOAT, new FloatSubtype(true, value));
    }

    static Optional<Double> floatSubtypeSingleValue(SubtypeData d) {
        if (d instanceof AllOrNothingSubtype) {
            return Optional.empty();
        }

        FloatSubtype f = (FloatSubtype) d;
        if (f.allowed) {
            return Optional.empty();
        }

        ArrayList<Double> values = f.values;
        if (values.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(values.get(0));
    }

    static boolean floatSubtypeContains(SubtypeData d, double f) {
        if (d instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d).isAllSubtype();
        }

        FloatSubtype v = (FloatSubtype) d;
        for (double val : v.values) {
            if (val == f) {
                return v.allowed;
            }
        }
        return !v.allowed;
    }

    static SubtypeData floatSubtypeUnion(SubtypeData d1, SubtypeData d2) {
        ArrayList<Double> values = new ArrayList<>();
        boolean allowed = true; // TODO create enumerable: enumerableSubtypeUnion
        return createFloatSubtype(allowed, values);
    }

    static SubtypeData floatSubtypeIntersect(SubtypeData d1, SubtypeData d2) {
        ArrayList<Double> values = new ArrayList<>();
        boolean allowed = true; // TODO create enumerable: enumerableSubtypeIntersect
        return createFloatSubtype(allowed, values);
    }

    static SubtypeData floatSubtypeDiff(SubtypeData d1, SubtypeData d2) {
        return floatSubtypeIntersect(d1, floatSubtypeComplement(d2));
    }

    static SubtypeData floatSubtypeComplement(SubtypeData d) {
        FloatSubtype s = (FloatSubtype) d;
        return createFloatSubtype(!s.allowed, s.values);
    }

    static SubtypeData createFloatSubtype(boolean allowed, ArrayList<Double> values) {
        if (values.size() == 0) {
            return new AllOrNothingSubtype(!allowed);
        }
        return new FloatSubtype(allowed, values);
    }

}
