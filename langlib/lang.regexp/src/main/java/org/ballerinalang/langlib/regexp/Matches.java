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

import java.util.regex.Matcher;

/**
 * Native implementation of lang.regexp:matches(string).
 *
 * @since 2201.3.0
 */
public class Matches {
    public static BArray matchAt(BRegexpValue regExp, BString str, int startIndex) {
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        matcher.region(startIndex, str.length());
        if (matcher.matches()) {
            return RegexUtil.getGroupZeroAsSpan(matcher);
        }
        return null;
    }

    public static BArray matchGroupsAt(BRegexpValue regExp, BString str, int startIndex) {
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        matcher.region(startIndex, str.length());
        BArray resultArray = null;
        if (matcher.matches()) {
            resultArray = RegexUtil.getMatcherGroupsAsSpanArr(matcher);
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
