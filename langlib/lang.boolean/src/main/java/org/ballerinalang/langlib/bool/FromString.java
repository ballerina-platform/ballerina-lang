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

package org.ballerinalang.langlib.bool;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import static org.ballerinalang.jvm.util.BLangConstants.BOOLEAN_LANG_LIB;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.BOOLEAN_PARSING_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;
import static org.ballerinalang.util.BLangCompilerConstants.BOOLEAN_VERSION;

/**
 * Native implementation of lang.boolean:fromString(string).
 *
 * @since 1.2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.boolean", version = BOOLEAN_VERSION, functionName = "fromString",
        args = {@Argument(name = "s", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.UNION)},
        isPublic = true
)
public class FromString {

    public static Object fromString(Strand strand, BString str) {
        String s = str.getValue();
        if ("true".equalsIgnoreCase(s) || "1".equalsIgnoreCase(s)) {
            return true;
        } else if ("false".equalsIgnoreCase(s) || "0".equalsIgnoreCase(s)) {
            return false;
        } else {
            String reason = getModulePrefixedReason(BOOLEAN_LANG_LIB, BOOLEAN_PARSING_ERROR_IDENTIFIER);
            String msg = BLangExceptionHelper.getErrorMessage(RuntimeErrors.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                                              BTypes.typeString, s, BTypes.typeBoolean);
            return BallerinaErrors.createError(reason, msg);
        }
    }
}
