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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BUnionType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Native implementation of lang.regexp:search(string).
 *
 * @since 1.2.0
 */
public class Find {
    public static BArray find(BString re, BString str, long startIndex) {
        Object[] array = new Object[] {};
        Pattern pattern = Pattern.compile(re.getValue());
        Matcher matcher = pattern.matcher(str.getValue());
        while (matcher.find() && matcher.start() >= startIndex) {
            System.out.print("Start index: " + matcher.start());
            array[0] = matcher.start();
            System.out.print(" End index: " + matcher.end());
            array[1] = matcher.end();
            System.out.println(" Found: " + matcher.group());
            array[2] = matcher.group();
            return ValueCreator.createArrayValue(array,
                    new BArrayType(TypeCreator.createUnionType(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_INT)));
        }
        return ValueCreator.createArrayValue(new BArrayType(TypeCreator.createUnionType(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_INT)));
    }
}
