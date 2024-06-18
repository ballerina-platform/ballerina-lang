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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BString;

import java.util.regex.Matcher;

import static org.ballerinalang.langlib.regexp.RegexUtil.GROUPS_AS_SPAN_ARRAY_TYPE;
import static org.ballerinalang.langlib.regexp.RegexUtil.checkIndexWithinRange;
import static org.ballerinalang.langlib.regexp.RegexUtil.getSurrogateAdjustedStartIndex;
import static org.ballerinalang.langlib.regexp.RegexUtil.getSurrogatePositions;

/**
 * Native implementation of lang.regexp:find(string).
 *
 * @since 2201.3.0
 */
public class Find {

    private Find() {
    }

    public static BArray find(BRegexpValue regExp, BString str, long startIndex) {
        checkIndexWithinRange(str, startIndex);
        int[] surrogates = getSurrogatePositions(str);
        int adjustedStartIndex = getSurrogateAdjustedStartIndex((int) startIndex, surrogates);
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        if (matcher.find(adjustedStartIndex)) {
            return RegexUtil.getGroupZeroAsSpan(str, matcher, surrogates);
        }
        return null;
    }

    public static BArray findGroups(BRegexpValue regExp, BString str, long startIndex) {
        checkIndexWithinRange(str, startIndex);
        int[] surrogates = getSurrogatePositions(str);
        int adjustedStartIndex = getSurrogateAdjustedStartIndex((int) startIndex, surrogates);
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        BArray resultArray = ValueCreator.createArrayValue(GROUPS_AS_SPAN_ARRAY_TYPE);
        matcher.region(adjustedStartIndex, str.getValue().length());
        if (matcher.find()) {
            if (matcher.groupCount() != 0) {
                BArray spanArr = RegexUtil.getMatcherGroupsAsSpanArr(str, matcher, surrogates);
                for (int i = 0; i < spanArr.getLength(); i++) {
                    resultArray.append(spanArr.get(i));
                }
            } else {
                resultArray.append(RegexUtil.getGroupZeroAsSpan(str, matcher, surrogates));
            }
        }
        if (resultArray.getLength() == 0) {
            return null;
        }
        return resultArray;
    }

    public static BArray findAll(BRegexpValue regExp, BString str, long startIndex) {
        checkIndexWithinRange(str, startIndex);
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        int[] surrogates = getSurrogatePositions(str);
        int adjustedStartIndex = getSurrogateAdjustedStartIndex((int) startIndex, surrogates);
        BArray resultArray = ValueCreator.createArrayValue(GROUPS_AS_SPAN_ARRAY_TYPE);
        matcher.region(adjustedStartIndex, str.getValue().length());
        while (matcher.find()) {
            resultArray.append(RegexUtil.getGroupZeroAsSpan(str, matcher, surrogates));
        }
        if (resultArray.getLength() == 0) {
            return null;
        }
        return resultArray;
    }

    public static BArray findAllGroups(BRegexpValue regExp, BString str, long startIndex) {
        checkIndexWithinRange(str, startIndex);
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        int[] surrogates = getSurrogatePositions(str);
        int adjustedStartIndex = getSurrogateAdjustedStartIndex((int) startIndex, surrogates);
        matcher.region(adjustedStartIndex, str.getValue().length());
        BArray groupArray = ValueCreator.createArrayValue(RegexUtil.GROUPS_ARRAY_TYPE);
        while (matcher.find()) {
            BArray group = RegexUtil.getMatcherGroupsAsSpanArr(str, matcher, surrogates);
            if (group.getLength() != 0) {
                groupArray.append(group);
            }
        }
        if (groupArray.getLength() == 0) {
            return null;
        }
        return groupArray;
    }
}
