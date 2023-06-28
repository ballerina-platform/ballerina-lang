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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;

import static io.ballerina.runtime.api.constants.RuntimeConstants.BOOLEAN_LANG_LIB;
import static io.ballerina.runtime.internal.errors.ErrorReasons.BOOLEAN_PARSING_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.errors.ErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.boolean:fromString(string).
 *
 * @since 1.2.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.boolean", functionName = "fromString",
//        args = {@Argument(name = "s", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.UNION)},
//        isPublic = true
//)
public class FromString {

    private static final BString ERROR_REASON = getModulePrefixedReason(BOOLEAN_LANG_LIB,
                                                                        BOOLEAN_PARSING_ERROR_IDENTIFIER);

    public static Object fromString(BString str) {
        String s = str.getValue();
        try {
            return TypeConverter.stringToBoolean(s);
        } catch (NumberFormatException e) {
            BString msg = ErrorHelper.getErrorMessage(ErrorCodes.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                                                               PredefinedTypes.TYPE_STRING, s,
                                                               PredefinedTypes.TYPE_BOOLEAN);
            return ErrorCreator.createError(ERROR_REASON, msg);
        }

    }
}
