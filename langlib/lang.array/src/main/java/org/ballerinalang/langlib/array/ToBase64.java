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
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Base64;

import static org.ballerinalang.jvm.util.BLangConstants.ARRAY_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.OPERATION_NOT_SUPPORTED_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.ballerinalang.util.BLangCompilerConstants.ARRAY_VERSION;

/**
 * Native implementation of lang.array:toBase64(byte[]).
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.array", version = ARRAY_VERSION, functionName = "toBase64",
        args = {@Argument(name = "arr", type = TypeKind.ARRAY)},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class ToBase64 {

    public static BString toBase64(Strand strand, ArrayValue arr) {
        BType arrType = arr.getType();
        if (arrType.getTag() != TypeTags.ARRAY_TAG ||
                ((BArrayType) arrType).getElementType().getTag() != TypeTags.BYTE_TAG) {
            throw BallerinaErrors.createError(getModulePrefixedReason(ARRAY_LANG_LIB,
                                                                      OPERATION_NOT_SUPPORTED_IDENTIFIER),
                                              "toBase64() is only supported on 'byte[]'");
        }
        return StringUtils.fromString(Base64.getEncoder().encodeToString(arr.getBytes()));
    }
}
