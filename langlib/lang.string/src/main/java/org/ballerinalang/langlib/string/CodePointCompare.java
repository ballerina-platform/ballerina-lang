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

package org.ballerinalang.langlib.string;

import io.ballerina.runtime.api.values.BString;

import java.util.PrimitiveIterator;

/**
 * Extern function lang.string:codePointCompare(string, string).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.string", functionName = "codePointCompare",
//        args = {@Argument(name = "str1", type = TypeKind.STRING), @Argument(name = "str2", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.INT)},
//        isPublic = true
//)
public class CodePointCompare {

    public static long codePointCompare(BString str1, BString str2) {
        // Compare each code point of str1 with str2's codepoint at corresponding position.
        // If all all previous codepoints being equal and str1 is exhausted and str2 has more
        // codepoints remain in it then str2 is consider lager.

        PrimitiveIterator.OfInt iterator1 = str1.getValue().codePoints().iterator();
        PrimitiveIterator.OfInt iterator2 = str2.getValue().codePoints().iterator();
        while (iterator1.hasNext()) {
            if (!iterator2.hasNext()) {
                return 1;
            }
            Integer codePoint1 = iterator1.next();
            Integer codePoint2 = iterator2.next();

            int cmp = codePoint1.compareTo(codePoint2);
            if (cmp != 0) {
                return cmp;
            }
        }
        if (iterator2.hasNext()) {
            return -1;
        }
        return 0;
    }
}
