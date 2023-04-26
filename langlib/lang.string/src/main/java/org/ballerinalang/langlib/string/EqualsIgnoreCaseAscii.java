/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.string;

import io.ballerina.runtime.api.values.BString;

import java.nio.charset.StandardCharsets;

/**
 * Extern function lang.string:equalsIgnoreCase(string, string).
 *
 * @since 1.2
 */
public class EqualsIgnoreCaseAscii {

    private EqualsIgnoreCaseAscii() {
    }

    public static boolean equalsIgnoreCaseAscii(BString s1, BString s2) {
        if (s1.length() != s2.length()) {
            return false;
        }
        String str1 = s1.getValue();
        String str2 = s2.getValue();
        for (int i = 0; i < str1.length(); i++) {
            String charFromOne = Character.toString(str1.charAt(i));
            String charFromTwo = Character.toString(str2.charAt(i));
            if (isPureAscii(charFromOne) && isPureAscii(charFromTwo)) {
                if (!charFromOne.equalsIgnoreCase(charFromTwo)) {
                    return false;
                }
            } else if (!charFromOne.equals(charFromTwo)) {
                return false;
            }

        }
        return true;
    }

    private static boolean isPureAscii(String  str) {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(str);
    }
}
