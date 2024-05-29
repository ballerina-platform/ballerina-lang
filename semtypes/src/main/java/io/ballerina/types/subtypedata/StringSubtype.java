/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types.subtypedata;

import io.ballerina.types.BasicTypeCode;
import io.ballerina.types.EnumerableCharString;
import io.ballerina.types.EnumerableString;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.ProperSubtypeData;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represent StringSubtype.
 *
 * @since 2201.8.0
 */
public class StringSubtype implements ProperSubtypeData {

    private static final EnumerableString[] EMPTY_STRING_ARR = {};
    private static final EnumerableCharString[] EMPTY_CHAR_ARR = {};
    CharStringSubtype charData;
    NonCharStringSubtype nonCharData;

    public CharStringSubtype getChar() {
        return charData;
    }

    public NonCharStringSubtype getNonChar() {
        return nonCharData;
    }

    private StringSubtype(CharStringSubtype charData, NonCharStringSubtype nonCharData) {
        this.charData = charData;
        this.nonCharData = nonCharData;
    }

    public static StringSubtype from(CharStringSubtype chara, NonCharStringSubtype nonChar) {
        return new StringSubtype(chara, nonChar);
    }

    public static boolean stringSubtypeContains(SubtypeData d, String s) {
        if (d instanceof AllOrNothingSubtype allOrNothingSubtype) {
            return allOrNothingSubtype.isAllSubtype();
        }
        StringSubtype st = (StringSubtype) d;
        CharStringSubtype chara = st.charData;
        NonCharStringSubtype nonChar = st.nonCharData;
        if (s.length() == 1) {
            return Arrays.asList(chara.values).contains(EnumerableCharString.from(s)) == chara.allowed;
        }
        return Arrays.asList(nonChar.values).contains(EnumerableString.from(s)) == nonChar.allowed;
    }

    public static SubtypeData createStringSubtype(CharStringSubtype chara, NonCharStringSubtype nonChar) {
        if (chara.values.length == 0 && nonChar.values.length == 0) {
            if (!chara.allowed && !nonChar.allowed) {
                return AllOrNothingSubtype.createAll();
            } else if (chara.allowed && nonChar.allowed) {
                return AllOrNothingSubtype.createNothing();
            }
        }
        return StringSubtype.from(chara, nonChar);
    }

    public static Optional<String> stringSubtypeSingleValue(SubtypeData d) {
        if (d instanceof AllOrNothingSubtype) {
            return Optional.empty();
        }
        StringSubtype st = (StringSubtype) d;
        CharStringSubtype chara = st.charData;
        NonCharStringSubtype nonChar = st.nonCharData;
        int charCount = chara.allowed ? chara.values.length : 2;
        int nonCharCount = nonChar.allowed ? nonChar.values.length : 2;
        if (charCount + nonCharCount == 1) {
            return charCount != 0 ?
                    Optional.of(chara.values[0].value) : Optional.of(nonChar.values[0].value);
        }
        return Optional.empty();
    }

    public static SemType stringConst(String value) {
        CharStringSubtype chara;
        NonCharStringSubtype nonChar;
        if (value.codePointCount(0, value.length()) == 1) {
            chara = CharStringSubtype.from(true,
                    new EnumerableCharString[]{EnumerableCharString.from(value)});
            nonChar = NonCharStringSubtype.from(true, EMPTY_STRING_ARR);
        } else {
            chara = CharStringSubtype.from(true, EMPTY_CHAR_ARR);
            nonChar = NonCharStringSubtype.from(true, new EnumerableString[]{EnumerableString.from(value)});
        }
        return PredefinedType.basicSubtype(BasicTypeCode.BT_STRING, new StringSubtype(chara, nonChar));
    }

    public static SemType stringChar() {
        StringSubtype st = new StringSubtype(
                CharStringSubtype.from(false, EMPTY_CHAR_ARR),
                NonCharStringSubtype.from(true, EMPTY_STRING_ARR));
        return PredefinedType.basicSubtype(BasicTypeCode.BT_STRING, st);
    }

    /**
     * Describes the relationship between a StringSubtype and a list of strings
     * How the StringSubtype covers the list and vice versa.
     * type StringSubtypeListCoverage record {|
     *     // true if the StringSubtype is a subtype of the type containing the strings in the lists
     *     boolean isSubtype;
     *     // contains the index in order of each member of the list that is in the StringSubtype
     *     int[] indices;
     * |};
     */
    public static class StringSubtypeListCoverage {
        public final boolean isSubtype;
        public final int[] indices;

        public StringSubtypeListCoverage(boolean isSubtype, int[] indices) {
            this.isSubtype = isSubtype;
            this.indices = indices;
        }

        public static StringSubtypeListCoverage from(boolean isSubtype, int[] indices) {
            return new StringSubtypeListCoverage(isSubtype, indices);
        }
    }
}
