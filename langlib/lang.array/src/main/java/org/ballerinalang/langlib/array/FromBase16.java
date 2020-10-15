/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.array;

import io.ballerina.runtime.api.ErrorCreator;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;
import java.util.List;

/**
 * Native implementation of lang.array:fromBase16(string).
 *
 * @since 1.0
 */
public class FromBase16 {

    public static Object fromBase16(BString str) {
        if (str.length() % 2 != 0) {
            return ErrorCreator
                    .createError(StringUtils.fromString("Invalid base16 string"),
                                 StringUtils
                                         .fromString("Expected an even length string, but the length of the string" +
                                                             " was: " + str.length()));
        }

        char[] chars = str.getValue().toCharArray();
        byte[] bytes = new byte[chars.length / 2];
        List<Character> invalidChars = new ArrayList<>();

        for (int i = 0, j = 0; i < chars.length; i += 2, j++) {
            int upperHalf = Character.digit(chars[i], 16);
            int lowerHalf = Character.digit(chars[i + 1], 16);

            if (upperHalf < 0) {
                invalidChars.add(chars[i]);
            }

            if (lowerHalf < 0) {
                invalidChars.add(chars[i]);
            }

            bytes[j] = (byte) (upperHalf << 4 | lowerHalf);
        }

        if (!invalidChars.isEmpty()) {
            return ErrorCreator.createError(StringUtils.fromString("Invalid base16 string"),
                                            StringUtils
                                                     .fromString("Invalid character(s): " + invalidChars.toString()));
        }

        return ValueCreator.createArrayValue(bytes);
    }
}
