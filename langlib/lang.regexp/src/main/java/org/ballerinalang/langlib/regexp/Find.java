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
import io.ballerina.runtime.internal.types.BTupleType;
import org.wso2.ballerinalang.util.Lists;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Native implementation of lang.regexp:search(string).
 *
 * @since 1.2.0
 */
public class Find {
    static BTupleType tupleType = new BTupleType(Lists.of(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_INT,
            PredefinedTypes.TYPE_STRING));
    public static BArray find(BRegexpValue regExp, BString str, int startIndex) {
        Pattern pattern = Pattern.compile(StringUtils.getStringValue(regExp, null));
        Matcher matcher = pattern.matcher(str.getValue());
        if (matcher.find(startIndex)) {
            System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end());
            System.out.println(" Found: " + matcher.group());

            BArray resultTuple = ValueCreator.createTupleValue(tupleType);
            resultTuple.add(0, matcher.start());
            resultTuple.add(1, matcher.end());
            resultTuple.add(2, StringUtils.fromString(matcher.group()));
            return resultTuple;
        }
        return null;
    }

    public static void print(Object value) {
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println(StringUtils.getStringValue(value, null));
    }
}
