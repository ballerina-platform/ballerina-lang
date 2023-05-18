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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.FAILED_TO_DECODE_BYTES;

/**
 * Extern function lang.string:fromBytes(byte[]).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.string", functionName = "fromBytes",
//        args = {@Argument(name = "bytes", type = TypeKind.ARRAY)},
//        returnType = {@ReturnType(type = TypeKind.UNION)},
//        isPublic = true
//)
public class FromBytes {

    private FromBytes() {
    }

    public static Object fromBytes(BArray bytes) {
        try {
            CharsetDecoder charsetDecoder = StandardCharsets.UTF_8.newDecoder();
            String str = charsetDecoder.decode(ByteBuffer.wrap(bytes.getBytes())).toString();
            charsetDecoder.reset();
            return StringUtils.fromString(str);
        } catch (CharacterCodingException e) {
            return ErrorCreator.createError(FAILED_TO_DECODE_BYTES,
                    BLangExceptionHelper.getErrorDetails(RuntimeErrors.INVALID_UTF_8_BYTE_ARRAY_VALUE));
        }
    }
}
