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

import io.ballerina.types.EnumerableString;
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
 * Represent StringSubtype.
 *
 * @since 2.0.0
 */
public class StringSubtype extends EnumerableSubtype implements ProperSubtypeData {

    public boolean allowed;
    public EnumerableString[] values;

    private StringSubtype(boolean allowed, EnumerableString[] values) {
        this.allowed = allowed;
        this.values = values;
    }

    private StringSubtype(boolean allowed, EnumerableString value) {
        this(allowed, new EnumerableString[]{value});
    }

    public static boolean stringSubtypeContains(SubtypeData d, String s) {
        if (d instanceof AllOrNothingSubtype) {
            return ((AllOrNothingSubtype) d).isAllSubtype();
        }
        StringSubtype v = (StringSubtype) d;

        boolean found = false;
        for (EnumerableString value : v.values) {
            if (value.value.equals(s)) {
                found = true;
                break;
            }
        }

        return found ? v.allowed : !v.allowed;
    }

    public static SubtypeData createStringSubtype(boolean allowed, EnumerableString[] values) {
        if (values.length == 0) {
            return new AllOrNothingSubtype(!allowed);
        }
        return new StringSubtype(allowed, values);
    }

    public static Optional<String> stringSubtypeSingleValues(SubtypeData d) {
        if (d instanceof AllOrNothingSubtype) {
            return Optional.empty();
        }
        StringSubtype s = (StringSubtype) d;
        if (!s.allowed) {
            return Optional.empty();
        }
        EnumerableString[] values = s.values;
        if (values.length != 1) {
            return Optional.empty();
        }
        return Optional.of(values[0].value);
    }

    public static SemType stringConst(String value) {
        return PredefinedType.uniformSubtype(UniformTypeCode.UT_STRING, new StringSubtype(true,
                EnumerableString.from(value)));
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
        StringJoiner j = new StringJoiner(", ", "StringSubtype:" + (allowed ? "allowed[" : "disallowed["), "]");
        for (EnumerableString value : values) {
            j.add(value.value);
        }
        return j.toString();
    }
}
