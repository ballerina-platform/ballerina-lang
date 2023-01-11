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
import io.ballerina.runtime.internal.regexp.RegExpFactory;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.values.RegExpValue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return getMatcher(regexpVal, inputStr.getValue());
    }

    static Matcher getMatcher(BRegexpValue regexpVal, String inputStr) {
        // Map the required ballerina regexp constructs to java.
        RegExpValue translatedRegExpVal = RegExpFactory.translateRegExpConstructs((RegExpValue) regexpVal);
        String patternStr = StringUtils.getStringValue(translatedRegExpVal, null);
        Pattern pattern = Pattern.compile(patternStr);
        return pattern.matcher(inputStr);
    }

    static BArray getGroupZeroAsSpan(Matcher matcher) {
        BArray resultTuple = ValueCreator.createTupleValue(SPAN_AS_TUPLE_TYPE);
        resultTuple.add(0, matcher.start());
        resultTuple.add(1, matcher.end());
        resultTuple.add(2, StringUtils.fromString(matcher.group()));
        return resultTuple;
    }

    static BArray getMatcherGroupsAsSpanArr(Matcher matcher) {
        BArray group = ValueCreator.createArrayValue(GROUPS_AS_SPAN_ARRAY_TYPE);
        if (matcher.groupCount() == 0) {
            BArray span = getGroupZeroAsSpan(matcher);
            group.append(span);
            return group;
        }
        for (int i = 1; i <= matcher.groupCount(); i++) {
            int matcherStart = matcher.start(i);
            if (matcherStart == -1) {
                continue;
            }
            BArray resultTuple = ValueCreator.createTupleValue(SPAN_AS_TUPLE_TYPE);
            resultTuple.add(0, matcherStart);
            resultTuple.add(1, matcher.end(i));
            resultTuple.add(2, StringUtils.fromString(matcher.group(i)));
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
}
