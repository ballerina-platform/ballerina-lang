/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.langlib.string;

import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.util.exceptions.BallerinaErrorReasons;
import io.ballerina.runtime.util.exceptions.RuntimeErrors;

import static org.ballerinalang.langlib.string.utils.StringUtils.createNullReferenceError;

/**
 * Extern function ballerina.model.arrays:substring(string, int, int).
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.string",
//        functionName = "substring",
//        args = {@Argument(name = "mainString", type = TypeKind.STRING),
//                @Argument(name = "startIndex", type = TypeKind.INT),
//                @Argument(name = "endIndex", type = TypeKind.INT)},
//        returnType = {@ReturnType(type = TypeKind.STRING)},
//        isPublic = true
//)
public class Substring {

    public static BString substring(BString value, long startIndex, long endIndex) {
        if (value == null) {
            throw createNullReferenceError();
        }
        if (startIndex != (int) startIndex) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, startIndex);
        }
        if (endIndex != (int) endIndex) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                    RuntimeErrors.INDEX_NUMBER_TOO_LARGE, endIndex);
        }

        if (startIndex < 0 || endIndex > value.length()) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                    RuntimeErrors.STRING_INDEX_OUT_OF_RANGE, value.length(), startIndex, endIndex);
        }
        if (endIndex < startIndex) {
            throw BLangExceptionHelper.getRuntimeException(BallerinaErrorReasons.STRING_OPERATION_ERROR,
                    RuntimeErrors.INVALID_SUBSTRING_RANGE, value.length(), startIndex, endIndex);
        }
        return value.substring((int) startIndex, (int) endIndex);
    }
}
