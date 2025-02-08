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

import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.SubType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Runtime representation of subtype of string type.
 *
 * @since 2201.11.0
 */
public final class BStringSubType extends SubType {

    final SubTypeData data;
    private static final BStringSubType ALL = new BStringSubType(AllOrNothing.ALL);
    private static final BStringSubType NOTHING = new BStringSubType(AllOrNothing.NOTHING);

    private BStringSubType(SubTypeData data) {
        super(data == AllOrNothing.ALL, data == AllOrNothing.NOTHING);
        this.data = data;
    }

    public static BStringSubType createStringSubType(boolean charsAllowed, String[] chars, boolean nonCharsAllowed,
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
        return new BStringSubType(data);
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
        BStringSubType other = (BStringSubType) otherSubType;
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
        return createStringSubType(charsAllowed, chars.toArray(String[]::new), nonCharsAllowed,
                nonChars.toArray(String[]::new));
    }

    @Override
    public SubType intersect(SubType otherSubtype) {
        BStringSubType other = (BStringSubType) otherSubtype;
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
        return createStringSubType(charsAllowed, chars.toArray(String[]::new), nonCharsAllowed,
                nonChars.toArray(String[]::new));
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
    public boolean isEmpty(Context cx) {
        return data == AllOrNothing.NOTHING;
    }

    @Override
    public SubTypeData data() {
        return data;
    }

    static void stringListIntersect(String[] values, String[] target, List<Integer> indices) {
        int i1 = 0;
        int i2 = 0;
        int len1 = values.length;
        int len2 = target.length;
        while (true) {
            if (i1 >= len1 || i2 >= len2) {
                break;
            } else {
                switch (compareStrings(values[i1], target[i2])) {
                    case EQ:
                        indices.add(i1);
                        i1 += 1;
                        i2 += 1;
                        break;
                    case LT:
                        i1 += 1;
                        break;
                    case GT:
                        i2 += 1;
                        break;
                    default:
                        throw new AssertionError("Invalid comparison value!");
                }
            }
        }
    }

    private static ComparisonResult compareStrings(String s1, String s2) {
        return Objects.equals(s1, s2) ? ComparisonResult.EQ :
                (Common.codePointCompare(s1, s2) ? ComparisonResult.LT : ComparisonResult.GT);
    }

    private enum ComparisonResult {
        EQ,
        LT,
        GT
    }

    record StringSubTypeData(ValueData chars, ValueData nonChars) implements SubTypeData {

        StringSubtypeListCoverage stringSubtypeListCoverage(String[] values) {
            List<Integer> indices = new ArrayList<>();
            ValueData ch = chars();
            ValueData nonChar = nonChars();
            int stringConsts = 0;
            if (ch.allowed) {
                stringListIntersect(values, ch.values, indices);
                stringConsts = ch.values.length;
            } else if (ch.values.length == 0) {
                for (int i = 0; i < values.length; i++) {
                    if (values[i].length() == 1) {
                        indices.add(i);
                    }
                }
            }
            if (nonChar.allowed) {
                stringListIntersect(values, nonChar.values, indices);
                stringConsts += nonChar.values.length;
            } else if (nonChar.values.length == 0) {
                for (int i = 0; i < values.length; i++) {
                    if (values[i].length() != 1) {
                        indices.add(i);
                    }
                }
            }
            int[] inds = indices.stream().mapToInt(i -> i).toArray();
            return new StringSubtypeListCoverage(stringConsts == indices.size(), inds);
        }
    }

    record StringSubtypeListCoverage(boolean isSubType, int[] indices) {

    }

    static final class ValueData extends EnumerableSubtypeData<String> {

        private final boolean allowed;
        private final String[] values;

        // NOTE: this assumes values are sorted
        private ValueData(boolean allowed, String[] values) {
            this.allowed = allowed;
            this.values = values;
        }

        @Override
        public boolean allowed() {
            return allowed;
        }

        @Override
        public String[] values() {
            return values;
        }
    }
}
