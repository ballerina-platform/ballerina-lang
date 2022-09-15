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
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BRegexpValue;
import io.ballerina.runtime.api.values.BString;

import java.util.regex.Matcher;

/**
 * Native implementation of lang.regexp:find(string).
 *
 * @since 2.3.0
 */
public class Find {

    public static BArray find(BRegexpValue regExp, BString str, int startIndex) {
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        if (matcher.find(startIndex)) {
            BArray resultTuple = ValueCreator.createTupleValue(RegexUtil.SPAN_AS_TUPLE_TYPE);
            resultTuple.add(0, matcher.start());
            resultTuple.add(1, matcher.end());
            resultTuple.add(2, StringUtils.fromString(matcher.group()));
            return resultTuple;
        }
        return null;
    }

    public static BArray findGroups(BRegexpValue regExp, BString str, int startIndex) {
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        BArray resultArray = ValueCreator.createArrayValue(RegexUtil.GROUPS_AS_SPAN_ARRAY_TYPE);
        while (matcher.find()) {
            int matcherStart = matcher.start();
            if (matcherStart >= startIndex) {
                int matcherEnd = matcher.end();
                String matcherStr = matcher.group();
                BArray resultTuple = ValueCreator.createTupleValue(RegexUtil.SPAN_AS_TUPLE_TYPE);
                resultTuple.add(0, matcherStart);
                resultTuple.add(1, matcherEnd);
                resultTuple.add(2, StringUtils.fromString(matcherStr));
                resultArray.append(resultTuple);
            }
        }
        if (resultArray.getLength() == 0) {
            return null;
        }
        return resultArray;
    }

    public static BArray findAllGroups(BRegexpValue regExp, BString str, int startIndex) {
        Matcher matcher = RegexUtil.getMatcher(regExp, str);
        matcher.region(startIndex, str.length());
        BArray groupArray = ValueCreator.createArrayValue(RegexUtil.GROUPS_ARRAY_TYPE);
        while (matcher.find()) {
            BArray group = ValueCreator.createArrayValue(RegexUtil.GROUPS_AS_SPAN_ARRAY_TYPE);
            for (int i = 1; i <= matcher.groupCount(); i++) {
                BArray resultTuple = ValueCreator.createTupleValue(RegexUtil.SPAN_AS_TUPLE_TYPE);
                resultTuple.add(0, matcher.start());
                resultTuple.add(1, matcher.end());
                resultTuple.add(2, StringUtils.fromString(matcher.group()));
                group.append(resultTuple);
            }
            if (groupArray.getLength() != 0) {
                groupArray.append(group);
            }
        }
        if (groupArray.getLength() == 0) {
            return null;
        }
        return groupArray;
    }

    public static void print(Object value) {
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println(StringUtils.getStringValue(value, null));
    }
}
