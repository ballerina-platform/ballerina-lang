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

import io.ballerina.jvm.api.BErrorCreator;
import io.ballerina.jvm.api.BStringUtils;
import io.ballerina.jvm.api.TypeTags;
import io.ballerina.jvm.api.types.Type;
import io.ballerina.jvm.api.values.BString;
import io.ballerina.jvm.types.BArrayType;
import io.ballerina.jvm.values.ArrayValue;

import java.util.Base64;

import static io.ballerina.jvm.util.BLangConstants.ARRAY_LANG_LIB;
import static io.ballerina.jvm.util.exceptions.BallerinaErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static io.ballerina.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.array:toBase64(byte[]).
 *
 * @since 1.0
 */
public class ToBase64 {

    private static final BString NOT_SUPPORT_DETAIL_ERROR = BStringUtils
            .fromString("toBase64() is only supported on 'byte[]'");

    public static BString toBase64(ArrayValue arr) {
        Type arrType = arr.getType();
        if (arrType.getTag() != TypeTags.ARRAY_TAG ||
                ((BArrayType) arrType).getElementType().getTag() != TypeTags.BYTE_TAG) {
            throw BErrorCreator.createError(getModulePrefixedReason(ARRAY_LANG_LIB,
                                                                    OPERATION_NOT_SUPPORTED_IDENTIFIER),
                                            NOT_SUPPORT_DETAIL_ERROR);
        }
        return BStringUtils.fromString(Base64.getEncoder().encodeToString(arr.getBytes()));
    }
}
