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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;

import java.util.Base64;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ARRAY_LANG_LIB;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static io.ballerina.runtime.internal.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.array:toBase64(byte[]).
 *
 * @since 1.0
 */
public class ToBase64 {

    private static final BString NOT_SUPPORT_DETAIL_ERROR = StringUtils
            .fromString("toBase64() is only supported on 'byte[]'");

    public static BString toBase64(BArray arr) {
        Type arrType = TypeUtils.getReferredType(arr.getType());
        if (arrType.getTag() != TypeTags.ARRAY_TAG ||
                ((ArrayType) arrType).getElementType().getTag() != TypeTags.BYTE_TAG) {
            throw ErrorCreator.createError(getModulePrefixedReason(ARRAY_LANG_LIB,
                                                                   OPERATION_NOT_SUPPORTED_IDENTIFIER),
                                           NOT_SUPPORT_DETAIL_ERROR);
        }
        return StringUtils.fromString(Base64.getEncoder().encodeToString(arr.getBytes()));
    }
}
