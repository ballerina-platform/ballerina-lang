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

package org.ballerinalang.langlib.decimal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.errors.ErrorCodes;
import io.ballerina.runtime.internal.errors.ErrorHelper;

import static io.ballerina.runtime.api.constants.RuntimeConstants.DECIMAL_LANG_LIB;
import static io.ballerina.runtime.internal.errors.ErrorReasons.NUMBER_PARSING_ERROR_IDENTIFIER;
import static io.ballerina.runtime.internal.errors.ErrorReasons.getModulePrefixedReason;

/**
 * Native implementation of lang.decimal:fromString(string).
 *
 * @since 1.0
 */
//@BallerinaFunction(
//        orgName = "ballerina", packageName = "lang.decimal", functionName = "fromString",
//        args = {@Argument(name = "s", type = TypeKind.STRING)},
//        returnType = {@ReturnType(type = TypeKind.UNION)},
//        isPublic = true
//)
public class FromString {

    private static final BString ERROR_REASON = getModulePrefixedReason(DECIMAL_LANG_LIB,
                                                                        NUMBER_PARSING_ERROR_IDENTIFIER);

    public static Object fromString(BString s) {
        String decimalFloatingPointNumber = s.getValue();
        try {
            return TypeConverter.stringToDecimal(decimalFloatingPointNumber);
        } catch (NumberFormatException e) {
            return getTypeConversionError(decimalFloatingPointNumber);
        }
    }

    private static BError getTypeConversionError(String value) {
        return ErrorCreator.createError(ERROR_REASON, ErrorHelper.getErrorDetails(
                        ErrorCodes.INCOMPATIBLE_SIMPLE_TYPE_CONVERT_OPERATION,
                        PredefinedTypes.TYPE_STRING, value, PredefinedTypes.TYPE_DECIMAL));
    }
}
