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
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BTupleType;
import org.wso2.ballerinalang.util.Lists;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Native implementation of lang.regexp:search(string).
 *
 * @since 1.2.0
 */
public class FindGroups {

    static BTupleType tupleType = new BTupleType(Lists.of(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_INT,
            PredefinedTypes.TYPE_STRING));
    static BArrayType groupType = new BArrayType(tupleType);
    static BArrayType groupArrayType = new BArrayType(groupType);

    public static BArray findGroups(BRegexpValue regExp, BString str, int startIndex) {
        Pattern pattern = Pattern.compile(StringUtils.getStringValue(regExp, null));
        Matcher matcher = pattern.matcher(str.getValue());
        BArray resultArray = ValueCreator.createArrayValue(groupType);
        while (matcher.find()) {
            int matcherStart = matcher.start();
            if (matcherStart >= startIndex) {
                int matcherEnd = matcher.end();
                String matcherStr = matcher.group();
                System.out.print("Start index: " + matcherStart);
                System.out.print(" End index: " + matcherEnd);
                System.out.println(" Found: " + matcherStr);

                BArray resultTuple = ValueCreator.createTupleValue(tupleType);
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
        String stringVal = str.getValue();
        Matcher matcher = Pattern.compile(StringUtils.getStringValue(regExp, null)).matcher(stringVal);
        BArray groupArray = ValueCreator.createArrayValue(groupArrayType);
        int matchCount = 0;
        while (matcher.find()) {
            matchCount++;
            System.out.printf("Match count: %s, Group Zero Text: '%s'%n", matchCount,
                    matcher.group());
            BArray group = ValueCreator.createArrayValue(groupType);
            for (int i = 1; i <= matcher.groupCount(); i++) {
                int matcherStart = matcher.start();
                int matcherEnd = matcher.end();
                String matcherStr = matcher.group();

                BArray resultTuple = ValueCreator.createTupleValue(tupleType);
                resultTuple.add(0, matcherStart);
                resultTuple.add(1, matcherEnd);
                resultTuple.add(2, StringUtils.fromString(matcherStr));

                group.append(resultTuple);

                System.out.printf("Capture Group Number: %s, Captured Text: '%s' Start: '%s' Enc: '%s' %n", i,
                        matcher.group(i), matcher.start(i), matcher.end(i));
            }
            groupArray.append(group);
        }
        if (groupArray.getLength() == 0) {
            return null;
        }
        return groupArray;
    }
}
