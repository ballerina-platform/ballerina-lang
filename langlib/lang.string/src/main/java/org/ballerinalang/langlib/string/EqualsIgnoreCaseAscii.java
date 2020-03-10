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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Locale;

/**
 * Extern function lang.string:equalsIgnoreCase(string, string).
 *
 * @since 1.2
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.string", functionName = "equalsIgnoreCaseAscii",
        args = {@Argument(name = "str1", type = TypeKind.STRING), @Argument(name = "str2", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)

public class EqualsIgnoreCaseAscii {
    private static CharsetDecoder decoder;

    static {
        decoder = Charset.forName("US-ASCII").newDecoder();
    }

    public static boolean equalsIgnoreCaseAscii(Strand strand, String str1, String str2) {
        if (str1.length() != str2.length()) {
            return false;
        }

        for (int i = 0; i <str1.length(); i++) {
            String charFromOne = Character.toString(str1.charAt(i));
            String charFromTwo = Character.toString(str2.charAt(i));
            if (!(isPureAscii(charAtIfromOne) && isPureAscii(charAtIfromTwo))) {
                if (!charAtIfromOne.equals(charAtIfromTwo)) {
                    return false;
                }
            }
            if (!charAtIfromOne.equalsIgnoreCase(charAtIfromTwo)) {
                return false;
            }

        }
        return true;
    }

    private static boolean isPureAscii(String  str) {
        byte byteArray[] = str.getBytes();
        try {
            CharBuffer r = decoder.decode(ByteBuffer.wrap(byteArray));
            r.toString();
        } catch (CharacterCodingException e) {
            return false;
        }

        return true;
    }
}
