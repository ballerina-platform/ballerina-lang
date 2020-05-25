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

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:fromBase16(string).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", version = ARRAY_VERSION, functionName = "fromBase16",
        args = {@Argument(name = "str", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.UNION)},
        isPublic = true
)
public class FromBase16 {

    public static Object fromBase16(Strand strand, BString str) {
        if (str.length() % 2 != 0) {
            return BallerinaErrors
                    .createError("Invalid base16 string",
                                 "Expected an even length string, but the length of the string was: " + str.length());
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
            return BallerinaErrors.createError("Invalid base16 string",
                                               "Invalid character(s): " + invalidChars.toString());
        }

        return new ArrayValueImpl(bytes);
    }
}
