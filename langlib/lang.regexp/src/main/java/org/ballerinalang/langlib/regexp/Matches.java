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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BString;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;

import static org.ballerinalang.langlib.regexp.RegexUtil.checkIndexWithinRange;
import static org.ballerinalang.langlib.regexp.RegexUtil.getSurrogateAdjustedStartIndex;
import static org.ballerinalang.langlib.regexp.RegexUtil.getSurrogatePositions;

/**
 * Native implementation of lang.regexp:matches(string).
 *
 * @since 2201.3.0
 */
public final class Matches {

    private Matches() {
    }

    @Nullable
    public static BArray matchAt(BRegexpValue regExp, BString str, int startIndex) {
        checkIndexWithinRange(str, startIndex);
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        int[] surrogates = getSurrogatePositions(str);
        int adjustedStartIndex = getSurrogateAdjustedStartIndex(startIndex, surrogates);
        matcher.region(adjustedStartIndex, str.getValue().length());
        if (matcher.matches()) {
            return RegexUtil.getGroupZeroAsSpan(str, matcher, surrogates);
        }
        return null;
    }

    @Nullable
    public static BArray matchGroupsAt(BRegexpValue regExp, BString str, int startIndex) {
        checkIndexWithinRange(str, startIndex);
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        int[] surrogates = getSurrogatePositions(str);
        int adjustedStartIndex = getSurrogateAdjustedStartIndex(startIndex, surrogates);
        matcher.region(adjustedStartIndex, str.getValue().length());
        BArray resultArray = null;
        if (matcher.matches()) {
            resultArray = RegexUtil.getMatcherGroupsAsSpanArr(str, matcher, surrogates);
        }
        if (resultArray == null || resultArray.getLength() == 0) {
            return null;
        }
        return resultArray;
    }

    public static boolean isFullMatch(BRegexpValue regExp, BString str) {
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        return matcher.matches();
    }
}
