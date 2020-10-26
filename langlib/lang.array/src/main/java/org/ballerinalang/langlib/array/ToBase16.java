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
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.runtime.util.BLangConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static io.ballerina.runtime.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.array:toBase16(byte[]).
 *
 * @since 1.0
 */
public class ToBase16 {

    private static final char[] chars = "0123456789abcdef".toCharArray();
    private static final BString NOT_SUPPORTED_ERROR_DETAIL = StringUtils.fromString("toBase16() is only supported " +
                                                                                               "on 'byte[]'");
    public static BString toBase16(BArray arr) {
        Type arrType = arr.getType();
        if (arrType.getTag() != TypeTags.ARRAY_TAG ||
                ((ArrayType) arrType).getElementType().getTag() != TypeTags.BYTE_TAG) {
            throw ErrorCreator.createError(getModulePrefixedReason(ARRAY_LANG_LIB,
                                                                   OPERATION_NOT_SUPPORTED_IDENTIFIER),
                                           NOT_SUPPORTED_ERROR_DETAIL);
        }

        // Implementation borrowed from https://stackoverflow.com/a/9855338
        byte[] bytes = arr.getBytes();
        char[] base16Chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            base16Chars[i * 2] = chars[v >>> 4];
            base16Chars[i * 2 + 1] = chars[v & 0xF];
        }

        return StringUtils.fromString(new String(base16Chars));
    }
}
