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

import java.util.ArrayList;
import java.util.List;

public class StringSubType implements SubType {

    ValueData charValues;
    ValueData nonCharValues;

    private StringSubType(ValueData charValues, ValueData nonCharValues) {
        this.charValues = charValues;
        this.nonCharValues = nonCharValues;
    }

    StringSubType createStringSubTypeData(boolean charsAllowed, String[] chars, boolean nonCharsAllowed,
                                          String[] nonChars) {
        ValueData charValues = new ValueData(charsAllowed, chars);
        ValueData nonCharValues = new ValueData(nonCharsAllowed, nonChars);
        return new StringSubType(charValues, nonCharValues);
    }

    @Override
    public SubType union(SubType other) {
        if (other instanceof StringSubType otherString) {
            List<String> chars = new ArrayList<>();
            List<String> nonChars = new ArrayList<>();
            boolean charsAllowed = charValues.union(otherString.charValues, chars);
            boolean nonCharsAllowed = nonCharValues.union(otherString.nonCharValues, nonChars);
            return createStringSubTypeData(charsAllowed, chars.toArray(new String[0]), nonCharsAllowed,
                    nonChars.toArray(new String[0]));
        } else {
            throw new UnsupportedOperationException("union of different subtypes");
        }
    }

    @Override
    public SubType intersect(SubType other) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public SubType diff(SubType other) {
        return null;
    }

    @Override
    public SubType complement() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private static class ValueData extends EnumerableSubtypeData<String> {

        private final boolean allowed;
        private final String[] values;

        public ValueData(boolean allowed, String[] values) {
            this.allowed = allowed;
            this.values = values;
        }

        @Override
        boolean allowed() {
            return allowed;
        }

        @Override
        String[] values() {
            return values;
        }
    }
}
