/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.regexp;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;
import io.ballerina.runtime.internal.errors.ErrorReasons;
import io.ballerina.runtime.internal.regexp.RegExpFactory;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.values.NonBmpStringValue;
import io.ballerina.runtime.internal.values.RegExpValue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Util class used by lang.regexp.
 *
 * @since 2201.3.0
 */
public class RegexUtil {
    static final BTupleType SPAN_AS_TUPLE_TYPE = new BTupleType(List.of(PredefinedTypes.TYPE_INT,
            PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_STRING));

    static final BArrayType GROUPS_AS_SPAN_ARRAY_TYPE = new BArrayType(SPAN_AS_TUPLE_TYPE);

    static final BArrayType GROUPS_ARRAY_TYPE = new BArrayType(GROUPS_AS_SPAN_ARRAY_TYPE);
    static Matcher getMatcher(BRegexpValue regexpVal, BString inputStr) {
        try {
            return getMatcher(regexpVal, inputStr.getValue());
        } catch (PatternSyntaxException e) {
            throw ErrorHelper.getRuntimeException(ErrorReasons.REG_EXP_PARSING_ERROR,
                    ErrorCodes.REGEXP_INVALID_PATTERN, e.getMessage());
        }
    }

    static int[] getSurrogatePositions(BString str) {
        if (str instanceof NonBmpStringValue nonBmpStringValue) {
            return nonBmpStringValue.getSurrogates();
        }
        return new int[0];
    }

    static int getSurrogateAdjustedStartIndex(int startIndex, int[] surrogates) {
        int newStartIndex = startIndex;
        for (int surrogate : surrogates) {
            if (startIndex != 0 && surrogate < startIndex) {
                newStartIndex++;
            } else if (surrogate > startIndex) {
                break;
            }
        }
        return newStartIndex;
    }

    static Matcher getMatcher(BRegexpValue regexpVal, String inputStr) {
        // Map the required ballerina regexp constructs to java.
        RegExpValue translatedRegExpVal = RegExpFactory.translateRegExpConstructs((RegExpValue) regexpVal);
        String patternStr = StringUtils.getStringValue(translatedRegExpVal);
        Pattern pattern = Pattern.compile(patternStr, Pattern.UNICODE_CHARACTER_CLASS);
        return pattern.matcher(inputStr);
    }

    static BArray getGroupZeroAsSpan(BString str, Matcher matcher, int[] surrogates) {
        BArray resultTuple = ValueCreator.createTupleValue(SPAN_AS_TUPLE_TYPE);
        int[] adjustedPositions = getAdjustedPositions(str, matcher, surrogates);
        resultTuple.add(0, adjustedPositions[0]);
        resultTuple.add(1, adjustedPositions[1]);
        resultTuple.add(2, StringUtils.fromString(matcher.group()));
        return resultTuple;
    }

    static int[] getAdjustedPositions(BString str, Matcher matcher, int[] surrogates) {
        BString subString = StringUtils.fromString(matcher.group());
        return getAdjustedPositions(str, matcher.start(), subString, surrogates);
    }

    static int[] getAdjustedPositions(BString str, int startIndex, BString subString, int[] surrogates) {
        int newStartIndex = startIndex;
        int prevSurrogate = 0;
        for (int surrogate : surrogates) {
            long offset = str.indexOf(subString, prevSurrogate);
            prevSurrogate = surrogate;
            if (offset != surrogate && surrogate < startIndex) {
                newStartIndex--;
            } else if (surrogate > startIndex) {
                break;
            }
        }
        int newEndIndex = newStartIndex + subString.length();
        return new int[]{newStartIndex, newEndIndex};
    }

    static BArray getMatcherGroupsAsSpanArr(BString str, Matcher matcher, int[] surrogates) {
        BArray group = ValueCreator.createArrayValue(GROUPS_AS_SPAN_ARRAY_TYPE);
        BArray span = getGroupZeroAsSpan(str, matcher, surrogates);
        group.append(span);
        if (matcher.groupCount() == 0) {
            return group;
        }
        for (int i = 1; i <= matcher.groupCount(); i++) {
            int matcherStart = matcher.start(i);
            if (matcherStart == -1) {
                continue;
            }
            BArray resultTuple = ValueCreator.createTupleValue(SPAN_AS_TUPLE_TYPE);
            BString subString = StringUtils.fromString(matcher.group(i));
            int[] adjustedPositions = getAdjustedPositions(str, matcherStart, subString, surrogates);
            resultTuple.add(0, adjustedPositions[0]);
            resultTuple.add(1, adjustedPositions[1]);
            resultTuple.add(2, subString);
            group.append(resultTuple);
        }
        return group;
    }

    public static BString substring(BString value, long startIndex, long endIndex) {
        return value.substring((int) startIndex, (int) endIndex);
    }

    public static long length(BString value) {
        return value.length();
    }

    protected static void checkIndexWithinRange(BString str, long startIndex) {
        if (startIndex != (int) startIndex) {
            throw ErrorHelper.getRuntimeException(ErrorReasons.REGEXP_OPERATION_ERROR,
                    ErrorCodes.INDEX_NUMBER_TOO_LARGE, startIndex);
        }

        if (startIndex < 0) {
            throw ErrorHelper.getRuntimeException(ErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    ErrorCodes.NEGATIVE_REGEXP_FIND_INDEX);
        }

        int strLength = str.length();
        if (strLength != 0 && strLength <= startIndex) {
            throw ErrorHelper.getRuntimeException(ErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    ErrorCodes.INVALID_REGEXP_FIND_INDEX, startIndex, strLength);
        }
    }
}
