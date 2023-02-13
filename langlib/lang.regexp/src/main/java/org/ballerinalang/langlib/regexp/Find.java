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
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.util.regex.Matcher;

import static org.ballerinalang.langlib.regexp.RegexUtil.GROUPS_AS_SPAN_ARRAY_TYPE;

/**
 * Native implementation of lang.regexp:find(string).
 *
 * @since 2201.3.0
 */
public class Find {

    public static BArray find(BRegexpValue regExp, BString str, long startIndex) {
        checkIndexWithinRange(str, startIndex);
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        if (matcher.find((int) startIndex)) {
            return RegexUtil.getGroupZeroAsSpan(matcher);
        }
        return null;
    }

    public static BArray findGroups(BRegexpValue regExp, BString str, long startIndex) {
        checkIndexWithinRange(str, startIndex);
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        BArray resultArray = ValueCreator.createArrayValue(GROUPS_AS_SPAN_ARRAY_TYPE);
        matcher.region((int) startIndex, str.length());
        if (matcher.find()) {
            resultArray.append(RegexUtil.getGroupZeroAsSpan(matcher));
            if (matcher.groupCount() != 0) {
                BArray spanArr = RegexUtil.getMatcherGroupsAsSpanArr(matcher);
                for (int i = 0; i < spanArr.getLength(); i++) {
                    resultArray.append(spanArr.get(i));
                }
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
        BArray resultArray = ValueCreator.createArrayValue(GROUPS_AS_SPAN_ARRAY_TYPE);
        matcher.region((int) startIndex, str.length());
        while (matcher.find()) {
            resultArray.append(RegexUtil.getGroupZeroAsSpan(matcher));
        }
        if (resultArray.getLength() == 0) {
            return null;
        }
        return resultArray;
    }

    public static BArray findAllGroups(BRegexpValue regExp, BString str, long startIndex) {
        checkIndexWithinRange(str, startIndex);
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        matcher.region((int) startIndex, str.length());
        BArray groupArray = ValueCreator.createArrayValue(RegexUtil.GROUPS_ARRAY_TYPE);
        while (matcher.find()) {
            BArray group = RegexUtil.getMatcherGroupsAsSpanArr(matcher);
            if (group.getLength() != 0) {
                groupArray.append(group);
            }
        }
        if (groupArray.getLength() == 0) {
            return null;
        }
        return groupArray;
    }

    private static void checkIndexWithinRange(BString str, long startIndex) {
        if (startIndex != (int) startIndex) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.REGEXP_OPERATION_ERROR,
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, startIndex);
        }

        if (startIndex < 0) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    RuntimeErrors.NEGATIVE_REGEXP_FIND_INDEX);
        }

        int strLength = str.length();
        if (strLength <= startIndex) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.INDEX_OUT_OF_RANGE_ERROR,
                    RuntimeErrors.INVALID_REGEXP_FIND_INDEX, startIndex, strLength);
        }
    }
}
