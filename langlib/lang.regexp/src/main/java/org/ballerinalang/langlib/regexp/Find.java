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

import io.ballerina.runtime.api.values.BString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Native implementation of lang.regexp:search(string).
 *
 * @since 1.2.0
 */
public class Find {
    public static void find(BString re, BString str, long startIndex) {
        Pattern pattern = Pattern.compile(re.getValue());
        Matcher matcher = pattern.matcher(str.getValue());
        while (matcher.find() && matcher.start() >= startIndex) {
            System.out.print("Start index: " + matcher.start());
            System.out.print(" End index: " + matcher.end());
            System.out.println(" Found: " + matcher.group());
        }
    }
}
