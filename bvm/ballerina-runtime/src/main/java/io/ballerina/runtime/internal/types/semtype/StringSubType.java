/*
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
 */

package io.ballerina.runtime.internal.types.semtype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Runtime representation of StringSubType.
 *
 * @since 2201.10.0
 */
public class StringSubType implements SubType {

    final SubTypeData data;
    private static final StringSubType ALL = new StringSubType(AllOrNothing.ALL);
    private static final StringSubType NOTHING = new StringSubType(AllOrNothing.NOTHING);

    private StringSubType(SubTypeData data) {
        this.data = data;
    }

    static StringSubType createStringSubType(boolean charsAllowed, String[] chars, boolean nonCharsAllowed,
                                             String[] nonChars) {
        if (chars.length == 0 && nonChars.length == 0) {
            if (!charsAllowed && !nonCharsAllowed) {
                return ALL;
            } else if (charsAllowed && nonCharsAllowed) {
                return NOTHING;
            }
        }
        Arrays.sort(chars);
        Arrays.sort(nonChars);
        ValueData charValues = new ValueData(charsAllowed, chars);
        ValueData nonCharValues = new ValueData(nonCharsAllowed, nonChars);
        StringSubTypeData data = new StringSubTypeData(charValues, nonCharValues);
        return new StringSubType(data);
    }

    // TODO: returning the "correct" default value break some tests investigate
    Optional<String> defaultValue() {
        if (data instanceof AllOrNothing) {
            return Optional.of("");
        }
        StringSubTypeData stringData = (StringSubTypeData) data;
        ValueData chars = stringData.chars;
        ValueData nonChars = stringData.nonChars;
        if (chars.allowed && chars.values.length == 1 && nonChars.allowed && nonChars.values.length == 0) {
            return Optional.of(chars.values[0]);
        } else if (nonChars.allowed && nonChars.values.length == 1 && chars.allowed && chars.values.length == 0) {
            return Optional.of(nonChars.values[0]);
        } else if (includeEmptyString()) {
            return Optional.of("");
        }
        return Optional.empty();
    }

    private boolean includeEmptyString() {
        if (data instanceof AllOrNothing) {
            return data == AllOrNothing.ALL;
        }
        StringSubTypeData stringData = (StringSubTypeData) data;
        ValueData nonChars = stringData.nonChars;
        List<String> nonCharValues = List.of(nonChars.values);
        return (nonChars.allowed && nonCharValues.contains("")) || (!nonChars.allowed && !nonCharValues.contains(""));
    }

    @Override
    public String toString() {
        if (data instanceof StringSubTypeData stringSubTypeData) {
            var chars = stringSubTypeData.chars;
            var nonChars = stringSubTypeData.nonChars;
            if (chars.allowed && chars.values.length > 0 && nonChars.allowed && nonChars.values.length == 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < chars.values.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(chars.values[i]);
                }
                return sb.toString();
            } else if (nonChars.allowed && nonChars.values.length > 0 && chars.allowed && chars.values.length == 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < nonChars.values.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(nonChars.values[i]);
                }
                return sb.toString();
            }
        }
        return "string";
    }

    @Override
    public SubType union(SubType otherSubType) {
        StringSubType other = (StringSubType) otherSubType;
        // TODO: refactor
        if (this.data instanceof AllOrNothing || other.data instanceof AllOrNothing) {
            if (this.data == AllOrNothing.ALL) {
                return this;
            } else if (other.data == AllOrNothing.ALL) {
                return other;
            } else if (this.data == AllOrNothing.NOTHING) {
                return other;
            } else if (other.data == AllOrNothing.NOTHING) {
                return this;
            }
            throw new IllegalStateException("unreachable");
        }
        StringSubTypeData data = (StringSubTypeData) this.data;
        StringSubTypeData otherData = (StringSubTypeData) other.data;
        List<String> chars = new ArrayList<>();
        boolean charsAllowed = data.chars.union(otherData.chars, chars);
        List<String> nonChars = new ArrayList<>();
        boolean nonCharsAllowed = data.nonChars.union(otherData.nonChars, nonChars);
        return createStringSubType(charsAllowed, chars.toArray(new String[0]), nonCharsAllowed,
                nonChars.toArray(new String[0]));
    }

    @Override
    public SubType intersect(SubType otherSubtype) {
        StringSubType other = (StringSubType) otherSubtype;
        if (this.data instanceof AllOrNothing) {
            if (this.data == AllOrNothing.ALL) {
                return other;
            } else {
                return NOTHING;
            }
        } else if (other.data instanceof AllOrNothing) {
            if (other.data == AllOrNothing.ALL) {
                return this;
            } else {
                return NOTHING;
            }
        }
        StringSubTypeData data = (StringSubTypeData) this.data;
        StringSubTypeData otherData = (StringSubTypeData) other.data;
        List<String> chars = new ArrayList<>();
        boolean charsAllowed = data.chars.intersect(otherData.chars, chars);
        List<String> nonChars = new ArrayList<>();
        boolean nonCharsAllowed = data.nonChars.intersect(otherData.nonChars, nonChars);
        return createStringSubType(charsAllowed, chars.toArray(new String[0]), nonCharsAllowed,
                nonChars.toArray(new String[0]));
    }

    @Override
    public SubType complement() {
        if (data instanceof AllOrNothing) {
            if (data == AllOrNothing.ALL) {
                return NOTHING;
            } else {
                return ALL;
            }
        }
        StringSubTypeData stringData = (StringSubTypeData) data;
        ValueData chars = stringData.chars;
        ValueData nonChars = stringData.nonChars;
        return createStringSubType(!chars.allowed, chars.values, !nonChars.allowed, nonChars.values);
    }

    @Override
    public boolean isEmpty() {
        return data == AllOrNothing.NOTHING;
    }

    record StringSubTypeData(ValueData chars, ValueData nonChars) implements SubTypeData {

    }

    static class ValueData extends EnumerableSubtypeData<String> {

        private final boolean allowed;
        private final String[] values;

        // NOTE: this assumes values are sorted
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
